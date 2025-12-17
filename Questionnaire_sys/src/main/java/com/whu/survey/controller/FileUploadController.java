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
    // Mac/Linux 例子: "/Users/yourname/survey_files/"
    private static final String UPLOAD_DIR = "D:/survey_files/";

    // 定义文件访问的基础 URL (对应 WebMvcConfig 里的映射)
    private static final String BASE_URL = "http://localhost:8080/files/";

    @PostMapping("/file")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        try {
            // 1. 获取原始文件名
            String originalFilename = file.getOriginalFilename();

            // 2. 获取后缀名 (例如 .jpg, .mp3)
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 3. 生成唯一文件名 (防止同名覆盖)
            String fileName = UUID.randomUUID().toString() + suffix;

            // 4. 创建保存目录 (如果不存在)
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 5. 保存文件到硬盘
            File dest = new File(UPLOAD_DIR + fileName);
            file.transferTo(dest);

            // 6. 返回可访问的 URL
            // 结果类似: http://localhost:8080/files/abcd-1234.jpg
            String fileUrl = BASE_URL + fileName;

            return Result.success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}