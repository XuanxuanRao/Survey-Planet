import axios from "axios"
import router from "@/router"
//设置基本 URL：
//const baseURL = "http://localhost:8088"
const baseURL = "http://59.110.163.198:8088"
// 创建 axios 实例
const instance = axios.create({
    baseURL,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
})


// 请求拦截器,在请求发送之前，可以对请求配置进行修改
instance.interceptors.request.use(
    (config) => {
        return config
    },function (error) {
        return Promise.reject(error)
    }
)


// 响应拦截器
instance.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code === 0 && res.msg === 'NOT_LOGIN') {
            // 未登录，跳转到登录页面
            router.push('/login')
            return Promise.reject('未登录，跳转到登录页面')
        } else {
            return res
        }
    },
    (error) => {
        return Promise.reject(error)
    }
)

export default instance
