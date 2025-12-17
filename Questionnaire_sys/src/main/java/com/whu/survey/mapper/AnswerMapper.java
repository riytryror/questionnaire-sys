package com.whu.survey.mapper;

import com.whu.survey.entity.Answer;
import com.whu.survey.entity.SurveyResponse;
import java.util.List;

public interface AnswerMapper {
    // 插入一次答卷记录 (返回主键ID)
    int insertResponse(SurveyResponse response);

    // 批量插入答案详情
    int insertAnswerList(List<Answer> list);

    // 通过ID查询所有答案，用于统计
    List<Answer> selectByQuestionId(Integer questionId);
}