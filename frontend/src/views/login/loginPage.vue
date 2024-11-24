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

const getCode = async (tmp) => {
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
            const res = await getEmailCode(formModel.value.email, tmp)
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
        router.push({path: '/questionnaire/createQuestionnaire'})
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
    <div class="login_page">
        <div class="vuetype-container">
            <vuetyped :strings="['Ask, Analyze, Act',
            'Revolutionize the way you collect and analyze feedback',
            'Your ultimate tool for creating and managing surveys',
            'Unlock the power of data-driven decisions']"
                        :showCursor="true" :typeSpeed="80" :back-speed="20" :loop="true"
                        class="vuetype">
                <span class="typing"></span>
            </vuetyped>
        </div>
        <div class="ele">
            <span class="spacingEle"></span>
            <el-row>
                    <el-col :span="12" :offset="6" class="form">
                        <!-- <div class="spacingblock"></div> -->
                        <div class="bordered-block">
                        <!-- 注册 -->
                        <el-form style="display: flex; justify-content: center; align-items: center; flex-direction: column;"  :model="formModel" :rules="rules" ref="form" v-if="loginMode === 1">
                            <el-form-item>
                                <!-- <span class="spacing5"></span> -->
                                <h1 class="white">注册</h1>
                            </el-form-item>
                            <el-form-item style="display: flex; justify-content: center; align-items: center;" prop="username">
                                <!-- <span class="spacingUsername"></span> -->
                                <el-input style="width: 400px" v-model="formModel.username" :prefix-icon="User" placeholder="请输入用户名"></el-input>
                            </el-form-item>
                            <el-form-item style="display: flex; justify-content: center; align-items: center;" prop="email">
                                <!-- <span class="spacingUsername"></span> -->
                                <el-input style="width: 300px" v-model="formModel.email" :prefix-icon="User" placeholder="请输入邮箱"></el-input>
                                <el-button @click="getCode('reg')">
                                    {{seconds === totalSeconds? '获取验证码' : seconds + 's后重新发送'}}
                                </el-button>
                            </el-form-item>
                            <el-form-item style="display: flex; justify-content: center; align-items: center;" prop="code">
                                <!-- <span class="spacingUsername"></span> -->
                                <el-input style="width: 400px" v-model="formModel.code" :prefix-icon="User" placeholder="请输入验证码"></el-input>
                            </el-form-item>
                            <el-form-item style="display: flex; justify-content: center; align-items: center;" prop="password">
                                <!-- <span class="spacingUsername"></span> -->
                                <el-input style="width: 400px" v-model="formModel.password" :prefix-icon="Lock" type="password" placeholder="请输入密码"></el-input>
                            </el-form-item>
                            <el-form-item prop="repassword">
                                <!-- <span class="spacingUsername"></span> -->
                                <el-input style="width: 400px" v-model="formModel.repassword" :prefix-icon="Lock" type="password" placeholder="请再次输入密码"></el-input>
                            </el-form-item>
                            <el-form-item>
                                <!-- <span class="spacing2"></span> -->
                                <el-button @click="register" type="primary " class="custom-button">注册</el-button>
                            </el-form-item>
                            <el-form-item>
                                <!-- <span class="spacing"></span> -->
                                <el-link @click="loginMode = 0" class="white">去登录</el-link>
                            </el-form-item>
                        </el-form>

                        <!-- 登录 -->
                        <el-form style="display: flex; justify-content: center; align-items: center; flex-direction: column;" :model="formModel" :rules="rules" ref="form" v-else-if="loginMode === 0">
                            <div class="bgc">
                                <el-form-item>
                                    <span class="spacing"></span>
                                    <h1 class="white" >登录</h1>
                                </el-form-item >
                                <el-form-item style="display: flex; justify-content: center; align-items: center;"  prop="username">
                                    <!-- <span class="spacingUsername"></span> -->
                                    <el-input style="width: 400px" v-model="formModel.username" :prefix-icon="User" placeholder="请输入用户名"></el-input>
                                </el-form-item>
                                <el-form-item style="display: flex; justify-content: center; align-items: center;" prop="password">
                                    <!-- <span class="spacingUsername"></span> -->
                                    <el-input style="width: 400px" v-model="formModel.password" :prefix-icon="Lock" type="password" placeholder="请输入密码"></el-input>
                                </el-form-item>
                                <el-form-item>
                                    <span class="spacing2"></span>
                                    <el-button @click="login" type="primary" class="custom-button">登录</el-button>
                                </el-form-item>
                                <el-form-item>
                                    
                                    <span class="spacingQuZhuCe"></span>
                                    <el-link @click="loginMode = 1" class="white1">去注册</el-link>
                                    <span class="spacingWangJiMiMa"></span>
                                    <el-link @click="loginMode = 2" class="white1">忘记密码</el-link>
                                </el-form-item>
                                
                            </div>
                            
                        </el-form>

                        <!-- 忘记密码 -->
                        <el-form style="display: flex; justify-content: center; align-items: center; flex-direction: column;" :model="formModel" :rules="rules" ref="form" v-if="loginMode === 2">
                            <el-form-item>
                                <!-- <span class="spacingWangJi"></span> -->
                                <h1 class="white" >忘记密码</h1>
                            </el-form-item>
                            <el-form-item prop="email">
                                <!-- <span class="spacingUsername"></span> -->
                                <el-input style="width: 300px" v-model="formModel.email" :prefix-icon="User" placeholder="请输入邮箱"></el-input>
                                <el-button @click="getCode('reset')">
                                    {{seconds === totalSeconds? '获取验证码' : seconds + 's后重新发送'}}
                                </el-button>
                            </el-form-item>
                            <el-form-item prop="code">
                                <!-- <span class="spacingUsername"></span> -->
                                <el-input v-model="formModel.code" :prefix-icon="User" placeholder="请输入验证码" style="width: 400px"></el-input>
                            </el-form-item>
                            <el-form-item prop="password">
                                <!-- <span class="spacingUsername"></span> -->
                                <el-input v-model="formModel.password" :prefix-icon="Lock" type="password" placeholder="请输入密码" style="width: 400px"></el-input>
                            </el-form-item>
                            <el-form-item>
                                <!-- <span class="spacing2"></span> -->
                                <el-button @click="reset" type="primary" class="custom-button">重置密码</el-button>
                            </el-form-item>
                            <el-form-item>
                                <!-- <span class="spacingQuDengRu"></span> -->
                                <el-link @click="loginMode = 0" class="white">去登录</el-link>
                            </el-form-item>
                        </el-form>
                        </div>
                        
                    </el-col>
                </el-row>
            </div>
    </div>
</template>

<style scoped>
@font-face {
  font-family: DancingScript_Regular;
  font-weight: normal;
  src: url(@/assets/font/DancingScript-Regular.ttf) format("truetype");
  text-rendering: optimizeLegibility;
}
.white{
    color: white;
}
.white1{
    color: white;
    border-bottom: 1px solid wheat; /* 简化写法 */
}
/* 登入 */
.spacing {
  margin-right:180px; /* 调整这个值以满足实际需求 */
}
.spacing2 {
  margin-right:160px; /* 调整这个值以满足实际需求 */
}
.spacingQuZhuCe{
    margin-right: 140px;
}
.spacingWangJiMiMa{
    margin-right: 50px;
}
/* 注册 */
.spacing5{
    margin-right:285px; /* 调整这个值以满足实际需求 */
}


.spacing1 {
  margin-right:365px; /* 调整这个值以满足实际需求 */
}

.spacing3 {
  margin-right:100px; /* 调整这个值以满足实际需求 */
}
 .bgc{
     background-color: wheat ;
     background: transparent;
     backdrop-filter: blur(7px); /* 背景模糊效果，提升用户体验 */
} 
.spacingblock{
    margin-right:500px; /* 调整这个值以满足实际需求 */
}
.spacingUsername{
    margin-right: 1700px;
}
.spacingQuDengRu{
    margin-right: 470px;
}
.spacingWangJi{
    margin-right: 430px;
}

.bordered-block {
  border: 1px solid white; /* 简化写法 */
  padding: 1px; /* 可选：增加内边距 */
  width: 600px;
  
}
.ele{
  display: flex;
  justify-content: top;
  height: 100vh; 
  /* margin-right: 1050px; */
}
.spacingEle{
    margin-right: 300px;
}
 .login_page {
  display: flex;
  flex-direction: column;   
  height: 100vh; 
} 
.vuetype-container {
  height: 30vh; /* 视口高度 */
  display: flex;
  /* text-align: center; */
}
.vuetype{
  margin-top:10vh;
  margin-left: auto;
  margin-right: auto;
  font-size: 60px; /* 根据需要调整字体大小 */
  font-family: DancingScript_Regular;
  color: ghostwhite; /* 根据需要调整字体颜色 */
}
.custom-button {
  position: relative;
  width: 100px;
  height: 45px;
  text-align: center;
  line-height: 60px;
  color: #fff;
  font-size: 18px;
  text-decoration: none;
  font-family: sans-serif;
  border-radius: 30px;
  background: linear-gradient(90deg, #03a9f4, #f441a5, #ffeb3b, #03a9f4);
  background-size: 400%;
  transition: background-position 0.5s;
}
.custom-button:hover {
  animation: animate 8s linear infinite;
}

@keyframes animate {
  from {
    background-position: 0%;
  }
  to {
    background-position: 480%;
  }
}
</style>