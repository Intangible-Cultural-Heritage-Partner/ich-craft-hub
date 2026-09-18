package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(){
        return "index";
    }

    //非遗展品列表页
    @GetMapping("/work/list")
    public String workList(){
        return "work/list";
    }

    //手作教程列表页
    @GetMapping("/tutorial/list")
    public String tutorialList(){
        return "tutorial/list";
    }

    //定制对接页面：已迁移到 CustomOrderController.applyForm()
    // /custom/apply 由 CustomOrderController 处理（同时列出可选匠人）

    //用户作品分享页
    @GetMapping("/userWork/share")
    public String userWorkShare(){
        return "userWork/share";
    }

    //用户中心：从 session 注入当前登录用户
    @GetMapping("/user/userCenter")
    public String userUserCenter(HttpSession session, Model model){
        User loginUser = (User) session.getAttribute("loginUser");
        model.addAttribute("user", loginUser);
        return "user/userCenter";
    }
}