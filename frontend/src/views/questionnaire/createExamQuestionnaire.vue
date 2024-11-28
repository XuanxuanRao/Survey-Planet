<script setup>
import { onMounted, ref, watch, computed } from 'vue'
import { useRoute, onBeforeRouteLeave } from 'vue-router'
import { Edit, CircleCheckFilled, HelpFilled, EditPen, Checked, ArrowDownBold, ArrowUp, CloseBold, Plus, QuestionFilled, RemoveFilled, Tickets, Check } from '@element-plus/icons-vue'
import { userSendQuestionnaireList, userModifyQuestionnaireList } from '@/api/questionnaire'
import { useUserStore } from "@/stores/user"
const isEditing=ref()
const isEditingTianKong=ref()
const isEditingWenjian=ref()
const route = useRoute()
const questionnaireTitle = route.query.title
const questionnaireType = route.query.type
const examId = ref()

const questions = ref([])  // 存储所有问题的数组
const examData = ref([]) // 存储最终问卷数据的数组

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
        maxFileSize: 20, //文件大小
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
        maxFileSize: 20, //文件大小
    }
    questions.value.push(newQuestion)
  }
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
  const optionToRemove = question.options[index]
  question.options.splice(index, 1)  // 删除选项

  // 如果该选项在答案中，移除它
  const answerIndex = question.answer.indexOf(optionToRemove)
  if (answerIndex !== -1) {
    question.answer.splice(answerIndex, 1)  // 从答案中移除该选项
  }
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

const isSave = ref(true)  // 是否保存问卷
watch(questions.value, () => {
  isSave.value = false
})

// 保存问卷
const saveExam = async() => {
  if(questions.value.length === 0) {
    ElMessage.warning('请添加问题')
    return
  }

  // 检查选择题是否至少有一个选项
  for (const question of questions.value) {
    if ((question.type === 'single_choice' || question.type === 'multiple_choice') && question.options.length === 0) {
      ElMessage.error('选择题必须至少有一个选项')
      return
    }
    if(question.type === 'code') {
      if(question.languages.length === 0) {
        ElMessage.error('请选择编程语言')
        return
      }
      if(question.inputFileUrls.length === 0 || question.outputFileUrls.length === 0) {
        ElMessage.error('请上传输入输出文件')
        return
      }
      if(question.inputFileUrls.length !== question.outputFileUrls.length) {
        ElMessage.error('输入输出文件数量不一致')
        return
      }
    }
  }

  examData.value = [...questions.value] // 将当前问卷的问题和选项保存到 examData
  isSave.value = true
  const res = await userModifyQuestionnaireList(examId.value, questionnaireType, questionnaireTitle, '', examData.value)
  if(res.msg === 'success') {
    ElMessage.success('问卷已保存')
  } else {
    ElMessage.error('保存失败，请重试')
  }
}

// 路由离开时提示用户
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
    // localStorage.removeItem('questions')
    localStorage.removeItem('examId')
    next() // 已保存，允许路由跳转
  }
})

onMounted(async () => {
  // const savedQuestions = localStorage.getItem('questions')
  // if (savedQuestions) {
  //   questions.value = JSON.parse(savedQuestions)
  //   examId.value = localStorage.getItem('examId')
  // } else {
  //   const res = await userSendQuestionnaireList(questionnaireType, questionnaireTitle, '', examData.value)
  //   examId.value = res.data
  //   localStorage.setItem('examId', examId.value)
  // }
  examId.value = localStorage.getItem('examId')
  console.log(examId.value)
  if(examId.value === null) {
    const res = await userSendQuestionnaireList(questionnaireType, questionnaireTitle, '', examData.value)
    examId.value = res.data
    localStorage.setItem('examId', examId.value)
  }
})

// watch(questions, (newQuestions) => {
//   localStorage.setItem('questions', JSON.stringify(newQuestions));
// }, { deep: true });


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


</script>

<template>
  <div>
    <h2>{{questionnaireTitle}}</h2>
    
    <div class="question-list">
      <div v-for="(question, index) in questions" :key="index" class="question-item">
        <template v-if="question.type === 'single_choice' || question.type === 'multiple_choice'">
          <h4>
            {{ question.required == true ? "*" : "" }}Q{{ index + 1 }}
            <el-input 
              v-model="question.title" 
              style="width: 400px;" 
              placeholder="标题" 
              @focus="isEditing = true" 
              @blur="isEditing = false" 
            />
            <span v-if="!isEditing">
              {{ question.type === 'single_choice' ? '[单选题]' : '[多选题]' }}
            </span>
          </h4>
          <el-input @focus="isEditing = true" 
              @blur="isEditing = false"
               v-model="question.description" style="width: 600px;" placeholder="请填入问题描述" :prefix-icon="EditPen"/>
          <ul>
            <li v-for="(option, optIndex) in question.options" :key="optIndex">
              <el-input @focus="isEditing = true" 
              @blur="isEditing = false"
              style="width: 550px;"
               v-model="question.options[optIndex]" placeholder="选项内容">
                <template #append>
                  <el-button @focus="isEditing = true" 
                  @blur="isEditing = false"
                  @click="removeOption(question, optIndex)" style="margin: 5px;"><el-icon><RemoveFilled /></el-icon>删除选项</el-button>
                  <el-button @focus="isEditing = true" 
                  @blur="isEditing = false"
                  @click="addOption(question)" style="margin: 5px;"><el-icon><Plus /></el-icon>添加选项</el-button>
                  <el-button @focus="isEditing = true" 
                  @blur="isEditing = false"
                  @click="setAnswer(question, question.options[optIndex])" style="margin: 5px;"
                             :style="{ color: question.answer.includes(question.options[optIndex]) ? 'green' : 'gray' }">
                    <el-icon><Check /></el-icon>设为答案
                  </el-button>
                </template>
              </el-input>
            </li>
          </ul>
          <div class="question-header">
          <el-input-number @focus="isEditing = true" 
              @blur="isEditing = false"
               v-model="question.score" placeholder="分数" :min="1" />
          <button @click="moveUp(index)" v-if="index !== 0&& !isEditing" style="margin: 10px;"><el-icon><ArrowUp /></el-icon>上移</button>
          <button @click="moveDown(index)" v-if="index !== questions.length - 1&& !isEditing" style="margin: 10px;"><el-icon><ArrowDownBold /></el-icon>下移</button>
          <button @click="removeQuestion(index)" v-if="!isEditing" style="margin: 10px;"><el-icon><CloseBold /></el-icon>删除问题</button>
          <button @click="question.required = !question.required" v-if="!isEditing" style="margin: 10px;"><el-icon><QuestionFilled /></el-icon>{{ question.required ? '必填' : '非必填' }}</button>
          <button @click="isEditing=flase" v-if="isEditing" style="margin: 10px;">完成编辑</button>
        </div>
        </template>

        <template v-if="question.type === 'fill_blank'">
          <h4>{{ question.required == true ? "*" : "" }}Q{{ index + 1 }}
            <el-input  @focus="isEditingTianKong = true" 
                @blur="isEditingTianKong = false" style="width: 400px;"  v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
            <span v-if="!isEditingTianKong">[填空题] </span>
          </h4>
          <div>
            <el-input  @focus="isEditingTianKong = true" style="width: 500px;"
                @blur="isEditingTianKong = false" v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
          </div>
          
          
          <!-- 设置填空题答案 -->
          <el-input v-model="question.answer[0]" style="width: 500px;" @focus="isEditingTianKong = true" 
                @blur="isEditingTianKong = false" placeholder="请输入正确答案" />
          <div class="question-header">
            <el-input-number @focus="isEditingTianKong = true" 
                @blur="isEditingTianKong = false" v-model="question.score" placeholder="分数" :min="1" />
            <!-- 上移、下移按钮 -->
            <button @click="moveUp(index)" v-if="index !== 0 && (!isEditingTianKong)" style="margin: 10px;"><el-icon><ArrowUp /></el-icon>上移</button>
            <button @click="moveDown(index)" v-if="index !== questions.length - 1 && !isEditingTianKong" style="margin: 10px;" ><el-icon><ArrowDownBold /></el-icon>下移</button>
            <!-- 删除问题按钮 -->
            <button @click="removeQuestion(index)" v-if="!isEditingTianKong" style="margin: 10px;"><el-icon><CloseBold /></el-icon>删除问题</button>
            <!-- 是否必填按钮 -->
            <button @click="question.required = true" v-if="!isEditingTianKong" style="margin: 10px;"><el-icon><QuestionFilled /></el-icon>是否必填</button>
            <button @click="isEditing=flase" v-if="isEditingTianKong" style="margin: 10px;">完成编辑</button>
          </div>   
        </template>

        <template v-if="question.type === 'code'">
            <h4>{{ question.required == true ? "*" : "" }}Q{{ index + 1 }}
              <el-input @focus="isEditingWenjian = true" 
                  @blur="isEditingWenjian = false" style="width: 400px;"  v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
              <span v-if="!isEditingWenjian">[代码题] </span> 
           </h4>
           
            <el-input @focus="isEditingWenjian = true"  style="width: 500px;"
                  @blur="isEditingWenjian = false" v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
            

            <div>运行时间(ms),不超过10000:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<el-input-number  @focus="isEditingWenjian = true" 
              @blur="isEditingWenjian = false"
              v-model="question.timeLimit" 
              placeholder="运行时间(ms)"
              :min="1" 
              :max="10000"  
            /></div>
            
            <div>空间限制(MB),不超过1024:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<el-input-number @focus="isEditingWenjian = true" 
              @blur="isEditingWenjian = false"
              v-model="question.memoryLimit" 
              placeholder="空间限制(MB)"
              :min="1"
              :max="1024" 
            /></div>
            
            <div>栈空间限制(MB),不超过512:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<el-input-number @focus="isEditingWenjian = true" 
              @blur="isEditingWenjian = false"
              v-model="question.stackLimit" 
              placeholder="栈空间限制(MB)"
              :min="1"
              :max="512" 
            /></div>
            

            <div>选择支持的编程语言:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<el-select  @focus="isEditingWenjian = true" 
              @blur="isEditingWenjian = false"
              v-model="question.languages" 
              placeholder="选择编程语言" 
              multiple 
              style="width: 20%;"
            >
                <el-option label="Java" value="Java"></el-option>
                <el-option label="Python" value="Python"></el-option>
                <el-option label="C++" value="C++"></el-option>
                <el-option label="C" value="C"></el-option>
            </el-select></div>
            

             <!-- 上传 .in 文件 -->         
            <el-upload @focus="isEditingWenjian = true" 
              @blur="isEditingWenjian = false"
              :before-upload="beforeUploadIn"
              action="http://59.110.163.198:8088/api/common/upload"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccessIn(question)"
              :on-error="handleUploadError"
              :show-file-list="true"
              :on-remove="(file) => handleFileRemove(file, question, 'input')"
            >
            <el-button @focus="isEditingWenjian = true" 
                  @blur="isEditingWenjian = false"> 上传 .in 文件</el-button>
            </el-upload>
  
            <!-- 上传 .out 文件 -->
            <el-upload @focus="isEditingWenjian = true" 
              @blur="isEditingWenjian = false"
              :before-upload="beforeUploadOut"
              action="http://59.110.163.198:8088/api/common/upload"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccessOut(question)"
              :on-error="handleUploadError"
              :show-file-list="true"
              :on-remove="(file) => handleFileRemove(file, question, 'output')"
            >
            <el-button @focus="isEditingWenjian = true" 
                  @blur="isEditingWenjian = false">上传 .out 文件</el-button>
            </el-upload>
            <div class="question-header">
              <el-input-number @focus="isEditingWenjian = true" 
                  @blur="isEditingWenjian = false" v-model="question.score" placeholder="分数" :min="1"/>
              <!-- 上移、下移按钮 -->
              <button @click="moveUp(index)" v-if="index !== 0 && (!isEditingWenjian)" style="margin: 10px;"><el-icon><ArrowUp /></el-icon>上移</button>
              <button @click="moveDown(index)" v-if="index !== questions.length - 1 && !isEditingWenjian" style="margin: 10px;" ><el-icon><ArrowDownBold /></el-icon>下移</button>
              <!-- 删除问题按钮 -->
              <button @click="removeQuestion(index)" v-if="!isEditingWenjian" style="margin: 10px;"><el-icon><CloseBold /></el-icon>删除问题</button>
              <!-- 是否必填按钮 -->
              <button @click="question.required = true" v-if="!isEditingWenjian" style="margin: 10px;"><el-icon><QuestionFilled /></el-icon>是否必填</button>
              <button @click="isEditing=flase" v-if="isEditingWenjian" style="margin: 10px;">完成编辑</button>
            </div>
        </template>
        
      </div>
    </div>

    <h3>点击添加问题</h3>
    <div class="question-types">
      <button @click="addQuestion('single_choice')"  ><el-icon><HelpFilled /></el-icon>单选题</button>
      <button @click="addQuestion('multiple_choice')"><el-icon><CircleCheckFilled /></el-icon>多选题</button>
      <button @click="addQuestion('fill_blank')"><el-icon><Edit /></el-icon>填空题</button>
      <button @click="addQuestion('code')"><el-icon><Edit /></el-icon>代码题</button>
    </div>

    <button @click="saveExam" ><el-icon><Checked /></el-icon>保存问卷</button>

    <div class="exam-data">
      <h3>考试数据</h3>
      <pre>{{ examData }}</pre>
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
  display: flex;
  flex-direction: column;
  align-items: center;
}

.question-item {
  margin-bottom: 20px;
  background-color: white;
  width: 1200px;
}

.question-header {
  display: flex;
  justify-content: right;
  align-items: center;
  margin-bottom: 10px;
}

.exam-data {
  margin-top: 20px;
}
</style>