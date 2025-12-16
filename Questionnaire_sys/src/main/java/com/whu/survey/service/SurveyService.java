package com.whu.survey.service;

import com.whu.survey.entity.Survey;

public interface SurveyService {
    // 创建问卷（包含题目）
    void createSurvey(Survey survey);

    // 获取问卷详情（包含题目）
    Survey getSurveyDetail(Integer id);
}