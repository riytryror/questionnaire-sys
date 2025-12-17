import request from '@/utils/request'

// 1. 获取问卷详情
export function getSurveyDetail(id) {
  return request({
    url: `/api/surveys/${id}`,
    method: 'get'
  })
}

// 2. 提交问卷
export function submitSurvey(data) {
  return request({
    url: '/api/surveys/submit',
    method: 'post',
    data
  })
}

// 3. 创建问卷 (保存)
export function createSurvey(data) {
  return request({
    url: '/api/surveys/save',
    method: 'post',
    data
  })
}

// 4. 获取问卷列表
export function getSurveyList() {
  return request({
    url: '/api/surveys',
    method: 'get'
  })
}

// 5. 删除问卷
export function deleteSurvey(id) {
  return request({
    url: `/api/surveys/${id}`,
    method: 'delete'
  })
}