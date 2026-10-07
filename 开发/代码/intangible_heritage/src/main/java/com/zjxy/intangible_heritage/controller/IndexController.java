package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.service.InteractionService;
import com.zjxy.intangible_heritage.service.UserWorkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final UserWorkService userWorkService;
    private final InteractionService interactionService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/work/list")
    public String workList() {
        return "work/list";
    }

    @GetMapping("/tutorial/list")
    public String tutorialList() {
        return "tutorial/list";
    }

    @GetMapping("/custom/apply")
    public String customApply() {
        return "apply";
    }

    @GetMapping("/user/userCenter")
    public String userUserCenter(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loginUser);

        int worksCount = userWorkService.listByUser(loginUser.getId()).size();
        int likesCount = interactionService.listLikesByUser(loginUser.getId(), 3).size();
        int commentsCount = interactionService.listMyComments(loginUser.getId()).size();

        model.addAttribute("myWorksCount", worksCount);
        model.addAttribute("myLikesCount", likesCount);
        model.addAttribute("myCommentsCount", commentsCount);

        return "user/userCenter";
    }
}