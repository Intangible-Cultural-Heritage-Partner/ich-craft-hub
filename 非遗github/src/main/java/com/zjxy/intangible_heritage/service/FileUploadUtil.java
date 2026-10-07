package com.zjxy.intangible_heritage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 保存图片
     */
    public String save(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newName = UUID.randomUUID().toString().replace("-", "") + ext;

            String basePath = System.getProperty("user.dir") + "/" + uploadDir + "/" + subDir;
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(dir, newName);
            file.transferTo(dest);

            return "/uploads/" + subDir + "/" + newName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 保存视频
     */
    public String saveVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String originalName = file.getOriginalFilename();
            String ext = ".mp4";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newName = UUID.randomUUID().toString().replace("-", "") + ext;

            String basePath = System.getProperty("user.dir") + "/" + uploadDir + "/userwork/video";
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(dir, newName);
            file.transferTo(dest);

            return "/uploads/userwork/video/" + newName;
        } catch (IOException e) {
            throw new RuntimeException("视频上传失败", e);
        }
    }
}