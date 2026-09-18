package com.zjxy.intangible_heritage.controller;

import org.springframework.stereotype.Controller;
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

    //定制对接页面
    @GetMapping("/custom/apply")
    public String customApply(){
        return "apply";
    }

    //用户作品分享页
    @GetMapping("/userWork/share")
    public String userWorkShare(){
        return "share";
    }

    //用户中心
    @GetMapping("/user/userCenter")
    public String userUserCenter(){
        return "user/userCenter";
    }
}