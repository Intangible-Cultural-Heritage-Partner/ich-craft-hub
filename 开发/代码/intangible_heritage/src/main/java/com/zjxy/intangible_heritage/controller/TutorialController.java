package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.Tutorial;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.UserRepository;
import com.zjxy.intangible_heritage.service.TutorialService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TutorialController {

    private final TutorialService tutorialService;
    private final UserRepository userRepository;

    private static final List<String> CATEGORIES = Arrays.asList(
            "剪纸", "刺绣", "陶艺", "木雕", "扎染", "漆器", "竹编", "皮影", "泥塑", "其他"
    );

    @GetMapping("/tutorial/list")
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String msg,
                       Model model) {
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("currentCategory", category);
        model.addAttribute("mine", false);
        if (category != null && !category.isBlank() && !"全部".equals(category)) {
            model.addAttribute("tutorials", tutorialService.findByCategory(category));
        } else {
            model.addAttribute("tutorials", tutorialService.findAll());
        }
        model.addAttribute("msg", msg);
        return "tutorial/list";
    }

    @GetMapping("/tutorial/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        return tutorialService.findById(id).map(tutorial -> {
            User loginUser = (User) session.getAttribute("loginUser");
            boolean isOwner = loginUser != null && loginUser.getId().equals(tutorial.getCraftsmanId());
            boolean isAdmin = loginUser != null && "2".equals(loginUser.getRole());
            if (tutorial.getAuditStatus() != null && tutorial.getAuditStatus() != 1 && !isOwner && !isAdmin) {
                return "redirect:/tutorial/list?msg=教程暂未审核通过";
            }
            model.addAttribute("tutorial", tutorial);
            userRepository.findById(tutorial.getCraftsmanId())
                    .ifPresent(c -> model.addAttribute("craftsman", c));
            if (tutorial.getTags() != null && !tutorial.getTags().isBlank()) {
                model.addAttribute("tagList", Arrays.asList(tutorial.getTags().split("[,，\\s]+")));
            }
            return "tutorial/detail";
        }).orElseGet(() -> "redirect:/tutorial/list?msg=教程不存在");
    }

    @GetMapping("/tutorial/create")
    public String createPage(HttpSession session, Model model) {
        if (currentCraftsman(session) == null) {
            return "redirect:/tutorial/list?msg=请先以匠人身份登录";
        }
        model.addAttribute("tutorial", new Tutorial());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("formAction", "/tutorial/create");
        return "tutorial/form";
    }

    @PostMapping("/tutorial/create")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "其他") String category,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false) String content,
                         @RequestParam(required = false) MultipartFile coverFile,
                         @RequestParam(required = false) String coverUrl,
                         @RequestParam(required = false) MultipartFile videoFile,
                         @RequestParam(required = false) String videoUrl,
                         HttpSession session, Model model) {
        User user = currentCraftsman(session);
        if (user == null) {
            return "redirect:/tutorial/list?msg=请先以匠人身份登录";
        }
        try {
            Tutorial tutorial = tutorialService.create(user.getId(), title, description, category, tags,
                    content, coverFile, coverUrl, videoFile, videoUrl);
            return "redirect:/tutorial/" + tutorial.getId();
        } catch (RuntimeException e) {
            Tutorial formTutorial = new Tutorial();
            formTutorial.setTitle(title);
            formTutorial.setDescription(description);
            formTutorial.setCategory(category);
            formTutorial.setTags(tags);
            formTutorial.setContent(content);
            model.addAttribute("tutorial", formTutorial);
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("formAction", "/tutorial/create");
            model.addAttribute("msg", e.getMessage());
            return "tutorial/form";
        }
    }

    @GetMapping("/tutorial/{id}/edit")
    public String editPage(@PathVariable Long id, HttpSession session, Model model) {
        User user = currentCraftsman(session);
        Tutorial tutorial = tutorialService.findById(id).orElse(null);
        if (user == null) return "redirect:/tutorial/list?msg=请先以匠人身份登录";
        if (tutorial == null || !user.getId().equals(tutorial.getCraftsmanId())) {
            return "redirect:/tutorial/list?msg=无权编辑该教程";
        }
        model.addAttribute("tutorial", tutorial);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("formAction", "/tutorial/" + id + "/edit");
        return "tutorial/form";
    }

    @PostMapping("/tutorial/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam(required = false) String description,
                       @RequestParam(defaultValue = "其他") String category,
                       @RequestParam(required = false) String tags,
                       @RequestParam(required = false) String content,
                       @RequestParam(required = false) MultipartFile coverFile,
                       @RequestParam(required = false) String coverUrl,
                       @RequestParam(required = false) MultipartFile videoFile,
                       @RequestParam(required = false) String videoUrl,
                       HttpSession session, Model model) {
        User user = currentCraftsman(session);
        if (user == null) return "redirect:/tutorial/list?msg=请先以匠人身份登录";
        try {
            tutorialService.update(id, user.getId(), title, description, category, tags,
                    content, coverFile, coverUrl, videoFile, videoUrl);
            return "redirect:/tutorial/" + id;
        } catch (RuntimeException e) {
            Tutorial formTutorial = tutorialService.findById(id).orElse(new Tutorial());
            formTutorial.setTitle(title);
            formTutorial.setDescription(description);
            formTutorial.setCategory(category);
            formTutorial.setTags(tags);
            formTutorial.setContent(content);
            model.addAttribute("tutorial", formTutorial);
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("formAction", "/tutorial/" + id + "/edit");
            model.addAttribute("msg", e.getMessage());
            return "tutorial/form";
        }
    }

    @GetMapping("/tutorial/mine")
    public String mine(HttpSession session, Model model) {
        User user = currentCraftsman(session);
        if (user == null) return "redirect:/tutorial/list?msg=请先以匠人身份登录";
        model.addAttribute("tutorials", tutorialService.findByCraftsman(user.getId()));
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("mine", true);
        return "tutorial/list";
    }

    private User currentCraftsman(HttpSession session) {
        Object value = session.getAttribute("loginUser");
        if (!(value instanceof User user) || user.getId() == null) return null;
        String role = user.getRole();
        return "CRAFTSMAN".equalsIgnoreCase(role) || "1".equals(role) ? user : null;
    }
}