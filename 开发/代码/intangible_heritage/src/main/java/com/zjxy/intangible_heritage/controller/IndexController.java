package com.zjxy.intangible_heritage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(){
        return "index";
    }

    //非遗展品列表页 /work/list 由 HeritageWorkController 接管（带数据），此处不再重复映射

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