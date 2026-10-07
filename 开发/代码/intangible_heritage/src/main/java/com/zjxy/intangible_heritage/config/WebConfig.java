package com.zjxy.intangible_heritage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截 /custom/** 与 /admin/**：
        //   - 未登录 → 跳 /login
        //   - /custom/apply、/custom/myApply 仅 USER
        //   - /custom/myCraft 仅 CRAFTSMAN
        //   - /admin/** 仅 ADMIN
        //   - 其它路径（chat/messages/accept/reject/finish）只做登录校验，
        //     "是否当事人"由 service 层细校验
        registry.addInterceptor(new RoleInterceptor())
                .addPathPatterns("/custom/**", "/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 用户上传的图片（项目根目录下的 uploads 文件夹）
        String uploadLocation = Paths.get(System.getProperty("user.dir"), "uploads")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);

        // 项目内置的默认头像等图片资源（src/main/resources/images/）
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/images/");
    }
}
