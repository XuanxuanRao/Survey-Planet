<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router';
import { Back } from '@element-plus/icons-vue';
import { userSendQuestionnaireList } from '@/api/questionnaire';

const route = useRoute();

const questionnaireTitle = route.query.title
const questionnaireType = route.query.type

const questions = ref([])  // 存储所有问题的数组
const surveyData = ref([]) // 存储最终问卷数据的数组

// 添加问题的函数，新的问题添加到数组的最上方
const addQuestion = (type) => {
  const newQuestion = {
    type: type,
    title: '',  // 问题内容可编辑
    description: '', // 问题描述
    options: type === 'single' || type === 'multiple' 
      ? ['选项1', '选项2'] // 默认两个选项
      : [],  // 非选择题没有选项
    required: false,  // 是否必填
  }
  questions.value.unshift(newQuestion)  // 使用 unshift 将新问题添加到数组的最前面
}

// 删除问题
const removeQuestion = (index) => {
  questions.value.splice(index, 1); // 根据索引删除问题
}

// 添加选项
const addOption = (question) => {
  question.options.push('新选项')
}

// 删除选项
const removeOption = (question, index) => {
  question.options.splice(index, 1)
}

// 上移问题
const moveUp = (index) => {
  if (index > 0) {
    const temp = questions.value[index]
    questions.value.splice(index, 1)  // 删除当前问题
    questions.value.splice(index - 1, 0, temp)  // 插入到上一个位置
  }
}

// 下移问题
const moveDown = (index) => {
  if (index < questions.value.length - 1) {
    const temp = questions.value[index]
    questions.value.splice(index, 1)  // 删除当前问题
    questions.value.splice(index + 1, 0, temp)  // 插入到下一个位置
  }
}


const isSave = ref(false)  // 是否保存问卷
watch(questions.value, () => {
  isSave.value = false
//   console.log('问题列表发生变化:', newVal)
})

// 保存问卷
const saveSurvey = async() => {
  if(questions.value.length === 0) {
    ElMessage.warning('请添加问题')
    return
  }
  surveyData.value = [...questions.value] // 将当前问卷的问题和选项保存到 surveyData
  console.log(questionnaireTitle)
  console.log(questionnaireType)
  console.log('问卷数据已保存:', surveyData.value)
  isSave.value = true
  const res = await userSendQuestionnaireList(questionnaireType, 
                                        questionnaireTitle,
                                        '', 
                                        surveyData.value)
  console.log(res)
}

const router = useRouter()
const backLastPage = () => {
  if (isSave.value === false) {
    ElMessageBox.confirm('问卷尚未保存，确定要返回吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.go(-1)
    }).catch(() => {
      console.log('取消返回')
    })
  } else {
    router.go(-1)
  }
}
</script>


<template>
  <div>
    <el-icon @click="backLastPage"><Back /></el-icon>
    <!-- 由上级传递 -->
    <h2>问卷名称</h2>

    <h3>点击添加问题</h3>
    <!-- 问题类型按钮 -->
    <div class="question-types">
      <button @click="addQuestion('single')">单选题</button>
      <button @click="addQuestion('multiple')">多选题</button>
      <button @click="addQuestion('text')">文本题</button>
      <button @click="addQuestion('rating')">打分题</button>
    </div>
    
    <!-- 显示添加的问题 -->
    <div class="question-list">
      <div v-for="(question, index) in questions" :key="index" class="question-item">
        <div class="question-header">
          <!-- 上移、下移按钮 -->
          <button @click="moveUp(index)" v-if="index !== 0">上移</button>
          <button @click="moveDown(index)" v-if="index !== questions.length - 1">下移</button>
          <!-- 删除问题按钮 -->
          <button @click="removeQuestion(index)">删除问题</button>
          <!-- 是否必填按钮 -->
          <button @click="question.required = true">是否必填</button>
        </div>
        
        <!-- 根据问题类型渲染不同的题目 -->
        <template v-if="question.type === 'single' || question.type === 'multiple'">
          <h4>Q{{ index + 1 }} {{ question.type === 'single' ? '单选题' : '多选题' }} </h4>
          <div>
            <input v-model="question.title" placeholder="请输入问题内容" />
          </div>
          <ul>
            <li v-for="(option, optIndex) in question.options" :key="optIndex">
              <input v-model="question.options[optIndex]" placeholder="选项内容" />
              <button @click="removeOption(question, optIndex)">删除选项</button>
            </li>
          </ul>
          <button @click="addOption(question)">添加选项</button>
          <textarea v-model="question.description" placeholder="请填入问题描述"></textarea>
        </template>

        <template v-if="question.type === 'text'">
          <h4>Q{{ index + 1 }}文本题 </h4>
          <div>
            <input v-model="question.title" placeholder="请输入问题内容" />
          </div>
          <textarea placeholder="请输入回答"></textarea>
          <textarea v-model="question.description" placeholder="请填入问题描述"></textarea>
        </template>

        <template v-if="question.type === 'rating'">
          <h4>Q{{ index + 1 }}打分题 </h4>
          <div>
            <input v-model="question.title" placeholder="请输入问题内容" />
          </div>
          <div>
            <span v-for="n in 5" :key="n">★</span>
          </div>
          <textarea v-model="question.description" placeholder="请填入问题描述"></textarea>
        </template>
      </div>
    </div>

    <!-- 保存按钮 -->
    <button @click="saveSurvey">保存问卷</button>

    <!-- 显示存储的问卷信息 -->
    <div class="survey-data">
      <h3>问卷数据</h3>
      <pre>{{ surveyData }}</pre>
    </div>
  </div>
</template>

<style scoped>
.question-types {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

button {
  padding: 10px 20px;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: #e0e0e0;
}

.question-list {
  margin-top: 20px;
}

.question-item {
  margin-bottom: 20px;
  background-color: antiquewhite;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.survey-data {
  margin-top: 20px;
}
</style>
