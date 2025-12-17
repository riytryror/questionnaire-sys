package com.whu.survey.entity;

import lombok.Data;
import java.util.Date;

// 记录 who, when , what
@Data
public class SurveyResponse {
    private Integer id;
    private Integer surveyId;
    private Integer userId;    // 如果是匿名提交，此值为 null
    private Date submitTime;   // 提交时间
    private String userIp;     // 提交者IP
    private String userAgent;  // 浏览器信息
}