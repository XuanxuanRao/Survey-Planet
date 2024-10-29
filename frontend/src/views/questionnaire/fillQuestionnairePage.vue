<script setup>

import { ref, onMounted } from 'vue'
import { userGetFilledQuestionnaireList } from '@/api/questionnaire'

const filledQuestionnaireList = ref([]);  // 响应式变量，存储用户数据

onMounted(async () => {
    const res = await userGetFilledQuestionnaireList();
    filledQuestionnaireList.value = res.data;
})

</script>

<template>

    <div class="box">
        <div class="header">
            <h1>填写问卷</h1>
        </div>
          <div class="showQues">
            <ul class="infinite-list" style="overflow: auto">
            <li v-for="(questionnaire, index) in filledQuestionnaireList" :key="index" class="infinite-list-item">
                <div class="name-description">
                    <div class="left-content">
                    <span class="title">{{ questionnaire.title }}</span>
                    </div>
                    <div class="right-content">
                        问卷描述：{{ questionnaire.description }}
                        {{ questionnaire.state }}
                        {{ questionnaire.type }}
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

.infinite-list {
  border-radius: 10px;
  background-color: white;
  height: 65vh;
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
  background: wheat; /* 背景颜色 */
  margin: 10px;
  /* color: white; 字体颜色 */
}

.name-description {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color:black;
}

</style>