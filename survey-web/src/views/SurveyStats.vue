<template>
  <div class="stats-container">
    <h2 class="page-title">📊 问卷数据统计</h2>

    <div v-if="loading" style="text-align: center; margin-top: 50px">
      <el-icon class="is-loading" size="30"><Loading /></el-icon>
      <p>数据分析中...</p>
    </div>

    <div v-else v-for="(item, index) in statsList" :key="item.questionId" class="chart-card">
      <h3>
        {{ index + 1 }}. {{ item.title }}
        <el-tag size="small">{{ item.type === 'SINGLE' ? '单选题' : '录音题' }}</el-tag>
      </h3>

      <div v-if="item.type === 'SINGLE'" class="chart-box" :id="'chart-' + item.questionId"></div>

      <div v-if="item.type === 'AUDIO'" class="audio-list">
        <el-alert
          v-if="item.audioList.length === 0"
          title="暂无录音数据"
          type="info"
          :closable="false"
        />

        <el-table
          v-else
          :data="item.audioList.map((url) => ({ url }))"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="用户录音文件">
            <template #default="scope">
              <audio
                controls
                :src="'http://localhost:8080' + scope.row.url"
                class="audio-player"
              ></audio>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-link
                type="primary"
                :href="'http://localhost:8080' + scope.row.url"
                target="_blank"
                >下载</el-link
              >
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import * as echarts from 'echarts'
import { Loading } from '@element-plus/icons-vue'

const route = useRoute()
const statsList = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const id = route.params.id
    // 请求后端统计接口
    const res = await request.get(`/api/surveys/${id}/stats`)
    statsList.value = res

    // 拿到数据后关闭loading；否则数据加载完时还是loading,图表dom隐藏，执行init时找不到，不能及时渲染
    loading.value = false
    // 等待 DOM 渲染完成，再画图
    await nextTick()

    // 保证图表正常显示
    setTimeout(() => {
        initCharts()
    }, 100)

  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

const initCharts = () => {
  console.log('📊 开始尝试绘制图表，总题目数:', statsList.value.length)
  
  statsList.value.forEach(item => {

    if (item.type === 'SINGLE') {
      const domId = 'chart-' + item.questionId
      const chartDom = document.getElementById(domId)
      
      console.log(`--------------------------------`)
      console.log(`题目ID: ${item.questionId}, 标题: ${item.title}`)
      console.log(`DOM元素是否存在:`, chartDom)
      console.log(`后端返回的数据 chartData:`, item.chartData)

      if (!chartDom) {
        console.error('❌ 失败原因：找不到 ID 为 ' + domId + ' 的 div 容器！')
        return
      }

      if (!item.chartData || item.chartData.length === 0) {
        console.warn('⚠️ 警告：数据为空！这道题还没有人回答过，或者数据库存的值有问题。')
        return
      }

      console.log('✅ 准备开始绘图...')
      const myChart = echarts.init(chartDom)
      myChart.setOption({
        tooltip: { trigger: 'item' },
        series: [
          {
            name: '选项统计',
            type: 'pie',
            radius: '50%',
            data: item.chartData
          }
        ]
      })
    }
  })
}
</script>

<style scoped>
/* 容器样式 */
.stats-container { 
  max-width: 900px; 
  margin: 20px auto; 
  padding: 20px; 
}

/* 标题样式 */
.page-title { 
  text-align: center; 
  margin-bottom: 30px; 
  color: #303133; 
}

/* 卡片样式 */
.chart-card { 
  background: #fff; 
  padding: 25px; 
  margin-bottom: 25px; 
  border-radius: 12px; 
  box-shadow: 0 4px 16px 0 rgba(0,0,0,0.08); 
  transition: all 0.3s; 
}

.chart-card:hover { 
  transform: translateY(-2px); 
  box-shadow: 0 6px 20px 0 rgba(0,0,0,0.12); 
}


.chart-box { 
  width: 100%; 
  height: 400px; 
  min-height: 400px;
}

/* 音频列表样式 */
.audio-list {
  margin-top: 10px;
}

/* 播放器样式 */
.audio-player { 
  width: 100%; 
  height: 40px; 
  outline: none; 
}
</style>