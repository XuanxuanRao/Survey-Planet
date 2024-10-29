<script setup>
import { getUserInfo } from '@/api/user'
import { onMounted, ref } from 'vue';

const userInfo = ref()

onMounted(async () => {
  const res = await getUserInfo();
  if (res.msg === 'success') {
    userInfo.value = res.data
  } else {
    el.Message.error('获取用户信息失败')
  }
  console.log(userInfo.value)
})



</script>

<template>
  <div v-if="userInfo">
    <img :src="userInfo.avatar" alt="用户头像" />
    <p>用户名: {{ userInfo.username }}</p>
    <p>邮箱: {{ userInfo.email }}</p>
    <p>创建时间: {{ userInfo.createTime }}</p>
    
  </div>
  <div v-else>
    <p>加载中...</p>
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

