import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/login/loginPage.vue'),
      meta:{requiresParallax:true}
    },
    {
      path: '/',
      component: () => import('@/views/layout/layoutPage.vue'),
      meta:{requiresParallax:false,requiresGradient:true},
      // redirect: '/questionnaire/createQuestionnaire',
      redirect: '/login',
      children: [
        { path: '/questionnaire/lookQuestionnaire', component: () => import('@/views/questionnaire/lookQuestionnaire.vue')},
        { path: '/questionnaire/CreateQuestionnaire', component: () => import('@/views/questionnaire/createQuestionnairePage.vue')},
        { path: '/questionnaire/questionnaireSquare', component: () => import('@/views/questionnaire/squarePage.vue')},
        { path: '/questionnaire/fillQuestionnaire', component: () => import('@/views/questionnaire/fillQuestionnairePage.vue')},   
        { path: '/questionnaire/modifyQuestionnaire', component: () => import('@/views/questionnaire/modifyQuestionnaire.vue') },  //新增
        { path: '/questionnaire/createQuestion', component: () => import('@/views/questionnaire/createQuestionnaire.vue') },       //新增
        { path: '/questionnaire/createExamQuestion', component: () => import('@/views/questionnaire/createExamQuestionnaire.vue') },  //新增
        { path: '/questionnaire/viewQuestionnaire', component: () => import('@/views/questionnaire/viewQuestionnaire.vue') },      //新增
        { path: '/userCenter', component: () => import('@/views/questionnaire/userInfo.vue') },
        { path: '/viewResult', component: () => import('@/views/questionnaire/viewResult.vue') },   
        { path: '/fill/:code', component: () => import('@/views/questionnaire/fillQuestionnaire.vue')},
        { path: '/responseSurvey', component: () => import('@/views/questionnaire/responseSurvey.vue') },
        { path: '/releaseSurvey', component: () => import('@/views/questionnaire/releaseSurvey.vue') },
      ]
    }
  ]
})

export default router
