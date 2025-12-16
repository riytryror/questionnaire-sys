package com.whu.survey.controller;

import com.whu.survey.entity.Survey;
import com.whu.survey.service.SurveyService;
import com.whu.survey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/surveys")
@CrossOrigin // 允许跨域，方便Vue调用
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    // 保存问卷
    @PostMapping("/save")
    public Result save(@RequestBody Survey survey) {
        try {
            surveyService.createSurvey(survey);
            return Result.success("创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    // 获取问卷详情
    @GetMapping("/{id}")
    public Result<Survey> getDetail(@PathVariable Integer id) {
        Survey survey = surveyService.getSurveyDetail(id);
        return Result.success(survey);
    }
}