package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.Tutorial;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.UserRepository;
import com.zjxy.intangible_heritage.service.FavoriteService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class TutorialController {

    private final TutorialService tutorialService;
    private final UserRepository userRepository;
    private final FavoriteService favoriteService;

    private static final List<String> CATEGORIES = Arrays.asList(
            "剪纸", "刺绣", "陶艺", "木雕", "扎染", "漆器", "竹编", "皮影", "泥塑", "其他"
    );

    @GetMapping("/tutorial/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            ra.addFlashAttribute("msg", "请先登录");
            return "redirect:/login";
        }
        Tutorial tutorial = tutorialService.findById(id).orElse(null);
        if (tutorial == null) {
            ra.addFlashAttribute("msg", "教程不存在");
            return "redirect:/tutorial/mine";
        }
        if (!user.getId().equals(tutorial.getCraftsmanId())) {
            ra.addFlashAttribute("msg", "无权删除该教程");
            return "redirect:/tutorial/mine";
        }
        tutorialService.deleteById(id);
        ra.addFlashAttribute("msg", "教程已删除");
        return "redirect:/tutorial/mine";
    }

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
        if (msg != null) model.addAttribute("msg", msg);
        return "tutorial/list";
    }

    @GetMapping("/tutorial/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes ra) {
        return tutorialService.findById(id).map(tutorial -> {
            User loginUser = (User) session.getAttribute("loginUser");
            boolean isOwner = loginUser != null && loginUser.getId().equals(tutorial.getCraftsmanId());
            boolean isAdmin = loginUser != null && "2".equals(loginUser.getRole());
            if (tutorial.getAuditStatus() != null && tutorial.getAuditStatus() != 1 && !isOwner && !isAdmin) {
                ra.addFlashAttribute("msg", "教程暂未审核通过");
                return "redirect:/tutorial/list";
            }
            model.addAttribute("tutorial", tutorial);
            if (loginUser != null) {
                model.addAttribute("isFavorited", favoriteService.isFavorited(loginUser.getId(), id, "tutorial"));
            } else {
                model.addAttribute("isFavorited", false);
            }
            userRepository.findById(tutorial.getCraftsmanId())
                    .ifPresent(c -> model.addAttribute("craftsman", c));
            if (tutorial.getTags() != null && !tutorial.getTags().isBlank()) {
                model.addAttribute("tagList", Arrays.asList(tutorial.getTags().split("[,，\\s]+")));
            }
            return "tutorial/detail";
        }).orElseGet(() -> {
            ra.addFlashAttribute("msg", "教程不存在");
            return "redirect:/tutorial/list";
        });
    }

    @GetMapping("/tutorial/create")
    public String createPage(HttpSession session, Model model, RedirectAttributes ra) {
        if (currentCraftsman(session) == null) {
            ra.addFlashAttribute("msg", "请先以匠人身份登录");
            return "redirect:/tutorial/list";
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
                         @RequestParam(required = false) MultipartFile[] contentImageFiles,
                         @RequestParam(required = false) String contentImages,
                         @RequestParam(required = false) MultipartFile coverFile,
                         @RequestParam(required = false) String coverUrl,
                         @RequestParam(required = false) MultipartFile videoFile,
                         @RequestParam(required = false) String videoUrl,
                         HttpSession session, Model model, RedirectAttributes ra) {
        User user = currentCraftsman(session);
        if (user == null) {
            ra.addFlashAttribute("msg", "请先以匠人身份登录");
            return "redirect:/tutorial/list";
        }
        try {
            Tutorial tutorial = tutorialService.create(user.getId(), title, description, category, tags,
                    content, contentImageFiles, contentImages, coverFile, coverUrl, videoFile, videoUrl);
            return "redirect:/tutorial/" + tutorial.getId();
        } catch (RuntimeException e) {
            Tutorial formTutorial = new Tutorial();
            formTutorial.setTitle(title);
            formTutorial.setDescription(description);
            formTutorial.setCategory(category);
            formTutorial.setTags(tags);
            formTutorial.setContent(content);
            formTutorial.setContentImages(contentImages);
            model.addAttribute("tutorial", formTutorial);
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("formAction", "/tutorial/create");
            model.addAttribute("msg", e.getMessage());
            return "tutorial/form";
        }
    }

    @GetMapping("/tutorial/{id}/edit")
    public String editPage(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        User user = currentCraftsman(session);
        Tutorial tutorial = tutorialService.findById(id).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("msg", "请先以匠人身份登录");
            return "redirect:/tutorial/list";
        }
        if (tutorial == null || !user.getId().equals(tutorial.getCraftsmanId())) {
            ra.addFlashAttribute("msg", "无权编辑该教程");
            return "redirect:/tutorial/list";
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
                       @RequestParam(required = false) MultipartFile[] contentImageFiles,
                       @RequestParam(required = false) String contentImages,
                       @RequestParam(required = false) MultipartFile coverFile,
                       @RequestParam(required = false) String coverUrl,
                       @RequestParam(required = false) MultipartFile videoFile,
                       @RequestParam(required = false) String videoUrl,
                       HttpSession session, Model model, RedirectAttributes ra) {
        User user = currentCraftsman(session);
        if (user == null) {
            ra.addFlashAttribute("msg", "请先以匠人身份登录");
            return "redirect:/tutorial/list";
        }
        try {
            tutorialService.update(id, user.getId(), title, description, category, tags,
                    content, contentImageFiles, contentImages, coverFile, coverUrl, videoFile, videoUrl);
            return "redirect:/tutorial/" + id;
        } catch (RuntimeException e) {
            Tutorial formTutorial = tutorialService.findById(id).orElse(new Tutorial());
            formTutorial.setTitle(title);
            formTutorial.setDescription(description);
            formTutorial.setCategory(category);
            formTutorial.setTags(tags);
            formTutorial.setContent(content);
            formTutorial.setContentImages(contentImages);
            model.addAttribute("tutorial", formTutorial);
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("formAction", "/tutorial/" + id + "/edit");
            model.addAttribute("msg", e.getMessage());
            return "tutorial/form";
        }
    }

    @GetMapping("/tutorial/mine")
    public String mine(@RequestParam(required = false) String category,
                       HttpSession session, Model model, RedirectAttributes ra) {
        User user = currentCraftsman(session);
        if (user == null) {
            ra.addFlashAttribute("msg", "请先以匠人身份登录");
            return "redirect:/tutorial/list";
        }
        if (category != null && !category.isBlank() && !"全部".equals(category)) {
            model.addAttribute("tutorials", tutorialService.findByCraftsmanAndCategory(user.getId(), category));
        } else {
            model.addAttribute("tutorials", tutorialService.findByCraftsman(user.getId()));
        }
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("currentCategory", category);
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