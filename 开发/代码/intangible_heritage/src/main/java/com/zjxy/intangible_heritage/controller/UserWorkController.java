package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.Comment;
import com.zjxy.intangible_heritage.entity.LikeRecord;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.entity.UserWork;
import com.zjxy.intangible_heritage.entity.UserWorkComment;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/userWork")
public class UserWorkController {

    private final UserWorkService userWorkService;
    private final InteractionService interactionService;
    private final FileUploadUtil fileUploadUtil;
    private final UserRepository userRepository;

    @GetMapping("/userWork/share")
    public String shareList(Model model) {
        List<UserWork> works = userWorkService.findAllPassed();
        model.addAttribute("works", works);
        return "userWork/share";
    }
    private static final int TYPE_USER_WORK = 3;

    @GetMapping("/share")
    public String shareList(Model model, HttpSession session) {
        List<UserWork> list = userWorkService.listApproved();
        model.addAttribute("works", list);

        Map<Long, Long> likeCounts = list.stream()
                .collect(Collectors.toMap(UserWork::getId,
                        w -> interactionService.countLikes(TYPE_USER_WORK, w.getId())));
        Map<Long, Long> commentCounts = list.stream()
                .collect(Collectors.toMap(UserWork::getId,
                        w -> interactionService.countComments(TYPE_USER_WORK, w.getId())));
        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("commentCounts", commentCounts);

    @GetMapping("/userWork/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        return userWorkService.findById(id).map(work -> {
            User loginUser = (User) session.getAttribute("loginUser");
            boolean isOwner = loginUser != null && loginUser.getId().equals(work.getUserId());
            boolean isAdmin = loginUser != null && "2".equals(loginUser.getRole());
            if (work.getAuditStatus() != null && work.getAuditStatus() != 1 && !isOwner && !isAdmin) {
                return "redirect:/userWork/share?msg=作品暂未审核通过";
            }
            model.addAttribute("work", work);
            userRepository.findById(work.getUserId()).ifPresent(u -> model.addAttribute("author", u));
            model.addAttribute("likeCount", userWorkService.likeCount(id));
            model.addAttribute("commentCount", userWorkService.commentCount(id));
            if (loginUser != null) {
                model.addAttribute("isLiked", userWorkService.isLiked(loginUser.getId(), id));
            } else {
                model.addAttribute("isLiked", false);
            }
            List<UserWorkComment> comments = userWorkService.findComments(id);
            Map<Long, String> userNames = new HashMap<>();
            for (UserWorkComment c : comments) {
                if (!userNames.containsKey(c.getUserId())) {
                    userRepository.findById(c.getUserId()).ifPresent(u -> userNames.put(u.getId(), u.getUsername()));
                }
            }
            model.addAttribute("comments", comments);
            model.addAttribute("userNames", userNames);
            return "userWork/detail";
        }).orElseGet(() -> "redirect:/userWork/share?msg=作品不存在");
        User loginUser = (User) session.getAttribute("loginUser");
        model.addAttribute("loginUser", loginUser);
        return "userWork/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        UserWork work = userWorkService.findById(id);
        if (work == null) {
            return "redirect:/userWork/share";
        }
        model.addAttribute("work", work);

        // 顶层评论
        List<Comment> topComments = interactionService.listTopComments(TYPE_USER_WORK, id);
        model.addAttribute("comments", topComments);

        // 每条评论的回复 + 所有评论者 id
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

        // 评论者信息
        Map<Long, User> commentUserMap = new HashMap<>();
        for (Long uid : userIds) {
            userRepository.findById(uid).ifPresent(u -> commentUserMap.put(u.getId(), u));
        }
        model.addAttribute("commentUserMap", commentUserMap);

        model.addAttribute("likeCount", interactionService.countLikes(TYPE_USER_WORK, id));

        User loginUser = (User) session.getAttribute("loginUser");
        boolean liked = false;
        if (loginUser != null) {
            liked = interactionService.isLiked(loginUser.getId(), TYPE_USER_WORK, id);
        }
        model.addAttribute("liked", liked);
        model.addAttribute("loginUser", loginUser);
        return "userWork/detail";
    }

    @GetMapping("/userWork/publish")
    public String publishPage(HttpSession session) {
        if (session.getAttribute("loginUser") == null) return "redirect:/login";
        return "userWork/publish";
    }

    @PostMapping("/userWork/publish")
    public String doPublish(@RequestParam String title,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) MultipartFile coverFile,
                            @RequestParam(required = false) MultipartFile[] imageFiles,
                            HttpSession session, Model model) {
                            @RequestParam(required = false) MultipartFile videoFile,
                            HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        UserWork userWork = new UserWork();
        userWork.setUserId(loginUser.getId());
        userWork.setTitle(title);
        userWork.setDescription(description);

        if (coverFile != null && !coverFile.isEmpty()) {
            userWork.setCoverImg(fileUploadUtil.save(coverFile, "userwork"));
        }

        if (imageFiles != null && imageFiles.length > 0) {
            List<String> paths = new ArrayList<>();
            for (MultipartFile f : imageFiles) {
                if (!f.isEmpty()) {
                    paths.add(fileUploadUtil.save(f, "userwork"));
                }
            }
            userWork.setImageList(String.join(",", paths));
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            userWork.setVideoUrl(fileUploadUtil.saveVideo(videoFile));
        }

        userWorkService.publish(userWork);
        return "redirect:/userWork/my";
    }

    @GetMapping("/my")
    public String myWorks(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        try {
            UserWork work = userWorkService.publish(loginUser.getId(), title, description, coverFile, imageFiles);
            return "redirect:/userWork/" + work.getId();
        } catch (RuntimeException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            return "userWork/publish";
        if (loginUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("works", userWorkService.listByUser(loginUser.getId()));
        return "userWork/my";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        UserWork work = userWorkService.findById(id);
        if (work != null && work.getUserId().equals(loginUser.getId())) {
            userWorkService.deleteById(id);
        }
        return "redirect:/userWork/my";
    }

    @PostMapping("/userWork/{id}/like")
    @PostMapping("/like/{id}")
    public String toggleLike(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        userWorkService.toggleLike(loginUser.getId(), id);
        return "redirect:/userWork/" + id;
        if (loginUser == null) {
            return "redirect:/login";
        }
        interactionService.toggleLike(loginUser.getId(), TYPE_USER_WORK, id);
        return "redirect:/userWork/detail/" + id;
    }

    @PostMapping("/userWork/{id}/comment")
    @PostMapping("/comment/{id}")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        try {
            userWorkService.addComment(id, loginUser.getId(), content);
        } catch (RuntimeException ignored) {}
        return "redirect:/userWork/" + id;
        if (loginUser == null) {
            return "redirect:/login";
        }
        interactionService.addComment(loginUser.getId(), TYPE_USER_WORK, id, content);
        return "redirect:/userWork/detail/" + id;
    }

    @GetMapping("/userWork/myPublish")
    public String myPublish(HttpSession session, Model model) {
    @PostMapping("/comment/reply/{parentId}")
    public String replyComment(@PathVariable Long parentId,
                               @RequestParam String content,
                               HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        model.addAttribute("works", userWorkService.findByUserId(loginUser.getId()));
        model.addAttribute("mine", true);
        return "userWork/share";
        if (loginUser == null) {
            return "redirect:/login";
        }
        Comment parent = interactionService.findCommentById(parentId);
        if (parent == null) {
            return "redirect:/userWork/share";
        }
        interactionService.replyComment(loginUser.getId(), parentId, content);
        return "redirect:/userWork/detail/" + parent.getTargetId();
    }

    @PostMapping("/userWork/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
    @GetMapping("/myLikes")
    public String myLikes(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        try {
            userWorkService.delete(id, loginUser.getId());
        } catch (RuntimeException ignored) {}
        return "redirect:/userWork/myPublish";
        if (loginUser == null) {
            return "redirect:/login";
        }
        List<LikeRecord> likes = interactionService.listLikesByUser(loginUser.getId(), TYPE_USER_WORK);
        List<UserWork> works = likes.stream()
                .map(l -> userWorkService.findById(l.getTargetId()))
                .filter(w -> w != null)
                .toList();
        model.addAttribute("works", works);
        return "userWork/myLikes";
    }

    @GetMapping("/myComments")
    public String myComments(Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        List<Comment> comments = interactionService.listMyComments(loginUser.getId());

        Map<Long, UserWork> workMap = new HashMap<>();
        for (Comment c : comments) {
            if (c.getTargetType() == TYPE_USER_WORK && !workMap.containsKey(c.getTargetId())) {
                UserWork w = userWorkService.findById(c.getTargetId());
                if (w != null) {
                    workMap.put(c.getTargetId(), w);
                }
            }
        }

        model.addAttribute("comments", comments);
        model.addAttribute("workMap", workMap);
        return "userWork/myComments";
    }
}