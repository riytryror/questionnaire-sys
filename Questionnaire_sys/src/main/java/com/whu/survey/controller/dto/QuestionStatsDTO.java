package com.whu.survey.controller.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionStatsDTO {
    private Integer questionId;
    private String title;
    private String type;

    // 饼图数据 (单选/多选/打分)
    private List<OptionCount> chartData = new ArrayList<>();

    // 音频数据
    private List<String> audioList = new ArrayList<>();

    //  图片列表 (图片题、签名题用这个)
    private List<String> imageList = new ArrayList<>();

    //  文本/文件列表 (填空题、文件题用这个)
    private List<String> textList = new ArrayList<>();

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