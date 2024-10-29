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
        { path: '/questionnaire/CreateQuestionnaire', component: () => import('@/views/questionnaire/createQuestionnairePage.vue')},
        { path: '/questionnaire/questionnaireSquare', component: () => import('@/views/questionnaire/squarePage.vue')},
        { path: '/questionnaire/fillQuestionnaire', component: () => import('@/views/questionnaire/fillQuestionnairePage.vue')},
        { path: '/questionnaire/modifyQuestionnaire', component: () => import('@/views/questionnaire/modifyQuestionnaire.vue') },
        { path: '/questionnaire/createQuestion', component: () => import('@/views/questionnaire/createQuestionnaire.vue') },
        { path: '/questionnaire/createExamQuestion', component: () => import('@/views/questionnaire/createExamQuestionnaire.vue') },
        { path: '/questionnaire/viewQuestionnaire', component: () => import('@/views/questionnaire/viewQuestionnaire.vue') },
        { path: '/userCenter', component: () => import('@/views/questionnaire/userInfo.vue') },
        // { path: '/questionnaire/fill', component: () => import('@/views/questionnaire/fillQuestionnaire.vue') },
        { path: '/fill/:code', component: () => import('@/views/questionnaire/fillQuestionnaire.vue')},
      ]
    }
  ]
})

export default router
