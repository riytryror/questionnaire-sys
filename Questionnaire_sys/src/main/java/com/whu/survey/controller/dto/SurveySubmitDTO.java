package com.whu.survey.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class SurveySubmitDTO {
    private Integer surveyId;
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem{
        private Integer questionId;
        private String type;
        private String value;
    }
}
