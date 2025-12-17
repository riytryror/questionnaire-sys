package com.whu.survey.service;

import com.whu.survey.controller.dto.QuestionStatsDTO;
import com.whu.survey.controller.dto.SurveySubmitDTO;
import com.whu.survey.entity.Survey;

import java.util.List;

public interface SurveyService {
    // 创建问卷（包含题目）
    void createSurvey(Survey survey);

    // 获取问卷详情（包含题目）
    Survey getSurveyDetail(Integer id);

    // 提交问卷
    void submitSurvey(SurveySubmitDTO submitDTO);

    //统计数据
    List<QuestionStatsDTO> getSurveyStats(Integer surveyId);

    //问卷列表
    List<Survey> getAllSurveys();

    //删除
    void deleteSurvey(Integer id);
}