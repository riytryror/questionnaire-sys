<template>
  <div class="stats-container">
    <div class="header">
      <el-button @click="$router.push('/')" icon="ArrowLeft">返回列表</el-button>
      <h2 class="page-title">问卷数据统计</h2>
      <el-button
        type="success"
        icon="Download"
        :loading="exporting"
        @click="handleExport"
        style="margin-left: auto"
      >
        导出 Excel
      </el-button>
    </div>

    <div v-if="loading" class="loading-box">
      <el-icon class="is-loading" size="40" color="#409EFF"><Loading /></el-icon>
      <p>数据分析中...</p>
    </div>

    <div v-else-if="!statsList || statsList.length === 0" class="empty-box">
      <el-empty description="暂无答卷数据" />
    </div>

    <div v-else v-for="(item, index) in statsList" :key="index" class="chart-card">
      <div class="card-header">
        <h3>
          <span class="q-seq">{{ index + 1 }}.</span>
          {{ item.title }}
          <el-tag effect="plain" round size="small" class="type-tag">{{
            getLabel(item.type)
          }}</el-tag>
        </h3>
        <span class="sub-text">
          样本: {{ isChartType(item.type) ? getChartTotal(item.chartData) : item.answers.length }}
        </span>
      </div>

      <div v-if="isChartType(item.type)" class="chart-area">
        <div :id="'chart-' + index" class="chart-box"></div>
      </div>

      <div v-else class="list-area">
        <el-alert
          v-if="!item.answers || item.answers.length === 0"
          title="暂无数据"
          type="warning"
          :closable="false"
          show-icon
        />

        <el-table
          v-else
          :data="item.answers.map((ans) => ({ content: ans }))"
          border
          stripe
          style="width: 100%"
          height="300"
        >
          <el-table-column type="index" label="#" width="60" align="center" />

          <el-table-column label="提交内容">
            <template #default="scope">
              <div v-if="['IMAGE', 'SIGN'].includes(item.type)" class="media-box">
                <el-image
                  style="width: 150px; height: 80px; border: 1px solid #eee; border-radius: 4px"
                  :src="processUrl(scope.row.content)"
                  :preview-src-list="[processUrl(scope.row.content)]"
                  fit="contain"
                  preview-teleported
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon> 无法加载
                    </div>
                  </template>
                </el-image>
              </div>

              <div v-else-if="item.type === 'AUDIO'" class="media-box">
                <audio controls :src="processUrl(scope.row.content)"></audio>
              </div>

              <div v-else-if="item.type === 'VIDEO'" class="media-box">
                <video
                  controls
                  :src="processUrl(scope.row.content)"
                  style="max-width: 300px; max-height: 200px"
                ></video>
              </div>

              <div v-else-if="item.type === 'FILE'">
                <el-link
                  type="primary"
                  :href="processUrl(scope.row.content)"
                  target="_blank"
                  :underline="false"
                >
                  <el-icon style="margin-right: 4px"><Document /></el-icon> 下载/查看文件
                </el-link>
              </div>

              <div v-else class="text-content">
                {{ scope.row.content }}
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { getSurveyStats, exportSurveyExcel } from '@/api/survey'
import { Loading, Document, Download, ArrowLeft, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(true)
const exporting = ref(false)
const statsList = ref([])
const chartInstances = []

const BASE_URL = ''

const typeMap = {
  SINGLE: '单选题',
  MULTI: '多选题',
  DROPDOWN: '下拉框',
  RATING: '打分题',
  TEXT: '填空题',
  AUDIO: '录音',
  VIDEO: '视频',
  IMAGE: '图片',
  FILE: '文件',
  SIGN: '签名',
  RANK: '排序题',
}

const getLabel = (type) => typeMap[type] || '题目'
const isChartType = (type) => ['SINGLE', 'MULTI', 'DROPDOWN', 'RATING', 'RANK'].includes(type)

onMounted(async () => {
  const surveyId = route.params.id
  if (!surveyId) return

  try {
    const res = await getSurveyStats(surveyId)
    const rawData = res.data || res

    statsList.value = rawData.map((item) => {
      // 合并所有可能的列表数据
      let combinedAnswers = [
        ...(item.answers || []),
        ...(item.textList || []),
        ...(item.imageList || []),
        ...(item.audioList || []),
      ]

      combinedAnswers = [...new Set(combinedAnswers)].filter((v) => v !== null && v !== '')

      // 映射图表数据
      let processedChartData = []
      if (item.chartData && item.chartData.length > 0) {
        processedChartData = item.chartData.map((d) => ({
          name: d.name || '未知选项',
          value: d.value !== undefined ? d.value : d.count !== undefined ? d.count : d.num || 0,
        }))
      }

      return {
        ...item,
        answers: combinedAnswers,
        chartData: processedChartData,
      }
    })

    // 初始化图表
    initCharts()
    window.addEventListener('resize', handleResize)
  } catch (e) {
    console.error(e)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
})

const processUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('data:')) return url
  if (typeof url === 'string' && url.startsWith('/uploads/')) {
    return BASE_URL + url
  }
  return url
}

const getChartTotal = (data) => {
  if (!data) return 0
  return data.reduce((sum, item) => sum + (Number(item.value) || 0), 0)
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstances.forEach((chart) => chart.dispose())
})

const handleResize = () => {
  chartInstances.forEach((chart) => chart.resize())
}

const initCharts = () => {
  // 延迟执行确保DOM渲染
  setTimeout(() => {
    statsList.value.forEach((item, index) => {
      if (!isChartType(item.type)) return

      const domId = 'chart-' + index
      const chartDom = document.getElementById(domId)

      if (!chartDom) return

      if (echarts.getInstanceByDom(chartDom)) {
        echarts.dispose(chartDom)
      }

      const myChart = echarts.init(chartDom)
      chartInstances.push(myChart)

      const validData = (item.chartData || []).map((d) => ({
        name: d.name,
        value: Number(d.value),
      }))

      const hasData = validData.length > 0 && validData.some((d) => d.value > 0)

      const option = {
        title: {
          show: !hasData,
          text: '暂无数据',
          left: 'center',
          top: 'center',
          textStyle: { color: '#999' },
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}人 ({d}%)',
        },
        legend: {
          bottom: '0%',
          left: 'center',
        },
        series: [
          {
            name: '数据分布',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: true,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2,
            },
            label: {
              show: true,
              formatter: '{b}: {c}',
            },
            data: hasData ? validData : [],
          },
        ],
      }

      myChart.setOption(option)
    })
  }, 300)
}

const handleExport = async () => {
  const surveyId = route.params.id
  exporting.value = true
  try {
    const res = await exportSurveyExcel(surveyId)
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `问卷数据_${surveyId}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.stats-container {
  max-width: 1000px;
  margin: 40px auto;
  padding: 0 20px;
}
.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}
.page-title {
  margin: 0;
  font-size: 24px;
  color: #303133;
}
.loading-box {
  text-align: center;
  margin-top: 100px;
  color: #909399;
}
.empty-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  text-align: center;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}
.card-header {
  margin-bottom: 20px;
  border-left: 4px solid #409eff;
  padding-left: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.q-seq {
  color: #409eff;
  font-weight: bold;
  margin-right: 5px;
}
.type-tag {
  margin-left: 10px;
  vertical-align: middle;
}
.sub-text {
  font-size: 12px;
  color: #909399;
}

.chart-area {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 20px 0;
}

.chart-box {
  width: 100%;
  min-width: 300px;
  height: 350px;
}

.media-box {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  padding: 10px;
  border-radius: 4px;
}
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #909399;
  font-size: 12px;
}
.text-content {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #606266;
}
</style>
