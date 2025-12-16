<template>
  <div style="padding: 50px;">
    <h1>前端测试控制台</h1>
    
    <el-card v-if="survey">
      <template #header>
        <div class="card-header">
          <span>问卷标题：{{ survey.title }}</span>
          <el-tag type="success">状态: {{ survey.status === 1 ? '发布中' : '草稿' }}</el-tag>
        </div>
      </template>
      <p>描述：{{ survey.description }}</p>
      <p>题目数量：{{ survey.questions.length }}</p>
    </el-card>

    <el-button type="primary" @click="fetchData" style="margin-top: 20px;">
      点击加载问卷数据
    </el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getSurveyDetail } from '@/api/survey'

const survey = ref(null)

const fetchData = async () => {
  try {
    // 假设我们要查 ID 为 2 的问卷 (根据你刚才的 JSON)
    const data = await getSurveyDetail(2)
    console.log('后端返回的数据:', data)
    survey.value = data
  } catch (error) {
    console.error('请求失败', error)
  }
}
</script>