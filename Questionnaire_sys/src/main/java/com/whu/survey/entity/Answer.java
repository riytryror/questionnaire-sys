package com.whu.survey.entity;

import lombok.Data;

@Data
public class Answer {
    private Integer id;
    private Integer responseId; // 关联到 SurveyResponse 的主键
    private Integer questionId; // 关联到 Question 的主键
    private String type;        // 冗余字段：方便统计时区分题型 (SINGLE, AUDIO)
    private String answerValue; // 核心字段：存选项值("A") 或 文件路径("/uploads/x.mp3")
}