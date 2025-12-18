<template>
  <div class="stats-container">
    <div class="header">
      <el-button @click="$router.push('/')" icon="ArrowLeft">返回列表</el-button>
      <h2 class="page-title">📊 问卷数据统计</h2>
      <el-button 
    type="success" 
    icon="Download" 
    :loading="exporting" 
    @click="handleExport"
    style="margin-left: auto;" 
  >
    导出 Excel
  </el-button>
    </div>

    <div v-if="loading" class="loading-box">
      <el-icon class="is-loading" size="40" color="#409EFF"><Loading /></el-icon>
      <p>数据疯狂分析中...</p>
    </div>

    <div v-else-if="!statsList || statsList.length === 0" class="empty-box">
       <el-empty description="暂无答卷数据" />
    </div>

    <div v-else v-for="(item, index) in statsList" :key="index" class="chart-card">
      
      <div class="card-header">
        <h3>
          <span class="q-seq">{{ index + 1 }}.</span> 
          {{ item.title }}
          <el-tag effect="plain" round size="small" class="type-tag">{{ getLabel(item.type) }}</el-tag>
        </h3>
      </div>

      <div v-if="isChartType(item.type)" class="chart-area">
         <div :id="'chart-' + index" class="chart-box"></div>
      </div>

      <div v-else class="list-area">
        
        <el-alert 
          v-if="!item.answers || item.answers.length === 0" 
          title="暂无用户提交数据" 
          type="info" 
          :closable="false" 
          show-icon
        />

        <el-table 
          v-else 
          :data="item.answers.map(ans => ({ content: ans }))" 
          border 
          stripe 
          style="width: 100%"
        >
          <el-table-column type="index" label="#" width="50" align="center" />
          
          <el-table-column label="用户提交内容">
            <template #default="scope">
              
              <div v-if="item.type === 'AUDIO'" class="media-box">
                <audio controls :src="scope.row.content"></audio>
              </div>

              <div v-else-if="item.type === 'VIDEO'" class="media-box">
                <video controls :src="scope.row.content" style="max-width: 300px; max-height: 200px"></video>
              </div>

              <div v-else-if="['IMAGE', 'SIGN'].includes(item.type)" class="media-box">
                <el-image 
                  style="width: 100px; height: 100px; border-radius: 4px;"
                  :src="scope.row.content"
                  :preview-src-list="[scope.row.content]"
                  fit="cover"
                  preview-teleported
                />
              </div>

              <div v-else-if="item.type === 'FILE'">
                <el-link type="primary" :href="scope.row.content" target="_blank" :underline="false">
                  <el-icon style="margin-right:4px"><Document /></el-icon> 点击下载文件
                </el-link>
              </div>

              <div v-else class="text-content">
                {{ scope.row.content }}
              </div>

            </template>
          </el-table-column>
          
          <el-table-column 
            v-if="['AUDIO','VIDEO','IMAGE','FILE','SIGN'].includes(item.type)" 
            label="操作" 
            width="100" 
            align="center"
          >
             <template #default="scope">
                <el-link type="primary" :href="scope.row.content" target="_blank">
                  <el-icon><Download /></el-icon> 下载
                </el-link>
             </template>
          </el-table-column>

        </el-table>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue' // 引入 onBeforeUnmount
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { getSurveyStats } from '@/api/survey'
import { Loading, Document, Download, ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(true)
const statsList = ref([])
// 存放所有的 chart 实例，方便 resize 和销毁
const chartInstances = [] 

const BASE_URL = 'http://localhost:8080'

const typeMap = {
  'SINGLE': '单选题', 'MULTI': '多选题', 'DROPDOWN': '下拉框', 
  'RATING': '打分题', 'TEXT': '填空题', 'AUDIO': '录音', 
  'VIDEO': '视频', 'IMAGE': '图片', 'FILE': '文件', 'SIGN': '签名'
}
const getLabel = (type) => typeMap[type] || '题目'
const isChartType = (type) => ['SINGLE', 'MULTI', 'DROPDOWN', 'RATING', 'RANK'].includes(type)

onMounted(async () => {
  const surveyId = route.params.id
  if (!surveyId) return

  try {
    const res = await getSurveyStats(surveyId)
    // 假设未解包，如果拦截器解包了请去掉 .data
    const rawData = res.data || res 

    statsList.value = rawData.map(item => {
      let rawList = []

      // ⭐⭐⭐ 核心修复：根据 Type 拿数据，而不是谁不为空拿谁 ⭐⭐⭐
      if (item.type === 'AUDIO') {
         rawList = item.audioList
      } else if (item.type === 'IMAGE' || item.type === 'SIGN') {
         rawList = item.imageList 
      } else if (['TEXT', 'FILE', 'VIDEO'].includes(item.type)) {
         rawList = item.textList
      } else {
         // 兜底：万一后端类型写错了，尝试合并所有非空列表
         rawList = [...(item.audioList||[]), ...(item.imageList||[]), ...(item.textList||[])]
      }

      // 处理 URL
      const processedAnswers = (rawList || []).map(content => {
        if (content && typeof content === 'string' && content.startsWith('/')) {
           return BASE_URL + content
        }
        return content
      })

      return { ...item, answers: processedAnswers }
    })
    
    nextTick(() => {
      initCharts()
      // 监听窗口变化，让图表自适应
      window.addEventListener('resize', handleResize)
    })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

// 销毁前移除监听，防止报错
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach(chart => chart.dispose())
})

const handleResize = () => {
  chartInstances.forEach(chart => chart.resize())
}

const initCharts = () => {
  statsList.value.forEach((item, index) => {
    if (isChartType(item.type)) {
      const chartDom = document.getElementById('chart-' + index)
      if (chartDom) {
        if (echarts.getInstanceByDom(chartDom)) {
           echarts.dispose(chartDom)
        }
        const myChart = echarts.init(chartDom)
        chartInstances.push(myChart) // 存起来

        const validData = (item.chartData || []).filter(d => d.name)
        
        const option = {
          tooltip: { trigger: 'item' },
          legend: { bottom: '0%' },
          series: [
            {
              name: '选择人数',
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
              data: validData.length > 0 ? validData : [{name:'暂无数据', value:0}]
            }
          ]
        }
        myChart.setOption(option)
      }
    }
  })
}


import { exportSurveyExcel } from '@/api/survey' // 记得导入刚才写的接口
import { ElMessage } from 'element-plus'

// 定义加载状态
const exporting = ref(false)

// 导出处理函数
const handleExport = async () => {
  const surveyId = route.params.id
  exporting.value = true // 开启加载转圈圈

  try {
    // 1. 发起请求
    const res = await exportSurveyExcel(surveyId)
    
    // ⚠️ 注意：有些封装的 request.js 拦截器会直接返回 res.data
    // 如果下载下来的文件打不开，尝试改成: const blob = new Blob([res.data]) 
    // 这里假设 res 就是返回的 blob 对象
    const blob = new Blob([res], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })

    // 2. 创建一个临时的下载链接
    const downloadLink = document.createElement('a')
    const href = window.URL.createObjectURL(blob) // 创建 Blob URL
    downloadLink.href = href
    
    // 3. 设置文件名 (你可以自定义，也可以尝试从响应头读取)
    downloadLink.download = `问卷数据_${surveyId}_${new Date().getTime()}.xlsx`
    
    // 4. 触发点击，开始下载
    document.body.appendChild(downloadLink)
    downloadLink.click()
    
    // 5. 清理垃圾
    document.body.removeChild(downloadLink)
    window.URL.revokeObjectURL(href)
    
    ElMessage.success('导出成功！请查看下载目录')

  } catch (e) {
    console.error('导出失败', e)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exporting.value = false // 关闭加载
  }
}
</script>

<style scoped>
.stats-container { max-width: 1000px; margin: 40px auto; padding: 0 20px; }
.header { display: flex; align-items: center; gap: 20px; margin-bottom: 30px; }
.page-title { margin: 0; }
.loading-box { text-align: center; margin-top: 100px; color: #909399; }
.empty-box { background: white; padding: 40px; border-radius: 8px; }

.chart-card { background: #fff; border-radius: 8px; padding: 25px; margin-bottom: 25px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.card-header { margin-bottom: 20px; border-left: 4px solid #409EFF; padding-left: 15px; }
.q-seq { color: #409EFF; font-weight: bold; margin-right: 5px; }
.type-tag { margin-left: 10px; vertical-align: middle; }

.chart-area { display: flex; justify-content: center; }
.chart-box { width: 100%; height: 300px; }

.media-box audio { height: 40px; }
.text-content { white-space: pre-wrap; line-height: 1.6; }
</style>