package com.whu.survey.service.impl;

import com.whu.survey.controller.dto.SurveySubmitDTO;
import com.whu.survey.controller.dto.QuestionStatsDTO;
import com.whu.survey.entity.Answer;
import com.whu.survey.entity.Question;
import com.whu.survey.entity.Survey;
import com.whu.survey.entity.SurveyResponse;
import com.whu.survey.mapper.AnswerMapper;
import com.whu.survey.mapper.QuestionMapper;
import com.whu.survey.mapper.SurveyMapper;
import com.whu.survey.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class SurveyServiceImpl implements SurveyService {

    @Autowired
    private SurveyMapper surveyMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    @Transactional // 开启事务：如果题目保存失败，问卷主表也会回滚
    public void createSurvey(Survey survey) {
        // 1. 先保存问卷主表
        survey.setStatus(1); // 默认为发布状态
        survey.setUserId(1); // 暂时写死用户ID，后续可从Session获取
        surveyMapper.insert(survey); // 这一步执行完，survey.getId()就有值了

        // 2. 循环保存题目
        if (survey.getQuestions() != null) {
            for (Question q : survey.getQuestions()) {
                q.setSurveyId(survey.getId()); // 关联刚才生成的问卷ID
                questionMapper.insert(q);
            }
        }
    }

    @Override
    public Survey getSurveyDetail(Integer id) {
        // 1. 查基本信息
        Survey survey = surveyMapper.selectById(id);
        if (survey != null) {
            // 2. 查关联的题目
            List<Question> questions = questionMapper.selectBySurveyId(id);
            survey.setQuestions(questions);
        }
        return survey;
    }

    @Autowired
    private AnswerMapper answerMapper;

    @Override
    @Transactional // 开启事务，保证原子性
    public void submitSurvey(SurveySubmitDTO dto) {
        // 1. 先保存答卷记录
        SurveyResponse response = new SurveyResponse();
        response.setSurveyId(dto.getSurveyId());
        response.setUserId(null); // 匿名填写，如果是登录用户需从Session取
        response.setUserIp("127.0.0.1"); // 简单写死，实际可从 Request 获取
        answerMapper.insertResponse(response); // 执行后 response.getId() 有值

        // 2. 转换并保存答案详情
        List<Answer> answerList = new ArrayList<>();
        for (SurveySubmitDTO.AnswerItem item : dto.getAnswers()) {
            Answer answer = new Answer();
            answer.setResponseId(response.getId()); // 关联刚才生成的ID
            answer.setQuestionId(item.getQuestionId());
            answer.setType(item.getType());
            answer.setAnswerValue(item.getValue());
            answerList.add(answer);
        }

        if (!answerList.isEmpty()) {
            answerMapper.insertAnswerList(answerList);
        }
    }

    //实现统计数据转换
    @Override
    public List<QuestionStatsDTO> getSurveyStats(Integer surveyId) {
        // 1. 查出该问卷下的所有题目
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);
        List<QuestionStatsDTO> statsList = new ArrayList<>();

        // 定义哪些类型属于“图表类”（客观题）
        List<String> chartTypes = Arrays.asList("SINGLE", "MULTI", "DROPDOWN", "RATING", "RANK");

        for (Question q : questions) {
            QuestionStatsDTO dto = new QuestionStatsDTO();
            dto.setQuestionId(q.getId());
            dto.setTitle(q.getTitle());
            dto.setType(q.getType());

            // 2. 查出这道题的所有答案
            List<Answer> answers = answerMapper.selectByQuestionId(q.getId());

            // 3. 根据类型分流处理

            // A. 图表类题目 (单选、多选、下拉、评分) -> 统计数量
            if (chartTypes.contains(q.getType())) {
                Map<String, Integer> countMap = new HashMap<>();

                for (Answer ans : answers) {
                    String val = ans.getAnswerValue();
                    if (val != null && !val.isEmpty()) {
                        // 特殊处理多选题：如果存的是 "A,B,C"，建议拆分统计
                        if ("MULTI".equals(q.getType())) {
                            String[] options = val.split(","); // 简单按逗号拆分
                            for (String opt : options) {
                                String cleanOpt = opt.trim();
                                countMap.put(cleanOpt, countMap.getOrDefault(cleanOpt, 0) + 1);
                            }
                        } else {
                            // 单选、下拉等直接统计
                            countMap.put(val, countMap.getOrDefault(val, 0) + 1);
                        }
                    }
                }

                // 转成 DTO
                for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                    dto.getChartData().add(
                            new QuestionStatsDTO.OptionCount(entry.getKey(), entry.getValue())
                    );
                }
            }

            // B. 音频题 -> 存入 audioList
            else if ("AUDIO".equals(q.getType())) {
                for (Answer ans : answers) {
                    if (ans.getAnswerValue() != null) {
                        dto.getAudioList().add(ans.getAnswerValue());
                    }
                }
            }

            // C. 图片/签名题 -> 存入 imageList (解决你刚才的问题)
            else if ("IMAGE".equals(q.getType()) || "SIGN".equals(q.getType())) {
                for (Answer ans : answers) {
                    if (ans.getAnswerValue() != null) {
                        dto.getImageList().add(ans.getAnswerValue());
                    }
                }
            }

            // D. 其他所有类型 (填空 TEXT, 文件 FILE, 视频 VIDEO) -> 存入 textList
            else {
                for (Answer ans : answers) {
                    if (ans.getAnswerValue() != null) {
                        dto.getTextList().add(ans.getAnswerValue());
                    }
                }
            }

            statsList.add(dto);
        }
        return statsList;
    }

    @Override
    public List<Survey> getAllSurveys() {
        return surveyMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSurvey(Integer id) {
        // 1. 先删除该问卷下的所有题目
        try {
            questionMapper.deleteBySurveyId(id);
        } catch (Exception e) {
            System.out.println("警告：删除关联题目失败，可能是Mapper未定义");
        }

        // 2. 再删除问卷本身
        surveyMapper.deleteById(id);
    }

}