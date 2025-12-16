package com.whu.survey.mapper;

import com.whu.survey.entity.Question;
import java.util.List;

public interface QuestionMapper {
    // 插入题目
    int insert(Question question);

    // 根据问卷ID查题目列表
    List<Question> selectBySurveyId(Integer surveyId);
}