package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.service.HeritageWorkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final HeritageWorkService heritageWorkService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/tutorial/list")
    public String tutorialList() {
        return "tutorial/list";
    }

    @GetMapping("/custom/apply")
    public String customApply() {
        return "apply";
    }

    @GetMapping("/userWork/share")
    public String userWorkShare() {
        return "share";
    }

    @GetMapping("/user/userCenter")
    public String userUserCenter(HttpSession session, Model model) {
        Object value = session.getAttribute("loginUser");
        if (value instanceof User user) {
            model.addAttribute("user", user);
            if (user.getId() != null && isCraftsman(user)) {
                model.addAttribute("myWorks", heritageWorkService.findByCraftsman(user.getId()));
            }
        }
        return "user/userCenter";
    }

    private boolean isCraftsman(User user) {
        return "CRAFTSMAN".equalsIgnoreCase(user.getRole()) || "1".equals(user.getRole());
    }
}
