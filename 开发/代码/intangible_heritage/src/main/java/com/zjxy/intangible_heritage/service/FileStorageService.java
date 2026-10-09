package com.zjxy.intangible_heritage.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 本地文件上传存储服务。
 * 上传文件保存到磁盘目录（app.upload.dir），通过 /upload/** 静态映射对外访问。
 */
@Service
public class FileStorageService {

    //允许的图片后缀
    private static final List<String> IMAGE_EXT = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
    //允许的3D模型后缀
    private static final List<String> MODEL_EXT = Arrays.asList("glb", "gltf");

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("无法创建上传目录: " + uploadRoot, e);
        }
    }

    /** 保存图片文件，返回可访问路径（/upload/xxx.png）；空文件返回 null。 */
    public String storeImage(MultipartFile file) {
        return store(file, IMAGE_EXT, "不支持的图片格式，仅允许 jpg/png/webp/gif");
    }

    /** 保存3D模型文件，返回可访问路径；空文件返回 null。 */
    public String storeModel(MultipartFile file) {
        return store(file, MODEL_EXT, "不支持的模型格式，仅允许 glb/gltf");
    }

    private String store(MultipartFile file, List<String> allowExt, String errMsg) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        //只取原始文件名后缀，用 UUID 重命名，杜绝路径穿越
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = StringUtils.getFilenameExtension(original);
        if (ext == null || !allowExt.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException(errMsg);
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext.toLowerCase();
        Path target = uploadRoot.resolve(storedName).normalize();
        //二次防御：目标必须仍在上传根目录下
        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("非法的文件路径");
        }
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("文件保存失败: " + original, e);
        }
        return "/upload/" + storedName;
    }
}
