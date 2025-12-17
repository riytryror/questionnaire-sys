<template>
  <div class="list-container">
    <div class="header">
      <div class="left-title">
        <h2>📑 问卷管理系统</h2>
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
      <el-card 
        class="box-card" 
        shadow="hover" 
        v-for="item in surveyList" 
        :key="item.id"
      >
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
        <div class="card-time">
          ID: {{ item.id }}
        </div>

        <div class="actions">
          <el-tooltip content="用户填写链接" placement="top">
            <el-button type="primary" link @click="toFill(item.id)">
              <el-icon><Edit /></el-icon> 填写
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSurveyList, deleteSurvey } from '@/api/survey' // 导入接口
import { ElMessage } from 'element-plus'
import { Plus, Edit, PieChart, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const surveyList = ref([])
const loading = ref(true)

// 1. 页面加载时获取数据
onMounted(() => {
  fetchList()
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getSurveyList()
    // 假设后端直接返回 List<Survey>，或者是 Result.data
    // 如果你的 request.js 拦截器已经解包了 res.data，就直接用 res
    surveyList.value = res 
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 2. 跳转逻辑
const toFill = (id) => {
  // 打开新窗口让用户模拟填写
  const url = router.resolve({ path: `/survey/${id}` }).href
  window.open(url, '_blank')
}

const toStats = (id) => {
  router.push(`/stats/${id}`)
}

// 3. 删除逻辑
const handleDelete = async (id) => {
  try {
    await deleteSurvey(id)
    ElMessage.success('删除成功')
    fetchList() // 刷新列表
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.list-container { max-width: 1200px; margin: 0 auto; padding: 40px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.subtitle { font-size: 14px; color: #909399; margin-left: 10px; }

.loading-box { padding: 20px; background: #fff; }
.empty-box { background: #fff; padding: 40px; border-radius: 8px; }

.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }

.box-card { transition: all 0.3s; border-radius: 8px; }
.box-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1); }

.card-header { display: flex; justify-content: space-between; align-items: center; }
.title-text { font-weight: bold; font-size: 16px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px;}

.card-desc { color: #606266; font-size: 14px; margin: 15px 0; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-time { font-size: 12px; color: #999; margin-bottom: 15px; }

.actions { border-top: 1px solid #f0f0f0; padding-top: 15px; display: flex; justify-content: space-between; }
</style>