import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import pinia from '@/stores/index'
import vuetyped from 'vue3typed'
import '@/assets/main.scss'

import ElementPlus from 'element-plus'

const app = createApp(App)
app.use(vuetyped)
app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
