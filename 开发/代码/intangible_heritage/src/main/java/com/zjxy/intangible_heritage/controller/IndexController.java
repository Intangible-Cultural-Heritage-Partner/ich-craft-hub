package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.Favorite;
import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.Tutorial;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.entity.UserWork;
import com.zjxy.intangible_heritage.repository.UserRepository;
import com.zjxy.intangible_heritage.service.FavoriteService;
import com.zjxy.intangible_heritage.service.HeritageWorkService;
import com.zjxy.intangible_heritage.service.TutorialService;
import com.zjxy.intangible_heritage.service.UserWorkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final HeritageWorkService heritageWorkService;
    private final UserRepository userRepository;
    private final FavoriteService favoriteService;
    private final TutorialService tutorialService;
    private final UserWorkService userWorkService;

    @GetMapping("/")
    public String index(Model model) {
        List<HeritageWork> allWorks = heritageWorkService.findAll();
        model.addAttribute("latestWorks", allWorks.size() > 4 ? allWorks.subList(0, 4) : allWorks);
        List<Tutorial> allTutorials = tutorialService.findAll();
        model.addAttribute("latestTutorials", allTutorials.size() > 4 ? allTutorials.subList(0, 4) : allTutorials);
        List<UserWork> allUserWorks = userWorkService.findAllPassed();
        model.addAttribute("latestUserWorks", allUserWorks.size() > 4 ? allUserWorks.subList(0, 4) : allUserWorks);
        return "index";
    }

    //定制对接页面：已迁移到 CustomOrderController.applyForm()
    // /custom/apply 由 CustomOrderController 处理（同时列出可选匠人）

    @GetMapping("/user/userCenter")
    public String userUserCenter(HttpSession session, Model model) {
        Object value = session.getAttribute("loginUser");
        if (value instanceof User user) {
            model.addAttribute("user", user);
            if (user.getId() != null && isCraftsman(user)) {
                model.addAttribute("myWorks", heritageWorkService.findByCraftsman(user.getId()));
            }
            if (user.getId() != null) {
                List<Favorite> workFavs = favoriteService.findByUserAndType(user.getId(), "work");
                List<HeritageWork> favWorks = new ArrayList<>();
                for (Favorite f : workFavs) {
                    heritageWorkService.findById(f.getTargetId()).ifPresent(favWorks::add);
                }
                model.addAttribute("favWorks", favWorks);

                List<Favorite> tutorialFavs = favoriteService.findByUserAndType(user.getId(), "tutorial");
                List<Tutorial> favTutorials = new ArrayList<>();
                for (Favorite f : tutorialFavs) {
                    tutorialService.findById(f.getTargetId()).ifPresent(favTutorials::add);
                }
                model.addAttribute("favTutorials", favTutorials);

                model.addAttribute("likedWorks", userWorkService.findLikedWorks(user.getId()));
            }
        }
        return "user/userCenter";
    }

    @PostMapping("/favorite/toggle")
    public String toggleFavorite(@RequestParam Long targetId,
                                 @RequestParam String targetType,
                                 @RequestParam(required = false) String redirect,
                                 HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (favoriteService.isFavorited(loginUser.getId(), targetId, targetType)) {
            favoriteService.unfavorite(loginUser.getId(), targetId, targetType);
        } else {
            favoriteService.favorite(loginUser.getId(), targetId, targetType);
        }
        if (redirect != null && !redirect.isBlank()) return "redirect:" + redirect;
        return "redirect:/";
    }

    private boolean isCraftsman(User user) {
        return "CRAFTSMAN".equalsIgnoreCase(user.getRole()) || "1".equals(user.getRole());
    }

    @PostMapping("/user/updateProfile")
    public String updateProfile(@RequestParam(required = false) MultipartFile avatarFile,
                                @RequestParam String username,
                                @RequestParam String phone,
                                @RequestParam(required = false) String password,
                                @RequestParam(required = false) String intro,
                                HttpSession session,
                                Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        User user = userRepository.findById(loginUser.getId()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (phone == null || !phone.matches("\\d{11}")) {
            model.addAttribute("profileMsg", "手机号必须为11位数字");
            return "redirect:/user/userCenter";
        }
        if (userRepository.existsByUsername(username) && !username.equals(user.getUsername())) {
            model.addAttribute("profileMsg", "用户名已被使用");
            return "redirect:/user/userCenter";
        }
        if (userRepository.existsByPhone(phone) && !phone.equals(user.getPhone())) {
            model.addAttribute("profileMsg", "该手机号已被注册");
            return "redirect:/user/userCenter";
        }

        user.setUsername(username);
        user.setPhone(phone);
        if (intro != null) {
            user.setIntro(intro.trim().isEmpty() ? null : intro.trim());
        }
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "avatar");
                Files.createDirectories(uploadDir);
                String originalName = avatarFile.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.lastIndexOf('.') >= 0) {
                    ext = originalName.substring(originalName.lastIndexOf('.'));
                }
                String fileName = UUID.randomUUID() + ext;
                Files.copy(avatarFile.getInputStream(), uploadDir.resolve(fileName));
                user.setAvatar("/uploads/avatar/" + fileName);
            } catch (IOException e) {
                model.addAttribute("profileMsg", "头像上传失败");
                return "redirect:/user/userCenter";
            }
        }

        userRepository.save(user);
        session.setAttribute("loginUser", user);
        model.addAttribute("profileMsg", "修改成功");
        return "redirect:/user/userCenter";
    }
}