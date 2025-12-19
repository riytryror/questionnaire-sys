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
        <div class="top-toolbar">
          <div class="toolbar-left">
            <el-input
              v-model="survey.title"
              placeholder="请输入问卷标题"
              class="main-title-input"
            />
          </div>
          <div class="toolbar-right">
            <el-button type="warning" plain icon="Setting" @click="settingVisible = true"
              >发布设置</el-button
            >
            <el-button type="primary" icon="Check" @click="saveSurvey">保存问卷</el-button>
          </div>
        </div>

        <div class="survey-desc-box">
          <el-input
            v-model="survey.description"
            type="textarea"
            :rows="2"
            placeholder="请输入问卷开篇欢迎语/描述..."
          />
        </div>

        <div v-if="survey.questions.length === 0" class="empty-tip">
          <el-empty description="请从左侧点击添加题目" />
        </div>

        <div v-else v-for="(q, index) in survey.questions" :key="index" class="question-card">
          <div class="card-header">
            <el-tag size="small" effect="dark">{{ getLabel(q.type) }}</el-tag>

            <div class="header-right">
              <el-button
                v-if="index > 0"
                type="warning"
                link
                size="small"
                @click="openLogicDialog(q, index)"
              >
                <el-icon><Connection /></el-icon> 逻辑
              </el-button>

              <el-button type="danger" link size="small" @click="removeQuestion(index)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
          </div>

          <div
            v-if="['SINGLE', 'MULTI', 'DROPDOWN', 'RANK'].includes(q.type)"
            class="options-design-area"
          >
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
              <el-tag type="warning" size="small"
                ><el-icon><Sort /></el-icon> 用户需要对以下选项进行排序</el-tag
              >
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
              <el-input
                v-model="q.title"
                placeholder="请输入打分题标题"
                class="title-input"
                size="large"
              />
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
              <el-input
                v-model="q.title"
                placeholder="请输入问题标题"
                class="title-input"
                size="large"
              />
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
                <el-input
                  v-model="q.config.placeholder"
                  size="small"
                  placeholder="例如：请输入您的姓名"
                  style="width: 200px"
                />
              </div>
            </div>
          </div>

          <div v-else class="other-design-area">
            <div class="question-title-row">
              <span class="q-index">{{ index + 1 }}.</span>
              <el-input
                v-model="q.title"
                placeholder="请输入题目标题"
                class="title-input"
                size="large"
              />
            </div>

            <div class="preview-box">
              <el-date-picker
                v-if="q.type === 'DATE'"
                type="date"
                placeholder="用户点击选择日期"
                style="width: 100%"
                readonly
              />

              <div v-if="['FILE', 'IMAGE', 'AUDIO', 'VIDEO'].includes(q.type)" class="fake-upload">
                <el-icon class="icon"><Upload /></el-icon>
                <span>点击上传 {{ getLabel(q.type) }} (最大10MB)</span>
              </div>

              <div v-if="q.type === 'SIGN'" class="fake-sign">此处为电子签名板区域</div>
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

<el-dialog v-model="logicDialogVisible" title="题目显示逻辑" width="500px">
  <el-alert
    title="当满足以下条件时，此题目才会显示。不设置则默认显示。"
    type="info"
    show-icon
    :closable="false"
    style="margin-bottom: 20px;"
  />

  <el-form label-width="100px">
    <el-form-item label="依赖题目">
      <el-select 
        v-model="currentLogic.targetQuestionIndex" 
        placeholder="请选择前置题目"
        style="width: 100%"
        @change="handleLogicQuestionChange"
        clearable
      >
        <el-option
          v-for="item in availablePreQuestions"
          :key="item.index"
          :label="`${item.index + 1}. ${item.title}`"
          :value="item.index"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="选中选项" v-if="currentLogic.targetQuestionIndex !== null">
      <el-select 
        v-model="currentLogic.targetOption" 
        placeholder="当用户选中此项时显示"
        style="width: 100%"
        clearable
      >
        <el-option
          v-for="opt in getOptionsByIndex(currentLogic.targetQuestionIndex)"
          :key="opt"
          :label="opt"
          :value="opt"
        />
      </el-select>
    </el-form-item>
  </el-form>

  <template #footer>
    <el-button @click="logicDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="confirmLogic">确定保存</el-button>
  </template>
</el-dialog>

    <el-drawer v-model="settingVisible" title="问卷发布设置" size="350px">
      <el-form label-position="top">
        <el-form-item label="问卷状态">
          <el-switch
            v-model="survey.status"
            :active-value="1"
            :inactive-value="0"
            active-text="发布中"
            inactive-text="暂停回收"
          />
        </el-form-item>

        <el-divider />

        <el-form-item label="有效时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateChange"
            style="width: 100%"
          />
          <div class="setting-tips">不设置则代表永久有效</div>
        </el-form-item>

        <el-divider />

        <el-form-item label="最大回收量限制">
          <el-input-number v-model="survey.maxLimit" :min="0" style="width: 100%" />
          <div class="setting-tips">设置为 0 表示不限制回收数量</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="flex: auto">
          <el-button @click="settingVisible = false">关闭</el-button>
          <el-button type="primary" @click="settingVisible = false">确认</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive,computed } from 'vue'
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
  Sort,
  Setting, 
  Connection,
} from '@element-plus/icons-vue'

const router = useRouter()

// === 新增：控制设置抽屉 ===
const settingVisible = ref(false)
const dateRange = ref([])
const dummyRate = ref(0) // 解决 el-rate 报错 warning

// 问卷数据模型
const survey = reactive({
  title: '未命名问卷',
  description: '感谢您能抽出几分钟时间来参加本次答题，现在我们就马上开始吧！',
  questions: [], //  确保初始化为空数组
  status: 1, // 1-发布, 0-暂停
  startTime: null,
  endTime: null,
  maxLimit: 0, // 0-不限制
})

// 处理日期变更
const handleDateChange = (val) => {
  if (val && val.length === 2) {
    survey.startTime = val[0]
    survey.endTime = val[1]
  } else {
    survey.startTime = null
    survey.endTime = null
  }
}


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
  switch (type) {
    case 'SINGLE':
      return '请输入单选题标题'
    case 'MULTI':
      return '请输入多选题标题'
    case 'DROPDOWN':
      return '请输入下拉菜单标题'
    case 'RANK':
      return '请输入排序题标题'
    default:
      return '请输入标题'
  }
}

// 获取题型名称
const getLabel = (type) => questionTypes.find((t) => t.value === type)?.label

// 添加题目
const addQuestion = (type) => {
  // 🔥 防御性检查：防止 questions 为 undefined
  if (!survey.questions) {
    survey.questions = []
  }

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
  // 1. 基础校验
  if (!survey.title) {
    ElMessage.warning('请输入问卷标题')
    return
  }
  if (!survey.questions || survey.questions.length === 0) {
    ElMessage.warning('请至少添加一道题目')
    return
  }

  try {
    // 2. 构造后端需要的 JSON 结构
    const payload = {
      title: survey.title,
      description: survey.description,

      status: survey.status,
      startTime: survey.startTime,
      endTime: survey.endTime,
      maxLimit: survey.maxLimit,

      questions: survey.questions.map((q) => ({
        title: q.title,
        type: q.type,
        required: q.required ? 1 : 0,
        config: q.config,
      })),
    }

    await createSurvey(payload)
    ElMessage.success('问卷创建成功！')
    router.push('/') // 返回首页
  } catch (e) {
    console.error(e)
  }
}


// 2. 定义逻辑控制的变量
const logicDialogVisible = ref(false)
const currentLogic = reactive({
  questionIndex: null,       // 当前正在设置哪道题
  targetQuestionIndex: null, // 依赖哪道题 (存索引，方便前端处理)
  targetOption: null         // 依赖哪个选项
})

// 计算属性：当前题目之前的所有题目 (只能依赖前面的题)
const availablePreQuestions = computed(() => {
  if (currentLogic.questionIndex === null) return []
  // 我只过滤出前面的单选题或下拉题，因为只有这些题适合做条件判断
  return survey.questions
    .map((q, i) => ({ ...q, index: i }))
    .filter(q => q.index < currentLogic.questionIndex)
    .filter(q => ['SINGLE', 'DROPDOWN'].includes(q.type))
})

// 打开逻辑弹窗
const openLogicDialog = (q, index) => {
  currentLogic.questionIndex = index
  
  // 如果之前设置过逻辑，我需要回显出来
  if (q.logic && q.logic.targetIndex !== undefined) {
    currentLogic.targetQuestionIndex = q.logic.targetIndex
    currentLogic.targetOption = q.logic.option
  } else {
    // 没设置过就重置
    currentLogic.targetQuestionIndex = null
    currentLogic.targetOption = null
  }
  
  logicDialogVisible.value = true
}

// 当用户改变了依赖题目时，我得清空选项，防止残留
const handleLogicQuestionChange = () => {
  currentLogic.targetOption = null
}

// 辅助方法：根据索引获取那道题的选项列表
const getOptionsByIndex = (index) => {
  if (index === null || index === undefined) return []
  const q = survey.questions[index]
  return q.config ? q.config.options : []
}

// 确认保存逻辑
const confirmLogic = () => {
  const q = survey.questions[currentLogic.questionIndex]
  
  // 如果用户选了题目和选项，我就把逻辑存进去
  if (currentLogic.targetQuestionIndex !== null && currentLogic.targetOption) {
    q.logic = {
      targetIndex: currentLogic.targetQuestionIndex,
      option: currentLogic.targetOption
    }
  } else {
    // 否则我就删掉逻辑字段，表示无条件显示
    delete q.logic
  }
  
  logicDialogVisible.value = false
  ElMessage.success('逻辑设置已保存')
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
  padding: 0; /* 🔥 修改 padding，因为顶部加了 toolbar */
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  height: 100%;
}

/* 🔥🔥🔥 顶部工具栏样式 🔥🔥🔥 */
.top-toolbar {
  background: #fff;
  padding: 15px 30px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky; /* 吸顶 */
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.toolbar-left {
  flex: 1;
  margin-right: 20px;
}
.main-title-input :deep(.el-input__wrapper) {
  box-shadow: none; /* 去掉边框 */
  font-size: 20px;
  font-weight: bold;
}
.survey-desc-box {
  padding: 20px 30px 0 30px;
}

.question-card {
  background: #fff;
  padding: 25px;
  margin: 20px 30px; /* 调整 margin */
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

.header-right{
  display: right;
  gap: 10px;
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
  margin-top: 10px;
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

/* 🔥 抽屉里的提示文字 */
.setting-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
