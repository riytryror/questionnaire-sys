<template>
  <div class="design-container">
    <el-container style="height: 100vh">
      <el-aside width="260px" class="sidebar">
        <h3 class="sidebar-title">题型控件</h3>
        <div class="control-grid">
          <el-button
            v-for="type in questionTypes"
            :key="type.value"
            class="type-btn"
            @click="addQuestion(type.value)"
          >
            <el-icon><component :is="type.icon" /></el-icon>
            {{ type.label }}
          </el-button>
        </div>
      </el-aside>

      <el-main class="main-content">
        <div v-if="survey.questions.length === 0" class="empty-tip">
          <el-empty description="请从左侧点击添加题目" />
        </div>

        <div v-else v-for="(q, index) in survey.questions" :key="index" class="question-card">
          <div class="card-header">
            <el-tag size="small" effect="dark">{{ getLabel(q.type) }}</el-tag>
            <el-button type="danger" link @click="removeQuestion(index)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>

<div v-if="['SINGLE', 'MULTI', 'DROPDOWN', 'RANK'].includes(q.type)" class="options-design-area">
            
            <div class="question-title-row">
              <span class="q-index">{{ index + 1 }}.</span>
              <el-input
                v-model="q.title"
                :placeholder="getPlaceholder(q.type)"
                class="title-input"
                size="large"
              />
            </div>

            <div class="mini-preview" v-if="q.type === 'DROPDOWN'">
               <el-select placeholder="用户端将显示为下拉菜单" disabled style="width: 100%">
                  <el-option label="选项1" value="1" />
               </el-select>
            </div>
            <div class="mini-preview" v-if="q.type === 'RANK'">
               <el-tag type="warning" size="small"><el-icon><Sort /></el-icon> 用户需要对以下选项进行排序</el-tag>
            </div>

            <div class="option-list">
              <div v-for="(opt, i) in q.config.options" :key="i" class="option-row">
                
                <el-icon v-if="q.type === 'SINGLE'" class="option-icon"><CircleCheck /></el-icon>
                <el-icon v-else-if="q.type === 'MULTI'" class="option-icon"><FullScreen /></el-icon>
                <el-icon v-else-if="q.type === 'RANK'" class="option-icon"><Sort /></el-icon>
                <span v-else class="option-icon text-icon">{{ i + 1 }}.</span>

                <el-input v-model="q.config.options[i]" placeholder="选项内容" />

                <el-button type="danger" link @click="removeOption(q, i)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>

            <div class="option-actions">
              <el-button type="primary" link @click="addOption(q)">
                <el-icon><Plus /></el-icon> 增加选项
              </el-button>

              <el-divider direction="vertical" />

              <el-button type="primary" link @click="openBatchDialog(q)">
                <el-icon><DocumentCopy /></el-icon> 批量添加
              </el-button>
            </div>
          </div>

          <div v-else-if="q.type === 'RATING'" class="other-design-area">
             <div class="question-title-row">
               <span class="q-index">{{ index + 1 }}.</span>
               <el-input v-model="q.title" placeholder="请输入打分题标题" class="title-input" size="large"/>
             </div>
             
             <div class="preview-box">
                <el-rate v-model="dummyRate" :max="q.config.max || 5" disabled text-color="#ff9900" />
             </div>

             <div class="config-panel">
                <span class="label">最大分数:</span>
                <el-input-number v-model="q.config.max" :min="3" :max="10" size="small" />
             </div>
          </div>

          <div v-else-if="q.type === 'TEXT'" class="other-design-area">
             <div class="question-title-row">
               <span class="q-index">{{ index + 1 }}.</span>
               <el-input v-model="q.title" placeholder="请输入问题标题" class="title-input" size="large"/>
             </div>

             <div class="preview-box">
                <el-input 
                  readonly 
                  :placeholder="q.config.placeholder || '用户将在此输入...'" 
                  :rows="q.config.maxLines"
                  :type="q.config.maxLines > 1 ? 'textarea' : 'text'"
                />
             </div>

             <div class="config-panel">
                <div class="config-item">
                   <span class="label">输入框高度(行):</span>
                   <el-input-number v-model="q.config.maxLines" :min="1" :max="10" size="small" />
                </div>
                <div class="config-item">
                   <span class="label">提示文字:</span>
                   <el-input v-model="q.config.placeholder" size="small" placeholder="例如：请输入您的姓名" style="width: 200px;"/>
                </div>
             </div>
          </div>

          <div v-else class="other-design-area">
             <div class="question-title-row">
               <span class="q-index">{{ index + 1 }}.</span>
               <el-input v-model="q.title" placeholder="请输入题目标题" class="title-input" size="large"/>
             </div>

             <div class="preview-box">
                <el-date-picker v-if="q.type === 'DATE'" type="date" placeholder="用户点击选择日期" style="width:100%" readonly />
                
                <div v-if="['FILE','IMAGE','AUDIO','VIDEO'].includes(q.type)" class="fake-upload">
                   <el-icon class="icon"><Upload /></el-icon> 
                   <span>点击上传 {{ getLabel(q.type) }} (最大10MB)</span>
                </div>

                <div v-if="q.type === 'SIGN'" class="fake-sign">
                   此处为电子签名板区域
                </div>
             </div>
          </div>
        </div>

        <div class="footer-action" v-if="survey.questions.length > 0">
          <el-button type="primary" size="large" class="save-btn" @click="saveSurvey"
            >保存问卷</el-button
          >
        </div>
      </el-main>
    </el-container>

    <el-dialog v-model="batchDialogVisible" title="批量添加选项" width="400px">
      <el-input
        v-model="batchText"
        type="textarea"
        :rows="10"
        placeholder="每行代表一个选项&#10;选项1&#10;选项2&#10;选项3"
      />
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchAdd">确定添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createSurvey } from '@/api/survey'
import { ElMessage } from 'element-plus'
import {
  Check,
  Coin,
  VideoPlay,
  Microphone,
  Document,
  Calendar,
  EditPen,
  Star,
  List,
  Upload,
  Picture,
  Delete,
  Plus,
  DocumentCopy,
  CircleCheck,
  FullScreen,
  Sort
} from '@element-plus/icons-vue'

const router = useRouter()

// 问卷数据模型
const survey = reactive({
  title: '未命名问卷',
  description: '请填写描述',
  questions: [],
})

// 题型定义
const questionTypes = [
  { label: '单选题', value: 'SINGLE', icon: 'Check' },
  { label: '多选题', value: 'MULTI', icon: 'List' },
  { label: '下拉框', value: 'DROPDOWN', icon: 'Coin' },
  { label: '打分题', value: 'RATING', icon: 'Star' },
  { label: '文本题', value: 'TEXT', icon: 'Document' },
  { label: '日期题', value: 'DATE', icon: 'Calendar' },
  { label: '排序题', value: 'RANK', icon: 'List' },
  { label: '文件上传', value: 'FILE', icon: 'Upload' },
  { label: '图片上传', value: 'IMAGE', icon: 'Picture' },
  { label: '音频上传', value: 'AUDIO', icon: 'Microphone' },
  { label: '电子签名', value: 'SIGN', icon: 'EditPen' },
]

const getPlaceholder = (type) => {
  switch(type) {
    case 'SINGLE': return '请输入单选题标题'
    case 'MULTI': return '请输入多选题标题'
    case 'DROPDOWN': return '请输入下拉菜单标题'
    case 'RANK': return '请输入排序题标题'
    default: return '请输入标题'
  }
}

// 获取题型名称
const getLabel = (type) => questionTypes.find((t) => t.value === type)?.label

// 添加题目
const addQuestion = (type) => {
  let config = {}

  // 根据类型初始化默认配置
  switch (type) {
    case 'SINGLE':
    case 'MULTI':
    case 'DROPDOWN':
    case 'RANK':
      config = { options: ['选项1', '选项2'] }
      break
    case 'RATING':
      config = { max: 5 }
      break
    case 'TEXT':
      config = { placeholder: '请输入您的回答', maxLines: 1 }
      break
    // 其他题型默认空配置
  }

  survey.questions.push({
    title: '请输入问题标题',
    type: type,
    required: true,
    config: config,
  })
}

// 删除题目
const removeQuestion = (index) => {
  survey.questions.splice(index, 1)
}

// === 选项操作逻辑 ===

const addOption = (q) => {
  if (!q.config.options) q.config.options = []
  q.config.options.push('新选项')
}

const removeOption = (q, index) => {
  if (q.config.options.length <= 1) {
    ElMessage.warning('至少保留一个选项')
    return
  }
  q.config.options.splice(index, 1)
}

// === 批量添加逻辑 ===
const batchDialogVisible = ref(false)
const batchText = ref('')
const currentEditingQuestion = ref(null)

const openBatchDialog = (q) => {
  currentEditingQuestion.value = q
  batchText.value = ''
  batchDialogVisible.value = true
}

const confirmBatchAdd = () => {
  if (!batchText.value) return

  const lines = batchText.value.split('\n').filter((line) => line.trim() !== '')
  if (lines.length > 0 && currentEditingQuestion.value) {
    // 追加模式
    currentEditingQuestion.value.config.options.push(...lines)
  }
  batchDialogVisible.value = false
  ElMessage.success(`成功添加 ${lines.length} 个选项`)
}

// === 保存问卷 ===
const saveSurvey = async () => {
  if (survey.questions.length === 0) {
    ElMessage.warning('请至少添加一道题目')
    return
  }

  try {
    // 构造后端需要的 JSON 结构
    const payload = {
      title: survey.title,
      description: survey.description,
      questions: survey.questions.map((q) => ({
        title: q.title,
        type: q.type,
        required: q.required ? 1 : 0,
        config: q.config
      })),
    }

    await createSurvey(payload)
    ElMessage.success('问卷创建成功！')
    router.push('/') // 返回首页
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.design-container {
  height: 100vh;
  background: #f5f7fa;
}
.sidebar {
  background: #fff;
  border-right: 1px solid #eee;
  padding: 20px;
  overflow-y: auto;
}
.sidebar-title {
  font-size: 16px;
  margin-bottom: 20px;
  font-weight: bold;
  color: #333;
}
.control-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}
.type-btn {
  justify-content: flex-start;
  margin-left: 0 !important;
  width: 100%;
}

.main-content {
  padding: 30px;
  overflow-y: auto;
  height: 100%;
}
.question-card {
  background: #fff;
  padding: 25px;
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}
.question-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

/* 题目输入行样式 */
.question-title-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.q-index {
  font-weight: bold;
  font-size: 18px;
  margin-right: 15px;
  color: #409eff;
}
.title-input {
  font-weight: bold;
}

/* 选项区域样式 */
.options-design-area {
  margin-left: 35px;
}
.option-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.option-icon {
  margin-right: 10px;
  color: #909399;
  font-size: 18px;
  width: 20px;
  text-align: center;
}
.option-actions {
  margin-top: 15px;
  padding-left: 30px;
}

/* 其他题型占位符 */
.other-design-area {
  margin-left: 0;
}
.placeholder-box {
  background: #f9fafc;
  padding: 15px;
  border-radius: 4px;
  color: #909399;
  border: 1px dashed #dcdfe6;
  margin-left: 35px;
}

.footer-action {
  text-align: center;
  margin-top: 30px;
  padding-bottom: 50px;
}
.save-btn {
  width: 200px;
}

/* 预览盒子 */
.preview-box {
  margin-left: 35px;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px dashed #e0e0e0;
  border-radius: 4px;
  background: #fafafa;
}

/* 配置面板 (灰色背景) */
.config-panel {
  margin-left: 35px;
  background: #f5f7fa;
  padding: 10px 15px;
  border-radius: 4px;
  display: flex;
  gap: 20px;
  align-items: center;
  font-size: 13px;
  color: #606266;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  font-weight: bold;
}

/* 仿真上传按钮样式 */
.fake-upload {
  border: 1px solid #dcdfe6;
  background: white;
  color: #606266;
  padding: 8px 15px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: not-allowed;
}

/* 仿真签名板 */
.fake-sign {
  height: 80px;
  background: #f0f0f0;
  border: 1px solid #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

/* 微预览条 */
.mini-preview {
  margin-left: 35px;
  margin-bottom: 15px;
  opacity: 0.8;
}

/* 纯文字序号图标 */
.text-icon {
  font-size: 14px;
  font-weight: bold;
  line-height: 32px;
}
</style>
