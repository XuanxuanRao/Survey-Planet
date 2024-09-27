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
      // redirect: '/questionnaire/questionnairePage',
      redirect: '/login',
      children: [
        {path: '/questionnaire/questionnairePage', component: () => import('@/views/questionnaire/questionnairePage.vue')},
        {path: '/questionnaire/squarePage', component: () => import('@/views/questionnaire/squarePage.vue')},
      ]
    }
  ]
})

export default router
