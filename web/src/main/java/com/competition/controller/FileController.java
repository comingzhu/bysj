package com.competition.controller;

import com.competition.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {
    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String filePath = uploadPath + fileName;
            
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
            
            Map<String, Object> data = new HashMap<>();
            data.put("filePath", "/uploads/" + fileName);
            data.put("fileName", originalFilename);
            data.put("fileSize", file.getSize());
            return Result.success("上传成功", data);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/download")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @RequestParam String path) {
        try {
            // path格式可能是 /uploads/filename 或完整路径
            String filePath = path.startsWith("/uploads/") 
                ? uploadPath + path.substring("/uploads/".length())
                : path;
            
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            
            org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
            String fileName = file.getName();
            
            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8") + "\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

