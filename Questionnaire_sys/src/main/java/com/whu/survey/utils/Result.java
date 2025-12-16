package com.whu.survey.utils;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 200成功, 500失败
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.code = 500;
        result.msg = msg;
        return result;
    }
}