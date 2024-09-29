import axios from "axios"

const baseURL = "http://localhost:8088"

const instance = axios.create({
    baseURL,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
})


// 请求拦截器
instance.interceptors.request.use(
    (config) => {
        return config
    },function (error) {
        return Promise.reject(error)
    }
)


// 响应拦截器
instance.interceptors.response.use(function (response){
        return response.data
    },function (error) {
        return Promise.reject(error)
    }
)

export default instance
// export { baseURL }