package com.zjxy.intangible_heritage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 把本地上传目录映射为静态资源，浏览器可通过 /upload/** 访问上传的图片和3D模型。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebMvcConfig(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);
    }
}
