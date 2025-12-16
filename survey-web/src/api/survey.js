import request from '@/utils/request'

// 获取问卷详情
export function getSurveyDetail(id){
    return request({
        url: `/api/surveys/${id}`,
        method: 'get'
    })
}


//提交问卷
export function submitSurvey(data){
    return request({
        url: '/api/surveys/submit',
        method: 'post',
        data: data
    })
}

//上传音频文件
export const uploadAudioUrl = 'http://localhost:8080/api/upload/audio'