package com.whu.survey.controller;

import com.whu.survey.controller.dto.QuestionStatsDTO;
import com.whu.survey.controller.dto.SurveySubmitDTO;
import com.whu.survey.entity.Survey;
import com.whu.survey.service.SurveyService;
import com.whu.survey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // 提交问卷
    @PostMapping("/submit")
    public Result submit(@RequestBody SurveySubmitDTO submitDTO) {
        try {
            surveyService.submitSurvey(submitDTO);
            return Result.success("提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    // 获取统计数据接口
    @GetMapping("/{id}/stats")
    public Result<List<QuestionStatsDTO>> getStats(@PathVariable Integer id) {
        List<QuestionStatsDTO> list = surveyService.getSurveyStats(id);
        return Result.success(list);
    }

    // 获取所有问卷
    @GetMapping
    public Result<List<Survey>> list() {
        List<Survey> list = surveyService.getAllSurveys();
        return Result.success(list);
    }

    // 删除问卷
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        surveyService.deleteSurvey(id);
        return Result.success();
    }
}