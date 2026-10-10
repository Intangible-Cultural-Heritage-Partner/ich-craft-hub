package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.Comment;
import com.zjxy.intangible_heritage.entity.LikeRecord;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.entity.UserWork;
import com.zjxy.intangible_heritage.repository.UserRepository;
import com.zjxy.intangible_heritage.service.FileUploadUtil;
import com.zjxy.intangible_heritage.service.InteractionService;
import com.zjxy.intangible_heritage.service.UserWorkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/userWork")
public class UserWorkController {

    private static final int TYPE_USER_WORK = 3;

    private final UserWorkService userWorkService;
    private final InteractionService interactionService;
    private final FileUploadUtil fileUploadUtil;
    private final UserRepository userRepository;

    @GetMapping("/share")
    public String shareList(Model model) {
        model.addAttribute("works", userWorkService.listApproved());
        return "userWork/share";
    }

    @GetMapping({"/{id}", "/detail/{id}"})
    public String detail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        UserWork work = userWorkService.findById(id).orElse(null);
        if (work == null) {
            ra.addFlashAttribute("msg", "作品不存在");
            return "redirect:/userWork/share";
        }
        User loginUser = (User) session.getAttribute("loginUser");
        boolean isOwner = loginUser != null && loginUser.getId().equals(work.getUserId());
        boolean isAdmin = loginUser != null && "2".equals(loginUser.getRole());
        if (work.getAuditStatus() != null && work.getAuditStatus() != 1 && !isOwner && !isAdmin) {
            ra.addFlashAttribute("msg", "作品暂未审核通过");
            return "redirect:/userWork/share";
        }

        model.addAttribute("work", work);

        // 顶层评论及回复
        List<Comment> topComments = interactionService.listTopComments(TYPE_USER_WORK, id);
        model.addAttribute("comments", topComments);

        Map<Long, List<Comment>> replyMap = new HashMap<>();
        Set<Long> userIds = new HashSet<>();
        for (Comment c : topComments) {
            userIds.add(c.getUserId());
            List<Comment> replies = interactionService.listReplies(c.getId());
            replyMap.put(c.getId(), replies);
            for (Comment r : replies) {
                userIds.add(r.getUserId());
            }
        }
        model.addAttribute("replyMap", replyMap);

        Map<Long, User> commentUserMap = new HashMap<>();
        for (Long uid : userIds) {
            userRepository.findById(uid).ifPresent(u -> commentUserMap.put(u.getId(), u));
        }
        model.addAttribute("commentUserMap", commentUserMap);

        model.addAttribute("likeCount", interactionService.countLikes(TYPE_USER_WORK, id));

        boolean liked = false;
        if (loginUser != null) {
            liked = interactionService.isLiked(loginUser.getId(), TYPE_USER_WORK, id);
        }
        model.addAttribute("liked", liked);
        model.addAttribute("loginUser", loginUser);
        return "userWork/detail";
    }

    @GetMapping("/publish")
    public String publishPage(HttpSession session) {
        if (session.getAttribute("loginUser") == null) return "redirect:/login";
        return "userWork/publish";
    }

    @PostMapping("/publish")
    public String doPublish(@RequestParam String title,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) MultipartFile coverFile,
                            @RequestParam(required = false) MultipartFile[] imageFiles,
                            @RequestParam(required = false) MultipartFile videoFile,
                            HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        try {
            UserWork userWork = new UserWork();
            userWork.setUserId(loginUser.getId());
            userWork.setTitle(title);
            userWork.setDescription(description);

            if (coverFile != null && !coverFile.isEmpty()) {
                userWork.setCoverImg(fileUploadUtil.save(coverFile, "userwork"));
            }
            if (imageFiles != null) {
                List<String> paths = new ArrayList<>();
                for (MultipartFile f : imageFiles) {
                    if (f != null && !f.isEmpty()) {
                        paths.add(fileUploadUtil.save(f, "userwork"));
                    }
                }
                if (!paths.isEmpty()) userWork.setImageList(String.join(",", paths));
            }
            if (videoFile != null && !videoFile.isEmpty()) {
                userWork.setVideoUrl(fileUploadUtil.saveVideo(videoFile));
            }

            userWorkService.publish(userWork);
            return "redirect:/userWork/my";
        } catch (RuntimeException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            return "userWork/publish";
        }
    }

    @GetMapping("/my")
    public String myWorks(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        model.addAttribute("works", userWorkService.listByUser(loginUser.getId()));
        return "userWork/my";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        userWorkService.findById(id).ifPresent(work -> {
            if (work.getUserId().equals(loginUser.getId())) {
                userWorkService.deleteById(id);
            }
        });
        return "redirect:/userWork/my";
    }

    @PostMapping("/like/{id}")
    public String toggleLike(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        interactionService.toggleLike(loginUser.getId(), TYPE_USER_WORK, id);
        return "redirect:/userWork/detail/" + id;
    }

    @PostMapping("/comment/{id}")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        interactionService.addComment(loginUser.getId(), TYPE_USER_WORK, id, content);
        return "redirect:/userWork/detail/" + id;
    }

    @PostMapping("/comment/reply/{parentId}")
    public String replyComment(@PathVariable Long parentId,
                               @RequestParam String content,
                               HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        Comment parent = interactionService.findCommentById(parentId);
        if (parent == null) return "redirect:/userWork/share";
        interactionService.replyComment(loginUser.getId(), parentId, content);
        return "redirect:/userWork/detail/" + parent.getTargetId();
    }

    @GetMapping("/myLikes")
    public String myLikes(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        List<LikeRecord> likes = interactionService.listLikesByUser(loginUser.getId(), TYPE_USER_WORK);
        List<UserWork> works = likes.stream()
                .map(l -> userWorkService.findById(l.getTargetId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        model.addAttribute("works", works);
        return "userWork/myLikes";
    }

    @GetMapping("/myComments")
    public String myComments(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        List<Comment> comments = interactionService.listMyComments(loginUser.getId());

        Map<Long, UserWork> workMap = new HashMap<>();
        for (Comment c : comments) {
            if (c.getTargetType() == TYPE_USER_WORK && !workMap.containsKey(c.getTargetId())) {
                UserWork w = userWorkService.findById(c.getTargetId()).orElse(null);
                if (w != null) workMap.put(c.getTargetId(), w);
            }
        }
        model.addAttribute("comments", comments);
        model.addAttribute("workMap", workMap);
        return "userWork/myComments";
    }
}