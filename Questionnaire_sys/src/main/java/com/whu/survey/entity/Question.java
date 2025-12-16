package com.whu.survey.entity;

import lombok.Data;
import java.util.Map;

@Data
public class Question {
    private Integer id;
    private Integer surveyId;
    private String type;      // SINGLE, MULTI, AUDIO
    private String title;
    private Integer required; // 1必填 0非必填
    private Integer orderNo;

    // ⭐ 重点：配合 JsonTypeHandler 使用
    // 数据库是 JSON 字符串，这里直接用 Map 接收
    private Map<String, Object> config;
}