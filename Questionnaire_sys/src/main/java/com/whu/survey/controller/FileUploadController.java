package com.whu.survey.controller;

import com.whu.survey.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin // 配合之前的跨域设置
public class FileUploadController {

    // 定义文件保存的物理路径 (建议写在 application.yml 里，这里为了演示直接写死)
    // Windows 例子: "D:/survey_files/"
    private static final String UPLOAD_DIR = "D:/A_HomeWork/OSSDEV/lFinal/Questionnaire_sys/src/main/webapp/uploads/";

    // 定义文件访问的基础 URL (对应 WebMvcConfig 里的映射)
    private static final String URL_PREFIX = "/uploads/";

    @PostMapping("/file")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("空文件");

        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;

            // 确保目录存在
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File dest = new File(UPLOAD_DIR + fileName);
            file.transferTo(dest);

            // 返回相对路径，例如: /uploads/abc-123.mp3
            // 前端 SurveyStats.vue 会自动拼接 http://localhost:8080
            return Result.success(URL_PREFIX + fileName);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}