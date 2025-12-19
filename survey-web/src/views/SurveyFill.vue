<template>
  <div class="fill-container" v-loading="loading">
    <div v-if="survey" class="survey-header">
      <div class="header-content">
        <h1 class="title">{{ survey.title }}</h1>
        <div class="description">{{ survey.description }}</div>
      </div>
    </div>

    <div v-if="errorMsg" class="error-box">
      <el-empty :description="errorMsg">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>
    </div>

    <div v-else-if="survey" class="questions-list">
      <div
        v-for="(q, index) in survey.questions"
        :key="q.id"
        class="question-card"
        v-show="checkLogic(q)"
        :id="'q-' + q.id"
      >
        <div class="q-title">
          <span class="q-index">{{ getRealIndex(index) }}.</span>
          <span class="q-text">{{ q.title }}</span>
          <span v-if="q.required" class="required">*</span>
          <el-tag size="small" type="info" effect="plain" class="type-tag">
            {{ getLabel(q.type) }}
          </el-tag>
        </div>

        <div v-if="q.type === 'SINGLE'">
          <el-radio-group v-model="answers[q.id]" class="vertical-options">
            <el-radio
              v-for="opt in q.config.options"
              :key="opt"
              :label="opt"
              size="large"
              border
            >{{ opt }}</el-radio>
          </el-radio-group>
        </div>

        <div v-if="q.type === 'MULTI'">
          <el-checkbox-group v-model="answers[q.id]" class="vertical-options">
            <el-checkbox
              v-for="opt in q.config.options"
              :key="opt"
              :label="opt"
              size="large"
              border
            >{{ opt }}</el-checkbox>
          </el-checkbox-group>
        </div>

        <div v-if="q.type === 'DROPDOWN'">
          <el-select v-model="answers[q.id]" placeholder="请选择" style="width: 100%" size="large">
            <el-option v-for="opt in q.config.options" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </div>

        <div v-if="q.type === 'TEXT'">
          <el-input
            v-model="answers[q.id]"
            :type="q.config.maxLines > 1 ? 'textarea' : 'text'"
            :rows="q.config.maxLines"
            :placeholder="q.config.placeholder || '请输入您的回答'"
          />
        </div>

        <div v-if="q.type === 'RATING'" class="rate-box">
          <el-rate
            v-model="answers[q.id]"
            :max="q.config.max || 5"
            allow-half
            show-score
            size="large"
          />
        </div>

        <div v-if="q.type === 'DATE'">
          <el-date-picker
            v-model="answers[q.id]"
            type="date"
            placeholder="请选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            size="large"
          />
        </div>

        <div v-if="q.type === 'RANK'">
          <div v-for="opt in q.config.options" :key="opt" class="rank-item">
            <span class="rank-label">{{ opt }}</span>
            <el-input-number
              v-model="answers[q.id + '_' + opt]"
              :min="1"
              :max="q.config.options.length"
              size="small"
              controls-position="right"
            />
            <span class="rank-suffix">位</span>
          </div>
        </div>

        <div v-if="['FILE', 'IMAGE', 'AUDIO', 'VIDEO'].includes(q.type)">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :accept="getAccept(q.type)"
            :show-file-list="false"
            :on-success="(res) => handleUploadSuccess(q.id, res)"
            :before-upload="(file) => beforeUpload(file, q.type)"
            class="upload-demo"
          >
            <el-button type="primary" plain icon="Upload">
              点击上传{{ getLabel(q.type).replace('附件', '文件') }}
            </el-button>
          </el-upload>

          <div v-if="answers[q.id]" class="file-preview">
            <div class="preview-header">
              <el-icon color="#67C23A"><CircleCheckFilled /></el-icon>
              <span class="file-name">已上传成功</span>
              <el-button link type="danger" size="small" @click="answers[q.id] = null">
                删除
              </el-button>
            </div>

            <img v-if="q.type === 'IMAGE'" :src="answers[q.id]" class="preview-img" />

            <audio
              v-if="q.type === 'AUDIO'"
              controls
              :src="answers[q.id]"
              class="preview-media"
            ></audio>

            <video
              v-if="q.type === 'VIDEO'"
              controls
              :src="answers[q.id]"
              class="preview-media"
            ></video>

            <a
              v-if="q.type === 'FILE'"
              :href="answers[q.id]"
              target="_blank"
              class="file-link"
            >查看文件</a>
          </div>
        </div>

        <div v-if="q.type === 'SIGN'">
          <div class="sign-wrapper">
            <vue-esign
              :ref="(el) => setSignRef(el, q.id)"
              :width="800"
              :height="300"
              :isCrop="false"
              :lineWidth="4"
              lineColor="#000"
              bgColor="#f9f9f9"
            />
          </div>
          <div class="sign-actions">
            <el-button size="small" @click="handleResetSign(q.id)">重写</el-button>
            <el-button type="primary" size="small" @click="handleGenerateSign(q.id)">
              确认签名
            </el-button>
          </div>
          <div v-if="answers[q.id]" class="sign-preview">
            <img :src="answers[q.id]" />
            <div class="tip">签名已保存</div>
          </div>
        </div>
      </div>

      <div class="footer-action">
        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          @click="submit"
          :loading="submitting"
        >
          提交问卷
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 请确保以下 API 路径正确
import { getSurveyDetail, submitSurvey } from '@/api/survey'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, CircleCheckFilled } from '@element-plus/icons-vue'
import VueEsign from 'vue-esign' 

const route = useRoute()
const router = useRouter()

// 状态变量
const loading = ref(true)
const submitting = ref(false)
const errorMsg = ref('')
const survey = ref(null)
const answers = ref({}) // 存放答案
const signRefs = reactive({}) // 存放签名板实例

// 配置上传接口
const uploadUrl = '/api/upload/file'
const uploadHeaders = {
  Authorization: localStorage.getItem('token') || '' 
}

// 初始化
onMounted(async () => {
  const id = route.params.id
  if (!id) {
    errorMsg.value = '链接参数错误'
    loading.value = false
    return
  }

  try {
    const res = await getSurveyDetail(id)
    const data = res.data || res
    
    if (data.status === 0) {
      errorMsg.value = '该问卷已暂停回收'
    } else {
      survey.value = data
      initAnswers(data.questions)
    }
  } catch (e) {
    console.error(e)
    errorMsg.value = '问卷加载失败或不存在'
  } finally {
    loading.value = false
  }
})

// 初始化答案结构
const initAnswers = (questions) => {
  questions.forEach((q) => {
    if (q.type === 'MULTI') {
      answers.value[q.id] = []
    } else {
      answers.value[q.id] = null
    }
  })
}

// 逻辑显隐判断
const checkLogic = (q) => {
  if (!q.logic || !q.logic.targetIndex) return true
  const targetQ = survey.value.questions[q.logic.targetIndex]
  if (!targetQ) return true
  const targetVal = answers.value[targetQ.id]
  return targetVal === q.logic.option
}

// 辅助显示真实题号
const getRealIndex = (index) => index + 1

// 辅助获取题目类型标签
const getLabel = (type) => {
  const map = {
    SINGLE: '单选', MULTI: '多选', TEXT: '填空',
    DATE: '日期', RATING: '评分', RANK: '排序',
    FILE: '附件', IMAGE: '图片', AUDIO: '音频', VIDEO: '视频',
    SIGN: '签名',
  }
  return map[type] || ''
}

// 辅助获取上传文件类型限制
const getAccept = (type) => {
  switch (type) {
    case 'IMAGE': return 'image/*'
    case 'AUDIO': return 'audio/*'
    case 'VIDEO': return 'video/*'
    default: return '*'
  }
}

// 上传前校验
const beforeUpload = (file, type) => {
  // 大小限制 50MB
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB!')
    return false
  }

  // 格式限制
  if (type === 'IMAGE' && !file.type.startsWith('image/')) {
    ElMessage.error('请上传图片格式文件')
    return false
  }
  if (type === 'AUDIO' && !file.type.startsWith('audio/')) {
    ElMessage.error('请上传音频格式文件')
    return false
  }
  if (type === 'VIDEO' && !file.type.startsWith('video/')) {
    ElMessage.error('请上传视频格式文件')
    return false
  }
  
  return true
}

// 上传成功回调
const handleUploadSuccess = (qid, res) => {
  if (res.code === 200) {
    // 后端返回的是 /uploads/xxxx，直接存入答案
    answers.value[qid] = res.data 
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

// 签名板相关方法
const setSignRef = (el, qid) => {
  if (el) signRefs[qid] = el
}
const handleResetSign = (qid) => {
  signRefs[qid]?.reset()
  answers.value[qid] = null
}
const handleGenerateSign = (qid) => {
  signRefs[qid]?.generate().then((res) => {
    answers.value[qid] = res
    ElMessage.success('签名已确认')
  }).catch(() => {
    ElMessage.warning('请先书写签名')
  })
}

// 3. 提交逻辑
const submit = async () => {
  // A. 必填校验
  for (const q of survey.value.questions) {
    if (!checkLogic(q)) continue

    if (q.required) {
      let val = answers.value[q.id]
      
      // 排序题特殊校验
      if (q.type === 'RANK') {
         const hasValue = q.config.options.some(opt => answers.value[q.id + '_' + opt])
         if (hasValue) val = 'valid'
      }

      if (val === null || val === undefined || val === '' || (Array.isArray(val) && val.length === 0)) {
        ElMessage.warning(`第 ${getRealIndex(survey.value.questions.indexOf(q))} 题是必填项，请填写`)
        document.getElementById('q-' + q.id)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
        return
      }
    }
  }

  submitting.value = true

  try {
    // B. 数据组装
    const submitList = []

    survey.value.questions.forEach((q) => {
      // 1. 隐藏题目不提交
      if (!checkLogic(q)) return 

      let finalVal = answers.value[q.id]

      // 2. 格式转换
      if (q.type === 'MULTI' && Array.isArray(finalVal)) {
        finalVal = finalVal.join(',')
      } 
      else if (q.type === 'RANK') {
        const arr = []
        q.config.options.forEach((opt) => {
          const v = answers.value[q.id + '_' + opt]
          if (v !== undefined && v !== null) {
             arr.push(`${opt}:${v}`)
          }
        })
        finalVal = arr.join(',')
      }

      // 3. 加入提交列表
      if (finalVal !== null && finalVal !== undefined && String(finalVal).trim() !== '') {
        submitList.push({
          questionId: q.id,
          type: q.type,
          value: String(finalVal) // ⭐ 修改点：这里改成了 value 以匹配后端 DTO
        })
      }
    })

    console.log('📦 提交数据:', submitList)

    if (submitList.length === 0) {
        ElMessage.warning('没有填写任何有效内容')
        submitting.value = false
        return
    }

    // C. 发送请求
    await submitSurvey({
      surveyId: survey.value.id,
      answers: submitList,
    })

    ElMessageBox.alert('您的答卷已提交成功，感谢参与！', '提交成功', {
      confirmButtonText: '关闭页面',
      callback: () => router.push('/'),
    })
  } catch (error) {
    console.error(error)
    ElMessage.error(error.response?.data?.msg || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.fill-container {
  max-width: 640px;
  margin: 0 auto;
  background-color: #f5f7fa;
  min-height: 100vh;
  padding-bottom: 40px;
}

.survey-header {
  background: #ffffff;
  padding: 24px 20px;
  margin-bottom: 12px;
  border-bottom: 1px solid #eee;
}
.title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
}
.description {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.questions-list {
  padding: 0 12px;
}

.question-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.q-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: #2c3e50;
  line-height: 1.4;
}
.q-index {
  font-weight: bold;
  margin-right: 4px;
}
.required {
  color: #f56c6c;
  margin-left: 4px;
  vertical-align: middle;
}
.type-tag {
  margin-left: 8px;
  vertical-align: middle;
  transform: scale(0.9);
}

/* 选项样式 */
.vertical-options .el-radio,
.vertical-options .el-checkbox {
  display: flex;
  margin-right: 0;
  margin-bottom: 10px;
  width: 100%;
  height: auto;
  padding: 10px;
  border-radius: 4px;
  white-space: normal;
}

/* 排序题 */
.rank-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  background: #f9f9f9;
  padding: 8px;
  border-radius: 4px;
}
.rank-label {
  flex: 1;
  font-size: 14px;
}
.rank-suffix {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}

/* 上传预览区域 */
.file-preview {
  margin-top: 15px;
  background: #f0f9eb;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #e1f3d8;
}
.preview-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  color: #67C23A;
  font-size: 14px;
}
.preview-img {
  max-width: 100%;
  max-height: 200px;
  display: block;
  border-radius: 4px;
}
.preview-media {
  width: 100%;
  margin-top: 5px;
}
.file-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

/* 签名板 */
.sign-wrapper {
  border: 1px dashed #dcdfe6;
  background: #f9f9f9;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}
.sign-actions {
  text-align: right;
}
.sign-preview img {
  max-width: 100%;
  height: auto;
  margin-top: 10px;
  border: 1px solid #eee;
}

/* 底部操作 */
.footer-action {
  margin-top: 24px;
  padding: 0 20px;
}
.submit-btn {
  width: 100%;
  font-weight: bold;
  height: 44px;
  font-size: 16px;
}
.error-box {
  padding-top: 100px;
  text-align: center;
}
</style>