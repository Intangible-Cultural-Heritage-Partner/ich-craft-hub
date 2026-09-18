package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.service.FileStorageService;
import com.zjxy.intangible_heritage.service.HeritageWorkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HeritageWorkController {

    private final HeritageWorkService heritageWorkService;
    private final FileStorageService fileStorageService;

    // ============ 前台展示 ============

    //非遗展品列表（审核通过的）
    @GetMapping("/work/list")
    public String list(Model model) {
        model.addAttribute("works", heritageWorkService.listPublished());
        return "work/list";
    }

    //展品详情
    @GetMapping("/work/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        HeritageWork work = heritageWorkService.findById(id).orElse(null);
        if (work == null) {
            return "redirect:/work/list";
        }
        User loginUser = (User) session.getAttribute("loginUser");
        //未审核通过的展品，仅本人匠人可预览
        if (!Integer.valueOf(1).equals(work.getAuditStatus()) && !isOwner(work, loginUser)) {
            return "redirect:/work/list";
        }
        model.addAttribute("work", work);
        model.addAttribute("images", splitImages(work.getImageList()));
        return "work/detail";
    }

    // ============ 匠人发布 ============

    @GetMapping("/heritageWork/publish")
    public String publishPage(HttpSession session) {
        if (!isCraftsman(session)) {
            return "redirect:/login";
        }
        return "work/form";
    }

    @PostMapping("/heritageWork/publish")
    public String publish(@RequestParam String title,
                          @RequestParam String category,
                          @RequestParam(required = false) String skillBackground,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false) MultipartFile coverImgFile,
                          @RequestParam(required = false) MultipartFile[] imageFiles,
                          @RequestParam(required = false) MultipartFile modelFile,
                          HttpSession session,
                          Model model) {
        if (!isCraftsman(session)) {
            return "redirect:/login";
        }
        User craftsman = (User) session.getAttribute("loginUser");
        if (title == null || title.isBlank() || category == null || category.isBlank()) {
            model.addAttribute("msg", "作品标题和非遗分类不能为空");
            return "work/form";
        }
        HeritageWork work = new HeritageWork();
        try {
            work.setTitle(title.trim());
            work.setCategory(category.trim());
            work.setSkillBackground(skillBackground);
            work.setDescription(description);
            work.setCoverImg(fileStorageService.storeImage(coverImgFile));
            work.setImageList(storeImages(imageFiles));
            work.setModelUrl(fileStorageService.storeModel(modelFile));
        } catch (IllegalArgumentException e) {
            model.addAttribute("msg", e.getMessage());
            return "work/form";
        }
        heritageWorkService.publish(work, craftsman);
        return "redirect:/heritageWork/my";
    }

    // ============ 匠人编辑 ============

    @GetMapping("/heritageWork/edit/{id}")
    public String editPage(@PathVariable Long id, HttpSession session, Model model) {
        if (!isCraftsman(session)) {
            return "redirect:/login";
        }
        HeritageWork work = heritageWorkService.findById(id).orElse(null);
        User craftsman = (User) session.getAttribute("loginUser");
        if (work == null || !isOwner(work, craftsman)) {
            return "redirect:/heritageWork/my";
        }
        model.addAttribute("work", work);
        return "work/form";
    }

    @PostMapping("/heritageWork/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String category,
                       @RequestParam(required = false) String skillBackground,
                       @RequestParam(required = false) String description,
                       @RequestParam(required = false) MultipartFile coverImgFile,
                       @RequestParam(required = false) MultipartFile[] imageFiles,
                       @RequestParam(required = false) MultipartFile modelFile,
                       HttpSession session,
                       Model model) {
        if (!isCraftsman(session)) {
            return "redirect:/login";
        }
        User craftsman = (User) session.getAttribute("loginUser");
        if (title == null || title.isBlank() || category == null || category.isBlank()) {
            model.addAttribute("msg", "作品标题和非遗分类不能为空");
            model.addAttribute("work", heritageWorkService.findById(id).orElse(null));
            return "work/form";
        }
        HeritageWork form = new HeritageWork();
        try {
            form.setTitle(title.trim());
            form.setCategory(category.trim());
            form.setSkillBackground(skillBackground);
            form.setDescription(description);
            form.setCoverImg(fileStorageService.storeImage(coverImgFile));
            form.setImageList(storeImages(imageFiles));
            form.setModelUrl(fileStorageService.storeModel(modelFile));
        } catch (IllegalArgumentException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("work", heritageWorkService.findById(id).orElse(null));
            return "work/form";
        }
        HeritageWork updated = heritageWorkService.update(id, form, craftsman);
        if (updated == null) {
            return "redirect:/heritageWork/my";
        }
        return "redirect:/heritageWork/my";
    }

    // ============ 个人中心：我发布的展品 ============

    @GetMapping("/heritageWork/my")
    public String myWorks(HttpSession session, Model model) {
        if (!isCraftsman(session)) {
            return "redirect:/login";
        }
        User craftsman = (User) session.getAttribute("loginUser");
        model.addAttribute("works", heritageWorkService.listByCraftsman(craftsman.getId()));
        return "work/my-list";
    }

    // ============ 辅助方法 ============

    //角色兼容：DB tinyint('1') 与 字符串 'CRAFTSMAN' 两种表示都认作匠人
    private boolean isCraftsman(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return false;
        }
        String role = user.getRole();
        return "1".equals(role) || "CRAFTSMAN".equalsIgnoreCase(role);
    }

    private boolean isOwner(HeritageWork work, User user) {
        return user != null && work.getCraftsman() != null
                && user.getId().equals(work.getCraftsman().getId());
    }

    //把多个图片文件存盘，逗号拼接路径
    private String storeImages(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return null;
        }
        List<String> paths = new ArrayList<>();
        for (MultipartFile f : files) {
            String p = fileStorageService.storeImage(f);
            if (p != null) {
                paths.add(p);
            }
        }
        return paths.isEmpty() ? null : String.join(",", paths);
    }

    //逗号分隔的图片路径拆成列表供模板遍历
    private List<String> splitImages(String imageList) {
        if (imageList == null || imageList.isBlank()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(imageList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
