package com.zjxy.intangible_heritage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

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
