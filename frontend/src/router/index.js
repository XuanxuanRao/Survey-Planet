import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/login/loginPage.vue'),
    },
    {
      path: '/',
      component: () => import('@/views/layout/layoutPage.vue'),
      redirect: '/questionnaire/createQuestionnaire',
      // redirect: '/login',
      children: [
        { path: '/questionnaire/CreateQuestionnaire', component: () => import('@/views/questionnaire/createQuestionnairePage.vue')},
        { path: '/questionnaire/questionnaireSquare', component: () => import('@/views/questionnaire/squarePage.vue')},
        { path: '/questionnaire/fillQuestionnaire', component: () => import('@/views/questionnaire/fillQuestionnairePage.vue')},
      ]
    }
  ]
})

export default router
