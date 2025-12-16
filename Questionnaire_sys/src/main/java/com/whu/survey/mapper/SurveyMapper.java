package com.whu.survey.mapper;

import com.whu.survey.entity.Survey;

public interface SurveyMapper {
    // 插入问卷主表，并返回自增ID
    int insert(Survey survey);

    // 根据ID查问卷基本信息
    Survey selectById(Integer id);
}