<script setup>
import { getUserInfo, updateUserInfo } from '@/api/user'
import { onMounted, ref } from 'vue'
import { userUploadFile } from '@/api/questionnaire'

const userInfo = ref(null);         // 存储用户信息
const newPassword = ref('');        // 存储新密码
const confirmPassword = ref('');    // 存储确认密码
const newAvatar = ref('');          // 存储新头像 URL

onMounted(async () => {
  try {
    const res = await getUserInfo()
    console.log(res)
    if (res.msg === 'success') {
      userInfo.value = res.data
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    console.error("获取用户信息出错：", error)
    ElMessage.error('获取用户信息时发生错误')
  }
})

const handleFileChange = async (index, event) => {
  const file = event.target.files[0]
  console.log('选择的文件:', file)
  if (file) {
    const res = await userUploadFile(file)
    newAvatar.value = res.data
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
    const res = await updateUserInfo(newPassword.value, newAvatar.value)
    if (res.msg === 'success') {
      ElMessage.success('更新用户信息成功')
      const res = await getUserInfo()
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
  <div v-if="userInfo">
    <img :src="userInfo.avatar" alt="用户头像" />
    <p>用户名: {{ userInfo.username }}</p>
    <p>邮箱: {{ userInfo.email }}</p>
    <p>创建时间: {{ userInfo.createTime }}</p>
  </div>

  <!-- 修改密码和头像的表单 -->
  <div>
    <h3>修改用户信息</h3>
      <el-form>
        <el-form-item label="新密码">
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
        <!--选择文件按钮-->
        <input type="file" @change="(event) => handleFileChange(index, event)" />
        <el-form-item>
          <el-button type="primary" @click="handleUpdateUserInfo">
            更新信息
          </el-button>
        </el-form-item>
      </el-form>
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
</style>
