package com.zjxy.intangible_heritage.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.UrlResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebConfigTest {
    @TempDir
    Path workingDirectory;

    @Test
    void servesFilesUploadedAfterStartupWhenUploadDirectoryDidNotExist() throws Exception {
        verifyUploadLocation();
    }

    @Test
    void servesFilesWhenUploadDirectoryAlreadyExists() throws Exception {
        Files.createDirectories(workingDirectory.resolve("uploads"));
        verifyUploadLocation();
    }

    private void verifyUploadLocation() throws Exception {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration uploads = mock(ResourceHandlerRegistration.class);
        ResourceHandlerRegistration images = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(uploads);
        when(registry.addResourceHandler("/images/**")).thenReturn(images);
        when(uploads.addResourceLocations(anyString())).thenReturn(uploads);
        when(images.addResourceLocations(anyString())).thenReturn(images);
        String originalDirectory = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", workingDirectory.toString());
            new WebConfig().addResourceHandlers(registry);
        } finally {
            System.setProperty("user.dir", originalDirectory);
        }
        ArgumentCaptor<String> location = ArgumentCaptor.forClass(String.class);
        verify(uploads).addResourceLocations(location.capture());
        Path uploadedFile = workingDirectory.resolve("uploads/heritage/example.jpg");
        Files.createDirectories(uploadedFile.getParent());
        Files.write(uploadedFile, new byte[]{1, 2, 3});
        assertTrue(new UrlResource(location.getValue()).createRelative("heritage/example.jpg").exists(),
                "Upload resource location must resolve files inside uploads, including after first upload");
    }
}
