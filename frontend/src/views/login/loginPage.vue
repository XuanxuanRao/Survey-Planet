<script setup>
import { ref, onUnmounted, watch} from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import { getEmailCode, userRegisterService, userLoginService, userResetPasswordService} from '@/api/login.js'
// import router from '@/router'
import { useUserStore } from '@/stores/index'
import { useRouter } from 'vue-router'

const loginMode = ref(0)
// 0代表登录, 1代表注册，2代表忘记密码
const form = ref()

const formModel = ref({
    username: '',
    email: '',
    code: '',
    password: '',
    repassword: ''
})

const rules = ref({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 10, message: '用户名在 3 到 10 个字符', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { pattern: /^[A-Za-z0-9\u4e00-\u9fa5]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/, message: '请输入正确的邮箱地址', trigger: 'blur' }
    ],
    code: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        {pattern: /^[0-9]{6}$/, message: '验证码必须是6位数字', trigger: 'blur'}
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        // {pattern: /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$/, message: '密码必须包含数字和字母,长度在6-20之间', trigger: 'blur'}
    ],
    repassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {pattern: /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$/, message: '密码必须包含数字和字母,长度在6-20之间', trigger: 'blur'},
        {validator: (rule, value, callback) => {
            if (value !== formModel.value.password) {
                callback(new Error('两次输入密码不一致'))
            } else {
                callback()
            }
        }, trigger: 'blur'}
    ]
})



const totalSeconds = ref(60)
const seconds = ref(60)
const timer = ref(null)

const getCode = async () => {
    try {
        await form.value.validateField('email')
        if (form.value.fields.email?.validateState === 'error') {
            return
        }
        if(!timer.value) {
            timer.value = setInterval(() => {
                // console.log(timer.value)
                seconds.value--
                if (seconds.value === 0) {
                    seconds.value = totalSeconds.value
                    clearInterval(timer.value)
                    timer.value = null
                }
            }, 1000)
            const res = await getEmailCode(formModel.value.email, 'reg')
            if (res.code === 1) {
                ElMessage({
                    // showClose: true,
                    duration: 3000,
                    message: '验证码发送成功',
                    type: 'success'
                })
                seconds.value = totalSeconds.value
                clearInterval(timer.value)
                timer.value = null
                return
            }
            // console.log(res)
        }
    } catch (error) {
        console.error('验证字段时出错:', error)
    }

//     onUnmounted(() => {
//     if (timer.value) {
//         clearInterval(timer.value)
//         timer.value = null
//     }
// })

}

const register = async () => {
    await form.value.validate()
    const res = await userRegisterService(formModel.value.username, 
                                            formModel.value.email,
                                            formModel.value.password,
                                            formModel.value.code)
    console.log(res)
    if (res.code === 0) {
        ElMessage({
            // showClose: true,
            duration: 3000,
            message: res.msg,
            type: 'error'
        })
    }else if (res.code === 1) {
        ElMessage({
            // showClose: true,
            duration: 3000,
            message: '注册成功',
            type: 'success'
        })
        isRegester.value = false
    }
}

const userStore = useUserStore()
const router = useRouter()

const login = async () => {
    await form.value.validate()
    const res = await userLoginService(formModel.value.username,
                                        formModel.value.email,
                                        formModel.value.password)
    console.log(res)
    if (res.code === 0) {
        ElMessage({
            // showClose: true,
            duration: 3000,
            message: res.msg,
            type: 'error'
        })
    }else if (res.code === 1) {
        ElMessage({
            // showClose: true,
            duration: 3000,
            message: '登录成功',
            type: 'success'
        })
        userStore.setToken(res.data.token)
        router.push({path: '/'})
    }
}

const reset = async () => {
    await form.value.validate()
    const res = await userResetPasswordService(formModel.value.email,
                                        formModel.value.password,
                                        formModel.value.code)
    console.log(res)
    if (res.code === 0) {
        ElMessage({
            // showClose: true,
            duration: 3000,
            message: res.msg,
            type: 'error'
        })
    }else if (res.code === 1) {
        ElMessage({
            // showClose: true,
            duration: 3000,
            message: '重置成功',
            type: 'success'
        })
    }
}

watch(loginMode, () => {
    formModel.value = {
        username: '',
        email: '',
        code: '',
        password: '',
        repassword: ''
    }
})

</script>

<template>
    <div>
        <el-row class="login_page">
            <el-col :span="12" :offset="6" class="form">
                <!-- 注册 -->
                <el-form :model="formModel" :rules="rules" ref="form" v-if="loginMode === 1">
                    <el-form-item>
                        <h1>注册</h1>
                    </el-form-item>
                    <el-form-item prop="username">
                        <el-input v-model="formModel.username" :prefix-icon="User" placeholder="请输入用户名"></el-input>
                    </el-form-item>
                    <el-form-item prop="email">
                        <el-input v-model="formModel.email" :prefix-icon="User" placeholder="请输入邮箱"></el-input>
                        <el-button @click="getCode">
                            {{seconds === totalSeconds? '获取验证码' : seconds + 's后重新发送'}}
                        </el-button>
                    </el-form-item>
                    <el-form-item prop="code">
                        <el-input v-model="formModel.code" :prefix-icon="User" placeholder="请输入验证码"></el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input v-model="formModel.password" :prefix-icon="Lock" type="password" placeholder="请输入密码"></el-input>
                    </el-form-item>
                    <el-form-item prop="repassword">
                        <el-input v-model="formModel.repassword" :prefix-icon="Lock" type="password" placeholder="请再次输入密码"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button @click="register">注册</el-button>
                    </el-form-item>
                    <el-form-item>
                        <el-link @click="loginMode = 0">去登录</el-link>
                    </el-form-item>
                </el-form>

                <!-- 登录 -->
                <el-form :model="formModel" :rules="rules" ref="form" v-else-if="loginMode === 0">
                    <el-form-item>
                        <h1>登录</h1>
                    </el-form-item>
                    <el-form-item prop="username">
                        <el-input v-model="formModel.username" :prefix-icon="User" placeholder="请输入用户名"></el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input v-model="formModel.password" :prefix-icon="Lock" type="password" placeholder="请输入密码"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button @click="login">登录</el-button>
                    </el-form-item>
                    <el-form-item>
                        <el-link @click="loginMode = 1">去注册</el-link>
                    </el-form-item>
                    <el-form-item>
                        <el-link @click="loginMode = 2">忘记密码</el-link>
                    </el-form-item>
                </el-form>

                <!-- 忘记密码 -->
                 <el-form :model="formModel" :rules="rules" ref="form" v-if="loginMode === 2">
                    <el-form-item>
                        <h1>忘记密码</h1>
                    </el-form-item>
                    <el-form-item prop="email">
                        <el-input v-model="formModel.email" :prefix-icon="User" placeholder="请输入邮箱"></el-input>
                        <el-button @click="getCode">
                            {{seconds === totalSeconds? '获取验证码' : seconds + 's后重新发送'}}
                        </el-button>
                    </el-form-item>
                    <el-form-item prop="code">
                        <el-input v-model="formModel.code" :prefix-icon="User" placeholder="请输入验证码"></el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input v-model="formModel.password" :prefix-icon="Lock" type="password" placeholder="请输入密码"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button @click="reset">重置密码</el-button>
                    </el-form-item>
                    <el-form-item>
                        <el-link @click="loginMode = 0">去登录</el-link>
                    </el-form-item>
                </el-form>
            </el-col>
        </el-row>
    </div>
</template>

<style scoped>
</style>