import axios from "axios"
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


// 响应拦截器,在接收到响应后，可以对响应数据进行处理。
instance.interceptors.response.use(function (response){
        return response.data
    },function (error) {
        return Promise.reject(error)
    }
)

export default instance
