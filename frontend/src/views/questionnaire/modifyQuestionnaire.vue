<script setup>
import { onMounted, ref, watch, nextTick, computed } from 'vue'
import { useRoute, onBeforeRouteLeave, useRouter } from 'vue-router'
import { Edit, CircleCheckFilled, HelpFilled, EditPen, Checked, ArrowDownBold, ArrowUp, CloseBold, Plus, QuestionFilled, RemoveFilled, Tickets, Check } from '@element-plus/icons-vue'
import { userGetQuestionnaire, userModifyQuestionnaireList } from '@/api/questionnaire' // 获取和修改问卷的API
import { useUserStore } from "@/stores/user"

const route = useRoute()
const router = useRouter()

const title = ref('')  // 问卷标题
const type = ref('')  // 问卷类型
const qusetionnaireId = ref(route.query.id) // 从路由传递问卷 ID
const questions = ref([])  // 存储所有问题的数组
const surveyData = ref([]) // 存储最终问卷数据的数组
const isOpen = ref(false)  // 问卷是否已发布

// 获取已有问卷数据并初始化
onMounted(async () => {
    const res = await userGetQuestionnaire(qusetionnaireId.value)
    console.log('问卷数据:', res.data)
    if(res.data.openTime !== null) {
        ElMessageBox.confirm(
          '问卷已发布，无法修改。是否返回上一页?', 
          '提示', 
          {
            confirmButtonText: '返回',
            cancelButtonText: '取消',
            type: 'warning',
          }
        ).then(() => {
          router.go(-1);
        }).catch(() => {
          isOpen.value = true
        })
    }
    questions.value = res.data.questions
    title.value = res.data.title
    type.value = res.data.type
    console.log(type.value)
    await nextTick()
    isSave.value = true
})

// 添加问题的函数，新的问题添加到数组的最上方
const addQuestion = (type) => {
  if (type === "code") {
        const newQuestion = {
        type: type,
        title: '',  // 问题内容可编辑
        description: '', // 问题描述
        options: type === 'single_choice' || type === 'multiple_choice' 
        ? ['选项1', '选项2'] // 默认两个选项
        : [],  // 非选择题没有选项
        required: true,  // 是否必填
        score: null,        // 分数，默认为 null
        timeLimit: 2000,
        memoryLimit: 512,
        stackLimit: 256,
        languages: [],
        inputFileUrls: [],
        outputFileUrls: [],
    }
    questions.value.push(newQuestion)
  } else {
    const newQuestion = {
        type: type,
        title: '',  // 问题内容可编辑
        description: '', // 问题描述
        options: type === 'single_choice' || type === 'multiple_choice' 
        ? ['选项1', '选项2'] // 默认两个选项
        : [],  // 非选择题没有选项
        required: true,  // 是否必填
        score: null,        // 分数，默认为 null
        answer: [],         // 答案，默认为空数组
    }
    questions.value.push(newQuestion)
  }
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

// 监控问题变化，避免初始加载时的触发
watch(
  questions,
  () => {
    isSave.value = false
  },
  { deep: true, immediate: false }
)

// 保存修改后的问卷
const saveSurvey = async () => {
  if (questions.value.length === 0) {
    ElMessage.warning('请添加问题')
    return
  }

  // 检查选择题是否至少有一个选项
  for (const question of questions.value) {
    if ((question.type === 'single_choice' || question.type === 'multiple_choice') && question.options.length === 0) {
      ElMessage.error('选择题必须至少有一个选项')
      return
    }
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

// 错误处理
const handleUploadError = (error) => {
    console.log('上传文件失败:', error);
  ElMessage.error(`文件上传失败：${error.message}`);
}

// 预上传钩子
const beforeUploadIn = (file) => {
  const isFileTypeValid = file.name.endsWith('.in');
  if (!isFileTypeValid) {
    ElMessage.error('只能上传 .in 文件');
  }
  return isFileTypeValid;
}

const beforeUploadOut = (file) => {
  const isFileTypeValid = file.name.endsWith('.out');
  if (!isFileTypeValid) {
    ElMessage.error('只能上传 .out 文件');
  }
  return isFileTypeValid;
}

// 处理上传成功的回调（.in 文件）
const handleUploadSuccessIn = (question) => {
  return async (response) => {
    try {
      console.log('上传文件结果in:', response.data);
      question.inputFileUrls.push(response.data); // 将接口返回的data填入inputFileUrls
      ElMessage.success(`上传成功！`);
    } catch (error) {
      ElMessage.error(`上传失败：${error.message}`);
    }
  }
}

// 处理上传成功的回调（.out 文件）
const handleUploadSuccessOut = (question) => {
  return async (response) => {
    try {
      console.log('上传文件结果out:', response.data);
      question.outputFileUrls.push(response.data); // 将接口返回的data填入outputFileUrls
      ElMessage.success(`上传成功！`);
    } catch (error) {
      ElMessage.error(`上传失败：${error.message}`);
    }
  }
}

// 删除文件的处理函数
const handleFileRemove = (file, question, type) => {
  const fileArray = type === 'input' ? question.inputFileUrls : question.outputFileUrls;
  const fileIndex = fileArray.findIndex(item => item === file.response.data);  // 根据 URL 查找索引
      
  if (fileIndex !== -1) {
    fileArray.splice(fileIndex, 1);  // 根据索引位置删除对应的 URL
    console.log(`已删除文件: ${file.name}，位于索引 ${fileIndex}`);
    ElMessage.info(`文件已删除`);
  } else {
    ElMessage.error('未找到文件');
  }
}


// 定义计算属性来返回请求头
const uploadHeaders = computed(() => {
  const userStore = useUserStore(); // 获取用户状态
  return {
    'token': userStore.token  // 设置 token 头
  }
})

// 设置答案
const setAnswer = (question, option) => {
  if (question.type === 'single_choice') {
    question.answer = [option]  // 单选题只允许一个答案
  } else if (question.type === 'multiple_choice') {
    const answerIndex = question.answer.indexOf(option)
    if (answerIndex === -1) {
      question.answer.push(option)  // 添加答案
    } else {
      question.answer.splice(answerIndex, 1)  // 取消答案
    }
  }
}

</script>

<template>
  <div>
    
    
    <div class="question-list">
      <h2>{{ title }}</h2>
      <div v-for="(question, index) in questions" :key="index" class="question-item">
        <template v-if="question.type === 'single_choice' || question.type === 'multiple_choice'">
          <h4>Q{{ index + 1 }} {{ question.type === 'single_choice' ? '单选题' : '多选题' }}</h4>
          <el-input v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
          <el-input v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
          <el-input-number v-if="type === 'exam'" v-model="question.score" placeholder="分数" />
          <ul>
            <li v-for="(option, optIndex) in question.options" :key="optIndex">
              <el-input v-model="question.options[optIndex]" placeholder="选项内容" />
              <el-button @click="removeOption(question, optIndex)"><el-icon><RemoveFilled /></el-icon>删除选项</el-button>
              <el-button @click="setAnswer(question, question.options[optIndex])" 
                         v-if="type === 'exam'"
                         :style="{ color: question.answer.includes(question.options[optIndex]) ? 'green' : 'gray' }">
                          <el-icon><Check /></el-icon>设为答案
              </el-button>
            </li>
          </ul>
          <el-button @click="addOption(question)"><el-icon><Plus /></el-icon>添加选项</el-button>
        </template>

        <template v-if="question.type === 'fill_blank'">
          <h4>Q{{ index + 1 }}填空题</h4>
          <el-input v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
          <el-input v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
          <el-input-number v-if="type === 'exam'" v-model="question.score" placeholder="分数" />
          <el-input v-if="type === 'exam'" v-model="question.answer[0]" placeholder="请输入正确答案" />
        </template>

        <template v-if="question.type === 'file'">
          <h4>Q{{ index + 1 }}文件上传题</h4>
          <el-input v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
          <el-input v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
        </template>

        <template v-if="question.type === 'code'">
            <h4>Q{{ index + 1 }} 代码题</h4>
            <div>
                <el-input v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
            </div>
            <el-input v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
            <el-input-number v-model="question.score" placeholder="分数" />

            <div>运行时间(ms),不超过10000</div>
            <el-input-number 
              v-model="question.timeLimit" 
              placeholder="运行时间(ms)"
              :min="1" 
              :max="10000"  
            />
            <div>空间限制(MB),不超过1024</div>
            <el-input-number 
              v-model="question.memoryLimit" 
              placeholder="空间限制(MB)"
              :min="1"
              :max="1024" 
            />
            <div>栈空间限制(MB),不超过512</div>
            <el-input-number 
              v-model="question.stackLimit" 
              placeholder="栈空间限制(MB)"
              :min="1"
              :max="512" 
            />

            <div>选择支持的编程语言</div>
            <el-select 
              v-model="question.languages" 
              placeholder="选择编程语言" 
              multiple 
              style="width: 100%;"
            >
                <el-option label="Java" value="Java"></el-option>
                <el-option label="Python" value="Python"></el-option>
                <el-option label="C++" value="C++"></el-option>
                <el-option label="C" value="C"></el-option>
            </el-select>

             <!-- 上传 .in 文件 -->    
             <div style="display: flex;">
              <el-upload
                :before-upload="beforeUploadIn"
                action="http://59.110.163.198:8088/api/common/upload"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccessIn(question)"
                :on-error="handleUploadError"
                :show-file-list="true"
                :on-remove="(file) => handleFileRemove(file, question, 'input')"
              >
              <el-button>上传 .in 文件</el-button>
              </el-upload>
    
              <!-- 上传 .out 文件 -->
              <el-upload
                :before-upload="beforeUploadOut"
                action="http://59.110.163.198:8088/api/common/upload"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccessOut(question)"
                :on-error="handleUploadError"
                :show-file-list="true"
                :on-remove="(file) => handleFileRemove(file, question, 'output')"
              >
              <el-button>上传 .out 文件</el-button>
              </el-upload>
             </div>     
            
        </template>
        <div class="question-header">
          <!-- 上移、下移按钮 -->
          <button @click="moveUp(index)" v-if="index !== 0 && (!isEditing)" style="margin: 10px;"><el-icon><ArrowUp /></el-icon>上移</button>
          <button @click="moveDown(index)" v-if="index !== questions.length - 1 && !isEditing" style="margin: 10px;" ><el-icon><ArrowDownBold /></el-icon>下移</button>
          <!-- 删除问题按钮 -->
          <button @click="removeQuestion(index)" v-if="!isEditing" style="margin: 10px;"><el-icon><CloseBold /></el-icon>删除问题</button>
          <!-- 是否必填按钮 -->
          <button @click="question.required = !question.required" v-if="!isEditing" style="margin: 10px;"><el-icon><QuestionFilled /></el-icon>是否必填</button>
          <button @click="isEditing=flase" v-if="isEditing" style="margin: 10px;">完成编辑</button>
        </div>   
      </div>
      <h3>点击添加问题</h3>
    </div>


    <div v-if="isOpen === false">
      
      <div class="question-types">
        <button @click="addQuestion('single_choice')"><el-icon><HelpFilled /></el-icon>单选题</button>
        <button @click="addQuestion('multiple_choice')"><el-icon><CircleCheckFilled /></el-icon>多选题</button>
        <button @click="addQuestion('fill_blank')"><el-icon><Edit /></el-icon>填空题</button>
        <button v-if="type === 'normol'" @click="addQuestion('file')"><el-icon><Edit /></el-icon>文件上传题</button>
        <button v-if="type === 'exam'" @click="addQuestion('code')"><el-icon><Edit /></el-icon>代码题</button>
      </div>
      <div style="display: flex;flex-direction: column; align-items: center; ">
        <button @click="saveSurvey"><el-icon><Checked /></el-icon>保存问卷</button>
      </div>
    </div>

    
  </div>
</template>

<style scoped>
.question-types {
  padding-left: 20%;
  padding-right: 20%;
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
  display: flex;
  flex-direction: column;
  align-items: center;
}

.question-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-start; /* 将子元素左对齐 */
  background-color: white;
  width: 50%;
  border: 1px solid lightblue;
  padding-left: 5%;
  
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
