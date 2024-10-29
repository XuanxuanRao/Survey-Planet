<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { userGetQuestionnaire } from '@/api/questionnaire' // 获取问卷的API

const route = useRoute()

const title = ref('')  // 问卷标题
const type = ref('')  // 问卷类型
const qusetionnaireId = ref(route.query.id) // 从路由传递问卷 ID
const questions = ref([])  // 存储所有问题的数组

// 获取已有问卷数据并初始化
onMounted(async () => {
    const res = await userGetQuestionnaire(qusetionnaireId.value)
    console.log('问卷数据:', res.data)
    questions.value = res.data.questions
    title.value = res.data.title
    type.value = res.data.type
})

</script>

<template>
  <div>
    <h2>{{ title }}</h2>

    <div class="question-list">
      <div v-for="(question, index) in questions" :key="index" class="question-item">
        <div class="question-header">
          <h4>Q{{ index + 1 }} 
            <template v-if="question.type === 'single_choice'">单选题</template>
            <template v-if="question.type === 'multiple_choice'">多选题</template>
            <template v-if="question.type === 'fill_blank'">填空题</template>
            <template v-if="question.type === 'file'">文件上传题</template>
            <template v-if="question.type === 'code'">代码题</template>
            <template v-if="question.required === true"> 必填</template>
          </h4>
        </div>
        
        <!-- 问题内容 -->
        <p><strong>问题标题:</strong> {{ question.title }}</p>
        <p><strong>问题描述:</strong> {{ question.description }}</p>
        
        <!-- 选项展示 -->
        <template v-if="question.type === 'single_choice' || question.type === 'multiple_choice'">
          <ul>
            <li v-for="(option, optIndex) in question.options" :key="optIndex">
              选项 {{ optIndex + 1 }}: {{ option }}
            </li>
          </ul>
          <p>答案:{{ question.answer }}</p>
          <p v-if="question.score !== null"> 分值:{{ question.score }}</p>
        </template>

        <!-- 填空题和文件上传题无需选项展示 -->
        <template v-if="question.type === 'fill_blank'">
          <p>答案:{{ question.answer }}</p>
          <p v-if="question.score !== null"> 分值:{{ question.score }}</p>
        </template>

        <template v-if="question.type === 'file'">
          <p>这是一个文件上传题。</p>
        </template>

        <template v-if="question.type === 'code'">
          <p v-if="question.score !== null"> 分值:{{ question.score }}</p>
        </template>
      </div>
    </div>

    <!-- 问卷数据展示 -->
    <div class="survey-data">
      <h3>问卷数据</h3>
      <pre>{{ questions }}</pre>
    </div>
  </div>
</template>

<style scoped>
.question-list {
  margin-top: 20px;
}

.question-item {
  margin-bottom: 20px;
  background-color: white;
}

.question-header {
  margin-bottom: 10px;
}

.survey-data {
  margin-top: 20px;
}
</style>
