package com.whu.survey.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis 自定义类型处理器
 * 用于将 Java对象 自动转换为 JSON字符串 存入数据库
 * 以及将 数据库JSON字符串 自动转回 Java对象
 */
public class JsonTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        try {
            // Java对象 -> JSON字符串
            ps.setString(i, mapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JsonTypeHandler 序列化失败", e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 数据库JSON -> Java对象
        return parse(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private Object parse(String json) {
        if (json == null || json.length() == 0) return null;
        try {
            // 默认转为 Map 或 List，方便直接使用
            return mapper.readValue(json, Object.class);
        } catch (IOException e) {
            throw new RuntimeException("JsonTypeHandler 反序列化失败", e);
        }
    }
}