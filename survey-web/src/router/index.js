import { createRouter, createWebHistory } from 'vue-router'

import SurveyList from '../views/SurveyList.vue'
import SurveyFill from '../views/SurveyFill.vue'
import SurveyStats from '../views/SurveyStats.vue'
import SurveyDesign from '../views/SurveyDesign.vue' 

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: SurveyList // 访问根路径，显示列表页
    },
    {
      path: '/design',
      name: 'survey-design',
      component: SurveyDesign // 访问 /design，显示设计页
    },
    {
      path: '/survey/:id',
      name: 'survey-fill',
      component: SurveyFill
    },
    {
      path: '/stats/:id',
      name: 'survey-stats',
      component: SurveyStats
    }
  ]
})

export default router