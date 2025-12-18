package com.whu.survey.controller;

import com.whu.survey.controller.dto.QuestionStatsDTO;
import com.whu.survey.controller.dto.SurveySubmitDTO;
import com.whu.survey.entity.Survey;
import com.whu.survey.mapper.QuestionMapper;
import com.whu.survey.mapper.SurveyMapper;
import com.whu.survey.service.SurveyService;
import com.whu.survey.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import com.alibaba.excel.EasyExcel;
import com.whu.survey.controller.dto.AnswerExportDTO;
import com.whu.survey.entity.Question;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/surveys")
@CrossOrigin // 允许跨域，方便Vue调用
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private SurveyMapper surveyMapper;

    @Autowired
    private QuestionMapper questionMapper;

    // 保存问卷
    @PostMapping("/save")
    public Result save(@RequestBody Survey survey) {
        try {
            surveyService.createSurvey(survey);
            return Result.success("创建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    // 获取问卷详情
    @GetMapping("/{id}")
    public Result<Survey> getDetail(@PathVariable Integer id) {
        Survey survey = surveyService.getSurveyDetail(id);
        return Result.success(survey);
    }

    // 提交问卷
    @PostMapping("/submit")
    public Result submit(@RequestBody SurveySubmitDTO submitDTO) {
        try {
            surveyService.submitSurvey(submitDTO);
            return Result.success("提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    // 获取统计数据接口
    @GetMapping("/{id}/stats")
    public Result<List<QuestionStatsDTO>> getStats(@PathVariable Integer id) {
        List<QuestionStatsDTO> list = surveyService.getSurveyStats(id);
        return Result.success(list);
    }

    // 获取所有问卷
    @GetMapping
    public Result<List<Survey>> list() {
        List<Survey> list = surveyService.getAllSurveys();
        return Result.success(list);
    }

    // 删除问卷
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        surveyService.deleteSurvey(id);
        return Result.success();
    }

    @GetMapping("/export/{id}")
    public void exportExcel(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        // 1. 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("问卷数据_" + id, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 2. 准备表头
        List<List<String>> head = new ArrayList<>();
        head.add(Collections.singletonList("提交序号"));
        head.add(Collections.singletonList("提交时间"));

        List<Question> questions = questionMapper.selectBySurveyId(id);
        for (Question q : questions) {
            head.add(Collections.singletonList(q.getTitle()));
        }

        // 3. 准备数据
        List<AnswerExportDTO> rawData = surveyMapper.selectExportData(id);
        Map<Integer, List<AnswerExportDTO>> groupedMap = rawData.stream()
                .collect(Collectors.groupingBy(AnswerExportDTO::getResponseId));

        List<List<Object>> data = new ArrayList<>();

        // 后端域名端口
        String domain = "http://localhost:8080";

        for (Map.Entry<Integer, List<AnswerExportDTO>> entry : groupedMap.entrySet()) {
            Integer responseId = entry.getKey();
            List<AnswerExportDTO> userAnswers = entry.getValue();

            List<Object> row = new ArrayList<>();
            row.add(responseId);
            // 判空处理，防止有些数据没有时间
            row.add(userAnswers.isEmpty() || userAnswers.get(0).getCreateTime() == null ? "" : userAnswers.get(0).getCreateTime());

            Map<Integer, String> answerMap = userAnswers.stream()
                    .collect(Collectors.toMap(
                            AnswerExportDTO::getQuestionId,
                            dto -> dto.getAnswerValue() == null ? "" : dto.getAnswerValue(),
                            (v1, v2) -> v1
                    ));

            for (Question q : questions) {
                String val = answerMap.get(q.getId());

                if (val != null) {
                    //：如果是文件路径（图片/音频/文档），补全链接
                    if (val.startsWith("/uploads/")) {
                        val = domain + val;
                    }

                    // 防爆截断（针对超长文本或异常数据）
                    // 设置 32000 为安全线
                    if (val.length() > 32000) {
                        val = val.substring(0, 32000) + "...(内容过长已截断)";
                    }
                } else {
                    val = "";
                }

                row.add(val);
            }
            data.add(row);
        }

        // 4. 写出 Excel
        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("答卷明细")
                .doWrite(data);
    }
}

