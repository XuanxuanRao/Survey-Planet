<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, onBeforeRouteLeave } from 'vue-router'
import { userFillQuestionnaire, userSubmitQuestionnaire, userUploadFile } from '@/api/questionnaire'
import router from '@/router';

const route = useRoute()
const code = route.params.code

const questionnaireTitle = ref('')
const questionnaireId = ref('')
const type = ref('')
const questions = ref([])
const answers = ref([])

// 获取问卷数据
onMounted(async () => {
  try {
    const res = await userFillQuestionnaire(code)
    if (res.data) {
      questionnaireTitle.value = res.data.title
      questionnaireId.value = res.data.sid
      type.value = res.data.type
      questions.value = res.data.questions

      answers.value = res.data.questions.map(q => ({
        qid: q.qid,
        content: [] 
      }))
    }
  } catch (error) {
    console.error('获取问卷失败', error)
    ElMessage.error('加载问卷失败，请稍后重试')
  }
})

const handleFileChange = (index, event) => {
  const file = event.target.files[0]
  console.log('选择的文件:', file)
  if (file) {
    answers.value[index].content = [file]
  } else {
    answers.value[index].content = []
  }
}

// 检查必填项
const validateSurvey = () => {
  for (let i = 0; i < questions.value.length; i++) {
    const question = questions.value[i]
    const answer = answers.value[i].content

    // 如果问题是必填项，并且没有回答，则返回 false
    if (question.required && (!answer || answer.length === 0)) {
      ElMessage.warning(`问题 ${i + 1} 是必填项，请完成后再提交`)
      return false
    }
  }
  return true
}

// 提交问卷
const submitSurvey = async () => {
  if (!validateSurvey()) return  // 如果必填项未通过检查，则不提交
  try {
    const surveyAnswers = []
    for (let i = 0; i < questions.value.length; i++) {
      const question = questions.value[i]
      if (question.type !== 'file' && question.type !== 'code') {
        surveyAnswers.push({
          qid: question.qid,
          content: answers.value[i].content
        })
      } else {
        const file = answers.value[i].content[0]
        if (file) {
          const uploadRes = await userUploadFile(file)
          console.log('上传文件结果:', uploadRes.data)
          if (question.type === 'code' && !answers.value[i].content[1]) {
            console.log('请选择编程语言')
            ElMessage.error('请选择编程语言')
            return
          }
          surveyAnswers.push({
            qid: question.qid,
            content: [uploadRes.data, answers.value[i].content[1]]
          })
        }
      }
    }

    const res = await userSubmitQuestionnaire(questionnaireId.value, surveyAnswers)
    if (res.msg === 'success') {
      ElMessage.success('提交成功')
      isCommit.value = true
      console.log(res.data)
      if(type.value === 'exam') {
        router.push({ path: '/viewResult', query: { rid: res.data } })
      } else if(type.value) {
        router.push('/questionnaire/CreateQuestionnaire')
      }
    } else {
      ElMessage.error('提交失败，请稍后重试')
    }
  } catch (error) {
    console.error('提交问卷失败', error)
    ElMessage.error('提交失败，请稍后重试')
  }
}
const isCommit = ref(false)  // 是否保存问卷

watch(answers, () => {
  isCommit.value = false
  console.log('回答发生改变', questions.value)
}, { deep: true })

// 路由离开时提示用户（仅对当前页面生效）
onBeforeRouteLeave((to, from, next) => {
    if (!isCommit.value) {
        ElMessageBox.confirm('当前回答未提交或发生改变，是否确定要离开？', '提示', {
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

</script>

<template>
  <div>
    <h2>{{ questionnaireTitle }}</h2>

    <div class="question-list">
      <div v-for="(question, index) in questions" :key="index" class="question-item">
        <template v-if="question.type === 'single_choice'">
          <h4>
            <span v-if="question.required" class="required-marker">*</span> <!-- 必填标识符 -->
            Q{{ index + 1 }} 
            {{ question.title }} (单选题) <br>
            问题描述：{{ question.description }}
          </h4>
          <el-radio-group v-model="answers[index].content[0]">
            <el-radio v-for="(option, optIndex) in question.options" :key="optIndex" :value="option">{{ option }}</el-radio>
          </el-radio-group>
          <div v-if="type === 'exam' && question.score !== null">分数:{{ question.score }}</div>
        </template>

        <template v-if="question.type === 'multiple_choice'">
          <h4>
            <span v-if="question.required" class="required-marker">*</span> <!-- 必填标识符 -->
            Q{{ index + 1 }} 
            {{ question.title }} (多选题) <br>
            问题描述：{{ question.description }}
          </h4>
          <el-checkbox-group v-model="answers[index].content">
            <el-checkbox v-for="(option, optIndex) in question.options" :key="optIndex" :value="option">{{ option }}</el-checkbox>
          </el-checkbox-group>
          <div v-if="type === 'exam' && question.score !== null">分数:{{ question.score }}</div>
        </template>

        <template v-if="question.type === 'fill_blank'">
          <h4>
            <span v-if="question.required" class="required-marker">*</span> <!-- 必填标识符 -->
            Q{{ index + 1 }} 
            {{ question.title }} <br>
            问题描述：{{ question.description }}
          </h4>
          <el-input v-model="answers[index].content[0]" type="textarea" placeholder="请输入您的答案" />
          <div v-if="type === 'exam' && question.score !== null">分数:{{ question.score }}</div>
        </template>

        <template v-if="question.type === 'file'">
          <h4>
            <span v-if="question.required" class="required-marker">*</span> <!-- 必填标识符 -->
            Q{{ index + 1 }} 
            {{ question.title }} <br>
            问题描述：{{ question.description }}
          </h4>
          <input type="file" @change="(event) => handleFileChange(index, event)" />
          <div class="el-upload__tip">文件最大为 {{ question.maxFileSize }} MB</div>
        </template>

        <template v-if="question.type === 'code'">
          <h4>
            <span v-if="question.required" class="required-marker">*</span> <!-- 必填标识符 -->
            Q{{ index + 1 }} 
            {{ question.title }} <br>
            问题描述：{{ question.description }}
          </h4>
          <input type="file" @change="(event) => handleFileChange(index, event)" /> <br>
            <!-- 多选编程语言 -->
          <el-select v-model="answers[index].content[1]" placeholder="请选择编程语言" style="width: 300px; margin-bottom: 10px;">
            <el-option
              v-for="(language, langIndex) in question.languages"
              :key="langIndex"
              :label="language"
              :value="language"
            />
          </el-select>
          
          <div v-if="type === 'exam' && question.score !== null">分数:{{ question.score }}</div>
        </template>

      </div>
    </div>

    <el-button type="primary" @click="submitSurvey">提交问卷</el-button>
  </div>
</template>

<style scoped>
.question-list {
  margin-top: 20px;
}
.question-item {
  margin-bottom: 20px;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  background-color: #fafafa;
}
.required-marker {
  color: red;
  margin-right: 5px;
}
</style>
