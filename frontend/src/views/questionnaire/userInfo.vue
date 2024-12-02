<script setup>
import { getUserInfo, updateUserInfo } from '@/api/user'
import { onMounted, ref } from 'vue'
import { userUploadFile } from '@/api/questionnaire'
import {UserFilled,Message,ChatSquare} from '@element-plus/icons-vue'
const userInfo = ref(null);         // 存储用户信息
const newPassword = ref('');        // 存储新密码
const confirmPassword = ref('');    // 存储确认密码
const newAvatar = ref('');          // 存储新头像 URL
const time=ref('')   
const centerDialogVisible = ref(false)
const newDescription = ref('')
const isHovered = ref(false); // 用于跟踪鼠标悬停状态
onMounted(async () => {
  try {
    const res = await getUserInfo()
    console.log("res",res)
    if (res.msg === 'success') {
      userInfo.value = res.data
      newDescription.value=userInfo.value.description
      const currentTime = new Date();
      const createTime = new Date(userInfo.value.createTime);
      time.value =Math.floor(  (currentTime - createTime) / (1000 * 60 * 60 * 24));  
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    console.error("获取用户信息出错：", error)
    ElMessage.error('获取用户信息时发生错误')
  }
})
// 监听 newDescription 的变化并更新 userInfo.description
const updateDescription = async() => {
  userInfo.value.description = newDescription.value;
  const res1 = await updateUserInfo(newPassword.value, newAvatar.value,newDescription.value)
  console.log("newDescription.value",newDescription.value)
  const res2 = await getUserInfo()
  console.log("res1",res1)
  console.log("res2",res2)
};

//模拟点击一个文件输入元素，以便用户可以选择文件。
const triggerFileInput = () => {
  //使用 document.querySelector 方法查找页面中第一个匹配 input[type="file"] 的元素，并将其赋值给 fileInput 变量。这个选择器用于获取文件输入框。
  const fileInput = document.querySelector('input[type="file"]');
  //模拟点击：调用 fileInput 的 click() 方法，模拟用户点击该文件输入元素。这将打开文件选择对话框，允许用户选择文件。
  fileInput.click();
};

const handleFileChange = async (index, event) => {
  const file = event.target.files[0]
  console.log('选择的文件:', file)
  if (file) {
    const res = await userUploadFile(file)
    newAvatar.value = res.data
    const res1 = await updateUserInfo(newPassword.value, newAvatar.value,newDescription.value)
    const res2 = await getUserInfo()
    ElMessage.success('上传成功')
  } else {
    elMessage.error('上传失败')
  }
}

const handleUpdateUserInfo = async () => {
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  try {
    console.log(newAvatar.value)
    const res = await updateUserInfo(newPassword.value, newAvatar.value,newDescription.value)
    if (res.msg === 'success') {
      ElMessage.success('更新用户信息成功')
      const res = await getUserInfo()
      // console.log("res",res)
      if (res.msg === 'success') {
        userInfo.value = res.data
      } else {
        ElMessage.error('获取用户信息失败')
      }
    } else {
      ElMessage.error('更新用户信息失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error('更新用户信息失败')
  }
}
</script>

<template>
  <div v-if="userInfo" style="margin-top: 30px;">
    <div class="common-layout" >
      <el-container >
        <div style="margin-right: 430px;"></div>
        <el-aside width="100px" >
          <img :src="userInfo.avatar" alt="用户头像" @click="triggerFileInput"/>
          <input type="file" ref="fileInput" @change="(event) => handleFileChange(index, event)" style="display: none;" />
        </el-aside>
        <el-container>
            <el-header height="30px" style="margin-top: 10px;">{{ userInfo.username }}</el-header>
            <el-main  >这是你来Survey Planet的第 {{ time }}天！</el-main>
        </el-container>
      </el-container>
    </div>  
    <div style="margin-left: 450px; margin-top: 30px; border-radius: 10px;"><el-icon><UserFilled /></el-icon>&nbsp;用&nbsp;&nbsp;户&nbsp;&nbsp;名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; {{ userInfo.username }}</div>    
    <div style="margin-left:450px; margin-top: 30px;"><el-icon><Message /></el-icon>&nbsp;邮&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;箱&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{{ userInfo.email }}</div>
    <div style="margin-left: 450px; margin-top: 30px;">      
        <div class="textarea-container" @mouseover="isHovered = true" @mouseleave="isHovered = false">
          <el-icon><ChatSquare /></el-icon>&nbsp;个人描述&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <el-input style="width: 400px;" v-model="newDescription" placeholder="请输入个人简介"></el-input>&nbsp;&nbsp;
          <el-button style="" class="edit-button" v-if="isHovered">编辑</el-button>
          <el-button style="" class="edit-button" v-if="isHovered" @click="updateDescription">保存</el-button>
        </div>
    </div>
  </div>
  <!-- 修改密码和头像的表单 -->
  <div>
    <!--选择文件按钮-->
    <!-- <input type="file" @change="(event) => handleFileChange(index, event)" /> -->
    <el-button plain @click="centerDialogVisible = true" style="margin-left: 550px; margin-top: 30px;">
      更改密码
    </el-button>
    <el-dialog
    v-model="centerDialogVisible"
    title="更改密码"
    width="500"
    align-center
    >
    <h3>修改用户信息</h3>
    <el-form>
        <el-form-item label="新密码&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;">
          <el-input
            v-model="newPassword"
            placeholder="输入新密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input
            v-model="confirmPassword"
            placeholder="再次输入新密码"
            show-password
          ></el-input>
        </el-form-item>
      
        <el-form-item>
          <el-button type="primary" @click="handleUpdateUserInfo" style="margin-left: 200px;">
            更改密码
          </el-button>
        </el-form-item>
      </el-form>
    
    </el-dialog>

      
  </div>



</template>

<style scoped>
p {
  margin: 0 0 10px;
}
img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
}
.textarea-container {
  position: relative; /* 使按钮相对于容器定位 */
  display: flex; /* 使用 flexbox 布局 */
  align-items: center; /* 垂直居中对齐 */
}

.edit-button {
  display: inline-block; /* 使按钮在 textarea 下方显示 */
  margin-top: 0px; /* 添加一些间距 */
}
@font-face {
  font-family: DancingScript_Regular;
  font-weight: normal;
  src: url(@/assets/font/DancingScript-Regular.ttf) format("truetype");
  text-rendering: optimizeLegibility;
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
