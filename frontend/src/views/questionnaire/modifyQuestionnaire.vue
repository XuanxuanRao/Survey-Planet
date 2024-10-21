<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { Back, Edit, CircleCheckFilled, HelpFilled, EditPen, Checked, ArrowDownBold, ArrowUp, CloseBold, Plus, QuestionFilled, RemoveFilled, Tickets, Check } from '@element-plus/icons-vue'
import { userGetQuestionnaire, userModifyQuestionnaireList } from '@/api/questionnaire' // 获取和修改问卷的API

const route = useRoute()
const router = useRouter()

const title = ref('')  // 问卷标题
const type = ref('')  // 问卷类型
const qusetionnaireId = ref(route.query.id) // 从路由传递问卷 ID
const questions = ref([])  // 存储所有问题的数组
const surveyData = ref([]) // 存储最终问卷数据的数组

// 获取已有问卷数据并初始化
onMounted(async () => {
    const res = await userGetQuestionnaire(qusetionnaireId.value)
    console.log('问卷数据:', res.data)
    questions.value = res.data.questions
    title.value = res.data.title
    type.value = res.data.type
})

// 添加问题的函数，新的问题添加到数组的最上方
const addQuestion = (type) => {
  const newQuestion = {
    type: type,
    title: '',
    description: '',
    maxFileSize: 20,
    options: type === 'single_choice' || type === 'multiple_choice' ? ['选项1', '选项2'] : [],
    required: false,
  }
  questions.value.push(newQuestion)
}

// 删除问题
const removeQuestion = (index) => {
  questions.value.splice(index, 1)
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
    questions.value.splice(index, 1)
    questions.value.splice(index - 1, 0, temp)
  }
}

// 下移问题
const moveDown = (index) => {
  if (index < questions.value.length - 1) {
    const temp = questions.value[index]
    questions.value.splice(index, 1)
    questions.value.splice(index + 1, 0, temp)
  }
}

const isSave = ref(true)  // 是否保存问卷

// 监控问题变化
watch(questions, () => {
  isSave.value = false;
}, { deep: true });

// 保存修改后的问卷
const saveSurvey = async () => {
  if (questions.value.length === 0) {
    ElMessage.warning('请添加问题')
    return
  }
  surveyData.value = [...questions.value]
  try {
    const res = await 
    userModifyQuestionnaireList(qusetionnaireId.value, type.value, title.value, '', surveyData.value)
    console.log('问卷修改成功:', res)
    isSave.value = true
    ElMessage.success('问卷已保存')
  } catch (error) {
    console.error('保存问卷失败', error)
    ElMessage.error('保存失败，请重试')
  }
}


// 路由离开时提示用户（仅对当前页面生效）
onBeforeRouteLeave((to, from, next) => {
    if (!isSave.value) {
        ElMessageBox.confirm('当前问卷尚未保存，是否确定要离开？', '提示', {
            confirmButtonText: '离开',
            cancelButtonText: '取消',
            type: 'warning',
        }).then(() => {
            next() // 用户确认离开，允许路由跳转
        }).catch(() => {
            next(false) // 用户取消，阻止路由跳转
        })
    } else {
        next() // 已保存，允许路由跳转
    }
})

// 返回上一页的函数
const backLastPage = () => {
    if (!isSave.value) {
        ElMessageBox.confirm('问卷尚未保存，确定要返回吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            router.go(-1)  // 确认离开
        }).catch(() => {
            // 用户取消，阻止返回上一页
            console.log('取消返回')
        })
    } else {
        router.go(-1) // 已保存的情况下直接返回上一页
    }
}

</script>

<template>
  <div>
    <h2>{{ title }}</h2>
    
    <div class="question-list">
      <div v-for="(question, index) in questions" :key="index" class="question-item">
        <div class="question-header">
          <button @click="moveUp(index)" v-if="index !== 0"><el-icon><ArrowUp /></el-icon>上移</button>
          <button @click="moveDown(index)" v-if="index !== questions.length - 1"><el-icon><ArrowDownBold /></el-icon>下移</button>
          <button @click="removeQuestion(index)"><el-icon><CloseBold /></el-icon>删除问题</button>
          <button @click="question.required = !question.required"><el-icon><QuestionFilled /></el-icon>{{ question.required ? '必填' : '非必填' }}</button>
        </div>
        
        {{ question }}
        <template v-if="question.type === 'single_choice' || question.type === 'multiple_choice'">
          <h4>Q{{ index + 1 }} {{ question.type === 'single_choice' ? '单选题' : '多选题' }}</h4>
          <el-input v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
          <el-input v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
          <ul>
            <li v-for="(option, optIndex) in question.options" :key="optIndex">
              <el-input v-model="question.options[optIndex]" placeholder="选项内容" />
              <el-button @click="removeOption(question, optIndex)"><el-icon><RemoveFilled /></el-icon>删除选项</el-button>
            </li>
          </ul>
          <el-button @click="addOption(question)"><el-icon><Plus /></el-icon>添加选项</el-button>
        </template>

        <template v-if="question.type === 'fill_blank'">
          <h4>Q{{ index + 1 }}填空题</h4>
          <el-input v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
          <el-input v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
        </template>

        <template v-if="question.type === 'file'">
          <h4>Q{{ index + 1 }}文件上传题</h4>
          <el-input v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
          <el-input v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
        </template>
      </div>
    </div>

    <h3>点击添加问题</h3>
    <div class="question-types">
      <button @click="addQuestion('single_choice')"><el-icon><HelpFilled /></el-icon>单选题</button>
      <button @click="addQuestion('multiple_choice')"><el-icon><CircleCheckFilled /></el-icon>多选题</button>
      <button @click="addQuestion('fill_blank')"><el-icon><Edit /></el-icon>填空题</button>
      <button @click="addQuestion('file')"><el-icon><Edit /></el-icon>文件上传题</button>
    </div>

    <button @click="saveSurvey"><el-icon><Checked /></el-icon>保存问卷</button>

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
}

button:hover {
  background-color: #e0e0e0;
}

.question-list {
  margin-top: 20px;
}

.question-item {
  margin-bottom: 20px;
  background-color: white;
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
