<template>
    <div class="page-header">
      <el-button link @click="$router.push('/')">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>

  <div class="main-container">
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" size="30"><Loading /></el-icon>
      <p>问卷加载中...</p>
    </div>

    <el-card v-else-if="survey" class="survey-card">
      <template #header>
        <div class="header-content">
          <h1 class="survey-title">{{ survey.title }}</h1>
          <p class="survey-desc">{{ survey.description }}</p>
        </div>
      </template>

      <div v-for="(q, index) in survey.questions" :key="q.id" class="question-item">
        <h3>
          {{ index + 1 }}. {{ q.title }}
          <span v-if="q.required" style="color: red">*</span>
          <el-tag size="small" effect="plain" style="margin-left: 10px">{{
            getLabel(q.type)
          }}</el-tag>
        </h3>

        <el-radio-group v-if="q.type === 'SINGLE'" v-model="answers[q.id]">
          <el-radio
            v-for="opt in q.config.options"
            :key="opt"
            :label="opt"
            size="large"
            border
            class="block-radio"
            >{{ opt }}</el-radio
          >
        </el-radio-group>

        <el-checkbox-group v-if="q.type === 'MULTI'" v-model="answers[q.id]">
          <el-checkbox
            v-for="opt in q.config.options"
            :key="opt"
            :label="opt"
            size="large"
            border
            class="block-radio"
            >{{ opt }}</el-checkbox
          >
        </el-checkbox-group>

        <el-select
          v-if="q.type === 'DROPDOWN'"
          v-model="answers[q.id]"
          placeholder="请选择"
          size="large"
          style="width: 100%"
        >
          <el-option v-for="opt in q.config.options" :key="opt" :label="opt" :value="opt" />
        </el-select>

        <el-rate
          v-if="q.type === 'RATING'"
          v-model="answers[q.id]"
          :max="q.config.max || 5"
          allow-half
          show-score
        />

        <el-input
          v-if="q.type === 'TEXT'"
          v-model="answers[q.id]"
          :type="q.config.maxLines > 1 ? 'textarea' : 'text'"
          :rows="q.config.maxLines"
          :placeholder="q.config.placeholder || '请输入您的回答'"
        />

        <el-date-picker
          v-if="q.type === 'DATE'"
          v-model="answers[q.id]"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />

        <div v-if="q.type === 'RANK'">
          <div v-for="opt in q.config.options" :key="opt" style="margin-bottom: 10px">
            <span style="display: inline-block; width: 120px">{{ opt }}</span>
            <el-input-number v-model="answers[q.id + '_' + opt]" :min="1" size="small" />
            <span class="tip-text">(填写序号)</span>
          </div>
        </div>

        <div v-if="['FILE', 'IMAGE', 'VIDEO', 'AUDIO'].includes(q.type)">
          <el-upload
            action="/api/upload/file"
            :show-file-list="false"
            :on-success="(res) => handleUploadSuccess(q.id, res)"
            :before-upload="(file) => beforeUpload(file, q.type)"
            :accept="getAcceptType(q.type)"
            class="upload-demo"
          >
            <el-button type="primary" plain>
              <el-icon><Upload /></el-icon> 点击上传 {{ getLabel(q.type) }}
            </el-button>
          </el-upload>

          <div v-if="answers[q.id]" class="preview-box">
            <el-icon style="color: #67c23a"><Check /></el-icon>
            已上传成功
            <img
              v-if="q.type === 'IMAGE'"
              :src="'http://localhost:8080' + answers[q.id]"
              class="preview-img"
            />
            <audio
              v-if="q.type === 'AUDIO'"
              controls
              :src="'http://localhost:8080' + answers[q.id]"
              class="preview-audio"
            ></audio>
          </div>
        </div>

        <div v-if="q.type === 'SIGN'">
          <div class="sign-board">
            <vue-esign
              :ref="(el) => setSignRef(el, q.id)"
              :width="800"
              :height="300"
              :isCrop="false"
              :lineWidth="6"
              lineColor="#000000"
              bgColor="#ffffff"
            />
          </div>
          <div style="margin-top: 10px">
            <el-button type="primary" size="small" @click="handleGenerateSign(q.id)"
              >确认使用签名</el-button
            >
            <el-button size="small" @click="handleResetSign(q.id)">重写</el-button>
          </div>
          <div v-if="answers[q.id]" style="margin-top: 10px; border: 1px dashed #ccc; padding: 5px">
            <p style="font-size: 12px; color: #999">生成的签名预览：</p>
            <img :src="answers[q.id]" style="max-width: 100%; height: auto" />
          </div>
        </div>
      </div>

      <div class="footer">
        <el-button
          type="primary"
          size="large"
          @click="submit"
          :loading="submitting"
          style="width: 200px"
        >
          提交问卷
        </el-button>
      </div>
    </el-card>
  </div>

</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getSurveyDetail, submitSurvey } from '@/api/survey'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Upload, Check } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(true)
const submitting = ref(false)
const survey = ref(null)
const answers = ref({})
const signRefs = reactive({}) // 用于存储签名板实例的 Map

// 1. 初始化数据
onMounted(async () => {
  try {
    const id = route.params.id
    if (!id) return

    const data = await getSurveyDetail(id)
    survey.value = data

    // 初始化答案对象
    data.questions.forEach((q) => {
      // ⚠️ 关键：多选题必须初始化为数组
      if (q.type === 'MULTI') {
        answers.value[q.id] = []
      } else {
        answers.value[q.id] = ''
      }
    })
  } catch (e) {
    console.error(e)
    ElMessage.error('问卷加载失败')
  } finally {
    loading.value = false
  }
})

// === 辅助逻辑 ===

// 题型名称映射
const getLabel = (type) => {
  const map = {
    SINGLE: '单选题',
    MULTI: '多选题',
    DROPDOWN: '下拉框',
    RATING: '评分题',
    TEXT: '文本题',
    DATE: '日期',
    RANK: '排序题',
    FILE: '文件',
    IMAGE: '图片',
    VIDEO: '视频',
    AUDIO: '音频',
    SIGN: '签名',
  }
  return map[type] || '题目'
}

// 限制文件类型
const getAcceptType = (type) => {
  switch (type) {
    case 'IMAGE':
      return 'image/*' // 只允许图片
    case 'AUDIO':
      return 'audio/*' // 只允许音频
    case 'VIDEO':
      return 'video/*' // 只允许视频
    case 'FILE':
      return '*' // 允许所有
    case 'SIGN':
      return 'image/*' // 签名本质是图片
    default:
      return '*'
  }
}

// 映射上传接口路径 (api/upload/audio, api/upload/image 等)
const uploadTypeMap = (type) => {
  if (['IMAGE', 'VIDEO', 'AUDIO'].includes(type)) return type.toLowerCase()
  return 'file' // 默认走普通文件接口
}

// === 事件处理 ===

// 上传成功回调
const handleUploadSuccess = (qid, response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    answers.value[qid] = response.data // 存入路径
  } else {
    ElMessage.error('上传失败: ' + response.msg)
  }
}

// 上传前校验 (可选)
const beforeUpload = (file, type) => {
  // 可以在这里限制大小，比如 10MB
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过 50MB!')
  }
  return isLt50M
}

// === 签名板逻辑 ===

// 动态收集 ref
const setSignRef = (el, qid) => {
  if (el) {
    signRefs[qid] = el
  }
}

// 生成签名图片
const handleGenerateSign = (qid) => {
  const esignInstance = signRefs[qid]
  if (!esignInstance) return

  esignInstance
    .generate()
    .then((res) => {
      answers.value[qid] = res // res 是 Base64 字符串
      ElMessage.success('签名已生成')
    })
    .catch((err) => {
      ElMessage.warning('请先完成签名')
    })
}

// 重置签名
const handleResetSign = (qid) => {
  const esignInstance = signRefs[qid]
  if (esignInstance) {
    esignInstance.reset()
    answers.value[qid] = '' // 清空答案
  }
}

// === 提交逻辑 ===

const submit = async () => {
  console.log('提交前原始数据:', answers.value)
  submitting.value = true

  try {
    const submitData = {
      surveyId: survey.value.id,
      answers: [],
    }

    // 遍历题目来构造答案，避免把 RANK 的临时 key (101_选项A) 当作题目ID
    survey.value.questions.forEach((q) => {
      let finalValue = answers.value[q.id]

      // 处理多选题：数组 -> 字符串
      if (q.type === 'MULTI' && Array.isArray(finalValue)) {
        finalValue = finalValue.join(',')
      }

      // 处理排序题：把散落在 answers 里的 key 聚合起来
      // 格式变成: "选项A:1,选项B:2"
      if (q.type === 'RANK') {
        const rankArr = []
        q.config.options.forEach((opt) => {
          const rankVal = answers.value[q.id + '_' + opt]
          if (rankVal) rankArr.push(`${opt}:${rankVal}`)
        })
        finalValue = rankArr.join(',')
      }

      // 推入结果
      submitData.answers.push({
        questionId: q.id,
        type: q.type,
        value: finalValue ? String(finalValue) : '', // 确保转字符串
      })
    })

    console.log('最终构造的数据:', submitData)

    await submitSurvey(submitData)
    ElMessage.success('提交成功！')

    setTimeout(() => {
      location.reload()
    }, 1500)
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-header {
  max-width: 800px;
  margin: 0 auto 20px; 
}
.main-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 10px;
}
.loading-state {
  text-align: center;
  padding: 50px;
  color: #909399;
}
.survey-title {
  text-align: center;
  color: #303133;
  margin-bottom: 10px;
}
.survey-desc {
  text-align: center;
  color: #606266;
  margin-bottom: 30px;
}
.question-item {
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
.question-item h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}
.block-radio {
  width: 100%;
  margin-left: 0 !important;
  margin-bottom: 10px;
}
.upload-demo {
  margin-bottom: 10px;
}
.preview-box {
  margin-top: 10px;
  padding: 10px;
  background: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
}
.preview-img {
  max-width: 200px;
  display: block;
  margin-top: 5px;
  border-radius: 4px;
}
.preview-audio {
  width: 100%;
  margin-top: 5px;
}
.sign-board {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}
.footer {
  text-align: center;
  margin-top: 40px;
  padding-bottom: 40px;
}
.tip-text {
  color: #999;
  font-size: 12px;
  margin-left: 5px;
}
</style>
