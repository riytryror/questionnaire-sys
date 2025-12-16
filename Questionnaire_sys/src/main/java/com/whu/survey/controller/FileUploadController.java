package com.whu.survey.controller;

import com.whu.survey.utils.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class FileUploadController {

    @PostMapping("/audio")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            // 1. 获取原始文件名和后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 2. 生成新文件名 (防止重名)
            String newFileName = UUID.randomUUID().toString() + suffix;

            // 3. 确定存储路径
            // 这里为了简单，直接存到 webapp/uploads 目录下
            String realPath = request.getSession().getServletContext().getRealPath("/uploads/");
            File dir = new File(realPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 4. 保存文件
            file.transferTo(new File(dir, newFileName));

            // 5. 返回访问 URL
            // 返回的是相对路径，前端拼接 base URL
            String fileUrl = "/uploads/" + newFileName;
            return Result.success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}