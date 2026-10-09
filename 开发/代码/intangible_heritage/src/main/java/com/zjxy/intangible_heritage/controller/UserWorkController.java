package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.entity.UserWork;
import com.zjxy.intangible_heritage.entity.UserWorkComment;
import com.zjxy.intangible_heritage.repository.UserRepository;
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

@Controller
@RequiredArgsConstructor
public class UserWorkController {

    private final UserWorkService userWorkService;
    private final UserRepository userRepository;

    @GetMapping("/userWork/share")
    public String shareList(Model model) {
        List<UserWork> works = userWorkService.findAllPassed();
        model.addAttribute("works", works);
        return "userWork/share";
    }

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
        }
    }

    @PostMapping("/userWork/{id}/like")
    public String toggleLike(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        userWorkService.toggleLike(loginUser.getId(), id);
        return "redirect:/userWork/" + id;
    }

    @PostMapping("/userWork/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        try {
            userWorkService.addComment(id, loginUser.getId(), content);
        } catch (RuntimeException ignored) {}
        return "redirect:/userWork/" + id;
    }

    @GetMapping("/userWork/myPublish")
    public String myPublish(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        model.addAttribute("works", userWorkService.findByUserId(loginUser.getId()));
        model.addAttribute("mine", true);
        return "userWork/share";
    }

    @PostMapping("/userWork/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        try {
            userWorkService.delete(id, loginUser.getId());
        } catch (RuntimeException ignored) {}
        return "redirect:/userWork/myPublish";
    }
}