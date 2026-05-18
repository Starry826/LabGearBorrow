// axios基础封装
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router/index'


const request = axios.create({
    baseURL: "/api",
    timeout: 10000
})

//拦截前端请求
request.interceptors.request.use(
    (config) =>{
        const loginUser = JSON.parse(localStorage.getItem('loginUser'));
        if(loginUser && loginUser.token){
            config.headers.id = loginUser.id;
            config.headers.token = loginUser.token;
        }
        if (config.data instanceof FormData) {
            delete config.headers['Content-Type']
        }
        return config
    },
    (error) =>{
        return Promise.reject(error)
    }
)
//拦截后端响应
request.interceptors.response.use(
    (response) =>{
        return response.data
    },
    (error) =>{
        if(error.response.status === 401){
            ElMessage.warning('请先登录')
            localStorage.removeItem('loginUser')
            router.push('/')
        }
        return Promise.reject(error)
    }
)

export default request