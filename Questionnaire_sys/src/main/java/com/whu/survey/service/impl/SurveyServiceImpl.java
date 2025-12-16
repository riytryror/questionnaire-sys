package com.whu.survey.service.impl;

import com.whu.survey.entity.Question;
import com.whu.survey.entity.Survey;
import com.whu.survey.mapper.QuestionMapper;
import com.whu.survey.mapper.SurveyMapper;
import com.whu.survey.service.SurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}