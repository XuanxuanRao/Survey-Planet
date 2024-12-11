<script setup>

import { ref, onMounted } from 'vue'
import { userGetFilledQuestionnaireList } from '@/api/questionnaire'
import router from "@/router";

const filledQuestionnaireList = ref([]);  // 响应式变量，存储用户数据

onMounted(async () => {
    const res = await userGetFilledQuestionnaireList();
    filledQuestionnaireList.value = res.data;
    console.log("filledQuestionnaireList.value",filledQuestionnaireList.value)
})

const checkQuestionnaire = (rid) => {
    console.log("id",rid)
    router.push({ path: '/viewResult' , query: { rid: rid } });
}

</script>

<template>

    <div class="box">
        <div class="header">
            <h1 style="color: black;">历史记录</h1>
        </div>
          <div class="showQues">
            <ul class="infinite-list" style="overflow: auto">
            <li v-for="(questionnaire, index) in filledQuestionnaireList.records" :key="index" class="infinite-list-item">
                <div class="name-description">
                    <div class="left-content">
                    <span class="title">{{ questionnaire.title }}</span>
                    </div>
                    <div class="right-content">
                        问卷描述：{{ questionnaire.description }}
                        {{ questionnaire.state }}
                        {{ questionnaire.type }}
                        <el-button type="primary" @click="checkQuestionnaire(questionnaire.rid)">查看填写结果</el-button>
                    </div>
                </div>
            </li>
            </ul>
        </div>
    </div>
</template>

<style>
.box {
  margin-left: 0px; /* 给侧边栏留出空间 */
  margin-right: 10px; /* 给侧边栏留出空间 */
  margin-top: 20px;
  height: 90vh;
  background-color: #f0f0f0; 
  border: 1px solid #ccc; 
  border-radius: 5px; 
  padding: 15px; /* 内边距 */
  box-shadow: 0 5px 10px rgba(34, 30, 30, 0.877);
} 

.showQues {
  margin-top: 20px;
}
.left-content {
  flex: 1;
  width: 250px; /* 设置固定宽度 */
  text-align: left;
  white-space: nowrap; /* 防止内容换行 */
}

.right-content {
  flex: 1;
  width: 400px; /* 设置固定宽度 */
  text-align: right;
  white-space: nowrap; /* 防止内容换行 */
}
.infinite-list {
  border-radius: 10px;
  background-color: white;
  height: 65vh;
  /* width: 80vh; */
  padding: 20px;
  margin: 0;
  list-style: none;
}
.infinite-list .infinite-list-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  background: lightblue; /* 背景颜色 */
  margin: 10px;
  /* background: url('@/assets/img/2.jpg') no-repeat center center;
  background-size: cover;
  z-index: 20; */
}

.name-description {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color:black;
  
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