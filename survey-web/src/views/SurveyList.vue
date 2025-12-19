<template>
  <div class="list-container">
    <div class="header">
      <div class="left-title">
        <h2>问卷管理系统</h2>
        <span class="subtitle">共 {{ surveyList.length }} 份问卷</span>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/design')">
        <el-icon style="margin-right: 5px"><Plus /></el-icon> 创建新问卷
      </el-button>
    </div>

    <div v-if="loading" class="loading-box">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="surveyList.length === 0" class="empty-box">
      <el-empty description="暂无问卷，快去创建一个吧！" />
    </div>

    <div v-else class="grid">
      <el-card class="box-card" shadow="hover" v-for="item in surveyList" :key="item.id">
        <template #header>
          <div class="card-header">
            <span class="title-text" :title="item.title">{{ item.title }}</span>

            <el-tag v-if="item.status === 1" type="success" size="small" effect="dark">
              <el-icon><VideoPlay /></el-icon> 已发布
            </el-tag>

            <el-tag v-else-if="item.status === 0" type="info" size="small" effect="plain">
              <el-icon><EditPen /></el-icon> 草稿箱
            </el-tag>

            <el-tag v-else type="danger" size="small">已结束</el-tag>
          </div>
        </template>

        <div class="card-desc">
          {{ item.description || '暂无描述' }}
        </div>
        <div class="card-time">ID: {{ item.id }}</div>

        <div class="actions">
          <el-tooltip content="用户填写链接" placement="top">
            <el-button type="primary" link @click="toFill(item.id)">
              <el-icon><Edit /></el-icon> 填写
            </el-button>
          </el-tooltip>

          <el-tooltip content="分享问卷" placement="top">
            <el-button type="success" link @click="handleShare(item)" :disabled="item.status === 0">
              <el-icon><Share /></el-icon> 分享
            </el-button>
          </el-tooltip>

          <el-tooltip content="查看数据报表" placement="top">
            <el-button type="warning" link @click="toStats(item.id)">
              <el-icon><PieChart /></el-icon> 统计
            </el-button>
          </el-tooltip>

          <el-popconfirm
            title="确定要删除该问卷吗？"
            confirm-button-text="删除"
            cancel-button-text="取消"
            @confirm="handleDelete(item.id)"
          >
            <template #reference>
              <el-button type="danger" link>
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="shareDialogVisible" title="问卷分享" width="90%" center>
      <div class="share-content">
        <div class="qrcode-box">
          <qrcode-vue :value="shareUrl" :size="200" level="H" />
          <p class="tip">扫码在手机上填写</p>
        </div>

        <div class="link-box">
          <el-input v-model="shareUrl" readonly>
            <template #append>
              <el-button @click="copyLink">复制链接</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSurveyList, deleteSurvey } from '@/api/survey'
import { ElMessage } from 'element-plus'
import { Plus, Edit, PieChart, Delete, Share, VideoPlay, EditPen } from '@element-plus/icons-vue'
import QrcodeVue from 'qrcode.vue'

const router = useRouter()
const surveyList = ref([])
const loading = ref(true)
const shareDialogVisible = ref(false)
const shareUrl = ref('')

onMounted(() => {
  fetchList()
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getSurveyList()
    surveyList.value = res
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const toFill = (id) => {
  router.push(`/fill/${id}`)
}

const toStats = (id) => {
  router.push(`/stats/${id}`)
}

const handleDelete = async (id) => {
  try {
    await deleteSurvey(id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    console.error(error)
  }
}

const handleShare = (row) => {
  const baseUrl = window.location.origin
  shareUrl.value = `${baseUrl}/fill/${row.id}`
  shareDialogVisible.value = true
}

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.list-container {
  width: 95%;
  /* max-width: 1200px; */
  margin: 20px auto;
  padding: 20px;   
  box-sizing: border-box;
}


.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 15px;       
}

.left-title {
  display: flex;
  align-items: baseline;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin-left: 10px;
}

.loading-box {
  padding: 20px;
  background: #fff;
}

.empty-box {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  text-align: center;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.box-card {
  transition: all 0.3s;
  border-radius: 8px;
}

.box-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-text {
  font-weight: bold;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px; 
  display: inline-block;
}

.card-desc {
  color: #606266;
  font-size: 14px;
  margin: 15px 0;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4; 
}

.card-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
}

.actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap; 
}


.actions .el-button {
  margin-left: 0 !important; 
  padding: 5px 8px;
}

.share-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.qrcode-box {
  text-align: center;
}

.qrcode-box .tip {
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}

.link-box {
  width: 100%;
}


@media (max-width: 768px) {
  .list-container {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    align-items: stretch; 
  }
  
  .el-button {
    width: 100%; 
  }
  
  .left-title {
    margin-bottom: 10px;
  }
}
</style>