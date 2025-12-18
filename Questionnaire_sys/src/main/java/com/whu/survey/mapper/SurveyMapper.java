package com.whu.survey.mapper;

import com.whu.survey.controller.dto.AnswerExportDTO;
import com.whu.survey.entity.Survey;

import java.util.List;

public interface SurveyMapper {
    // 插入问卷主表，并返回自增ID
    int insert(Survey survey);

    // 根据ID查问卷基本信息
    Survey selectById(Integer id);

    List<Survey> selectAll();
    int deleteById(Integer id);

    List<AnswerExportDTO> selectExportData(Integer surveyId);
}