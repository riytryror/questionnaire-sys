package com.whu.survey.controller.dto;

import lombok.Data;
import java.util.Date;

@Data
public class AnswerExportDTO {
    // 这几个字段来自 t_answer 表
    private Integer responseId;
    private Integer questionId;
    private String answerValue;

    // 这个字段来自 t_survey_response 表
    private Date createTime;
}