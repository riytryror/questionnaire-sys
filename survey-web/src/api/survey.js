import request from '@/utils/request'

// 1. 获取问卷列表
export function getSurveyList() {
  return request({
    url: '/api/surveys',
    method: 'get'
  })
}

// 2. 获取问卷详情
export function getSurveyDetail(id) {
  return request({
    url: `/api/surveys/${id}`,
    method: 'get'
  })
}

// 3. 创建问卷
export function createSurvey(data) {
  return request({
    url: '/api/surveys/save',
    method: 'post',
    data
  })
}

// 4. 删除问卷 (新增)
export function deleteSurvey(id) {
  return request({
    url: `/api/surveys/${id}`,
    method: 'delete'
  })
}

// 5. 提交问卷答案
export function submitSurvey(data) {
  return request({
    url: '/api/surveys/submit',
    method: 'post',
    data
  })
}

// 6. 获取统计数据
export function getSurveyStats(id) {
  return request({
    url: `/api/surveys/${id}/stats`,
    method: 'get'
  })
}

// 7. 导出 Excel
export function exportSurveyExcel(id) {
  return request({
    url: `/api/surveys/export/${id}`,
    method: 'get',
    responseType: 'blob',
    timeout: 60000
  })
}