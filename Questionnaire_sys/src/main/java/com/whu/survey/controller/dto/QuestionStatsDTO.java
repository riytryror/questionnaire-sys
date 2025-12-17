package com.whu.survey.controller.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionStatsDTO {
    private Integer questionId;
    private String title;
    private String type; // SINGLE, AUDIO

    // 给 ECharts 饼图用的数据
    // 格式: [{"name": "喜欢", "value": 10}, {"name": "不喜欢", "value": 5}]
    private List<OptionCount> chartData = new ArrayList<>();

    // 给 音频列表 用的数据
    // 格式: ["/uploads/a.mp3", "/uploads/b.mp3"]
    private List<String> audioList = new ArrayList<>();

    // 内部静态类，定义饼图的一个切片
    @Data
    public static class OptionCount {
        private String name;
        private Integer value;

        public OptionCount(String name, Integer value) {
            this.name = name;
            this.value = value;
        }
    }
}