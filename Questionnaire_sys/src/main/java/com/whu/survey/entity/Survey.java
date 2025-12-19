package com.whu.survey.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class Survey {
    private Integer id;
    private Integer userId; // 暂时可以写死或者先不管
    private String title;
    private String description;
    private Integer status; // 0草稿 1发布
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private Integer maxLimit;

    // 业务扩展字段：用于查询问卷详情时，直接带出下面的题目列表
    // 数据库表里没有这个字段，需要 MyBatis 关联查询
    private List<Question> questions;
}