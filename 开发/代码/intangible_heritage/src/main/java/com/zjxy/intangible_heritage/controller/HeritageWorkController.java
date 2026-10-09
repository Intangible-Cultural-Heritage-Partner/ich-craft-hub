package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.service.FavoriteService;
import com.zjxy.intangible_heritage.service.HeritageWorkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HeritageWorkController {

    private final HeritageWorkService heritageWorkService;
    private final FavoriteService favoriteService;

    @GetMapping("/work/list")
    public String list(Model model, @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String category) {
        model.addAttribute("works", heritageWorkService.findAll(category));
        model.addAttribute("categoryOptions", heritageWorkService.findCategories(null));
        model.addAttribute("selectedCategory", category == null ? "" : category.trim());
        model.addAttribute("mine", false);
        model.addAttribute("msg", msg);
        return "work/list";
    }

    @GetMapping("/work/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        return heritageWorkService.findById(id)
                .map(work -> {
                    User loginUser = (User) session.getAttribute("loginUser");
                    boolean isOwner = work.getCraftsman() != null && loginUser != null
                            && work.getCraftsman().getId().equals(loginUser.getId());
                    boolean isAdmin = loginUser != null && "2".equals(loginUser.getRole());
                    if (work.getAuditStatus() != null && work.getAuditStatus() != 1 && !isOwner && !isAdmin) {
                        return "redirect:/work/list?msg=展品暂未审核通过";
                    }
                    List<String> galleryImages = new ArrayList<>();
                     addImageIfPresent(galleryImages, work.getCoverImg());
                     if (work.getImageList() != null) {
                         Arrays.stream(work.getImageList().split("[,\\r\\n]+"))
                                 .map(String::trim)
                                 .forEach(image -> addImageIfPresent(galleryImages, image));
                     }
                     model.addAttribute("work", work);
                     model.addAttribute("galleryImages", galleryImages);
                     return "work/detail";
                    model.addAttribute("work", work);
                    if (loginUser != null) {
                        model.addAttribute("isFavorited", favoriteService.isFavorited(loginUser.getId(), id, "work"));
                    } else {
                        model.addAttribute("isFavorited", false);
                    }
                    return "work/detail";
                })
                .orElseGet(() -> "redirect:/work/list?msg=展品不存在");
    }

    private void addImageIfPresent(List<String> images, String image) {
        if (image != null && !image.isBlank() && !images.contains(image.trim())) {
            images.add(image.trim());
        }
    }

    @ModelAttribute("categoryOptions")
    public List<String> categoryOptions() {
        return HeritageWorkService.DEFAULT_CATEGORIES;
    }

    private void includeCurrentCategory(Model model, HeritageWork work) {
        List<String> options = new ArrayList<>(HeritageWorkService.DEFAULT_CATEGORIES);
        if (work.getCategory() != null && !work.getCategory().isBlank() && !options.contains(work.getCategory())) {
            options.add(work.getCategory());
        }
        model.addAttribute("categoryOptions", options);
    }
    @GetMapping("/work/create")
    public String createPage(HttpSession session, Model model) {
        if (currentCraftsman(session) == null) {
            return "redirect:/work/list?msg=请先以匠人身份登录";
        }
        model.addAttribute("work", new HeritageWork());
        model.addAttribute("formAction", "/work/create");
        return "work/form";
    }

    @PostMapping("/work/create")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String skillBackground,
                         @RequestParam(required = false) String modelUrl,
                         @RequestParam(required = false) MultipartFile modelFile,
                         @RequestParam(required = false) MultipartFile coverFile,
                         @RequestParam(required = false) String coverUrl,
                         @RequestParam(required = false) MultipartFile[] imageFiles,
                         @RequestParam(required = false) String imageUrls,
                         HttpSession session,
                         Model model) {
        User user = currentCraftsman(session);
        if (user == null) {
            return "redirect:/work/list?msg=请先以匠人身份登录";
        }
        try {
            HeritageWork work = heritageWorkService.create(user, title, description, skillBackground,
                    modelUrl, modelFile, coverFile, coverUrl, imageFiles, imageUrls, category);
            return "redirect:/work/" + work.getId();
        } catch (RuntimeException exception) {
            HeritageWork formWork = new HeritageWork();
            formWork.setTitle(title);
            formWork.setDescription(description);
            formWork.setSkillBackground(skillBackground);
            formWork.setModelUrl(modelUrl);
            formWork.setCategory(category);
            includeCurrentCategory(model, formWork);
            model.addAttribute("work", formWork);
            model.addAttribute("formAction", "/work/create");
            model.addAttribute("msg", exception.getMessage());
            return "work/form";
        }
    }

    @GetMapping("/work/{id}/edit")
    public String editPage(@PathVariable Long id, HttpSession session, Model model) {
        User user = currentCraftsman(session);
        HeritageWork work = heritageWorkService.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/work/list?msg=请先以匠人身份登录";
        }
        if (work == null || work.getCraftsman() == null || !user.getId().equals(work.getCraftsman().getId())) {
            return "redirect:/work/list?msg=无权编辑该展品";
        }
        model.addAttribute("work", work);
        includeCurrentCategory(model, work);
        model.addAttribute("formAction", "/work/" + id + "/edit");
        return "work/form";
    }

    @PostMapping("/work/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "false") boolean removeModel,
                       @RequestParam(required = false) String description,
                       @RequestParam(required = false) String skillBackground,
                       @RequestParam(required = false) String modelUrl,
                       @RequestParam(required = false) MultipartFile modelFile,
                       @RequestParam(required = false) MultipartFile coverFile,
                       @RequestParam(required = false) String coverUrl,
                       @RequestParam(required = false) MultipartFile[] imageFiles,
                       @RequestParam(required = false) String imageUrls,
                       HttpSession session,
                       Model model) {
        User user = currentCraftsman(session);
        if (user == null) {
            return "redirect:/work/list?msg=请先以匠人身份登录";
        }
        try {
            HeritageWork work = heritageWorkService.update(id, user, title, description, skillBackground,
                    modelUrl, modelFile, coverFile, coverUrl, imageFiles, imageUrls, category, removeModel);
            return "redirect:/work/" + id;
        } catch (RuntimeException exception) {
            HeritageWork formWork = heritageWorkService.findById(id).orElse(new HeritageWork());
            if (formWork.getCraftsman() == null || !user.getId().equals(formWork.getCraftsman().getId())) {
                return "redirect:/work/list?msg=无权编辑该展品";
            }
            formWork.setTitle(title);
            formWork.setDescription(description);
            formWork.setSkillBackground(skillBackground);
            if (category != null && !category.isBlank()) {
                formWork.setCategory(category.trim());
            }
            includeCurrentCategory(model, formWork);
            if (modelUrl != null && !modelUrl.isBlank()) {
                formWork.setModelUrl(modelUrl);
            }
            model.addAttribute("work", formWork);
            model.addAttribute("formAction", "/work/" + id + "/edit");
            model.addAttribute("removeModel", removeModel);
            model.addAttribute("msg", exception.getMessage());
            return "work/form";
        }
    }

    @GetMapping("/work/mine")
    public String mine(HttpSession session, Model model, @RequestParam(required = false) String category) {
        User user = currentCraftsman(session);
        if (user == null) {
            return "redirect:/work/list?msg=请先以匠人身份登录";
        }
        model.addAttribute("works", heritageWorkService.findByCraftsman(user.getId(), category));
        model.addAttribute("categoryOptions", heritageWorkService.findCategories(user.getId()));
        model.addAttribute("selectedCategory", category == null ? "" : category.trim());
        model.addAttribute("mine", true);
        return "work/list";
    }

    private User currentCraftsman(HttpSession session) {
        Object value = session.getAttribute("loginUser");
        if (!(value instanceof User user) || user.getId() == null) {
            return null;
        }
        String role = user.getRole();
        return "CRAFTSMAN".equalsIgnoreCase(role) || "1".equals(role) ? user : null;
    }

}
