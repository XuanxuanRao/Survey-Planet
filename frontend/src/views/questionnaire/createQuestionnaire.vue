<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { Edit,CircleCheckFilled,HelpFilled,EditPen,Checked,ArrowDownBold,ArrowUp,CloseBold,Plus,QuestionFilled,RemoveFilled,Tickets,Check} from '@element-plus/icons-vue'
import { userSendQuestionnaireList, userModifyQuestionnaireList } from '@/api/questionnaire'
const isEditing=ref()
const isEditingTianKong=ref()
const isEditingWenjian=ref()
const route = useRoute()
const isDark = ref(false)
const questionnaireTitle = route.query.title
const questionnaireType = route.query.type
const qusetionnaireId = ref()

const questions = ref([])  // 存储所有问题的数组
const surveyData = ref([]) // 存储最终问卷数据的数组
// 添加问题的函数，新的问题添加到数组的最上方
const addQuestion = (type) => {
  const newQuestion = {
    type: type,
    title: '',  // 问题内容可编辑
    description: '', // 问题描述
    maxFileSize: 20, //文件大小
    options: type === 'single_choice' || type === 'multiple_choice' 
      ? ['选项1', '选项2'] // 默认两个选项
      : [],  // 非选择题没有选项
    required: false,  // 是否必填
  }
  questions.value.push(newQuestion)
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


const isSave = ref(true)  // 是否保存问卷
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

  // 检查选择题是否至少有一个选项
  for (const question of questions.value) {
    if ((question.type === 'single_choice' || question.type === 'multiple_choice') && question.options.length === 0) {
      ElMessage.error('选择题必须至少有一个选项')
      return
    }
  }

  surveyData.value = [...questions.value] // 将当前问卷的问题和选项保存到 surveyData
  console.log(questionnaireTitle)
  console.log(questionnaireType)
  console.log('问卷数据已保存:', surveyData.value)
  isSave.value = true
  const res = await userModifyQuestionnaireList(qusetionnaireId.value,
                                        questionnaireType, 
                                        questionnaireTitle,
                                        '', 
                                        surveyData.value)
  console.log(res)
  if(res.msg === 'success') {
    ElMessage.success('问卷已保存')
  } else {
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
      localStorage.removeItem('qusetionnaireId')
        next() // 已保存，允许路由跳转
    }
})

onMounted(async () => {
  qusetionnaireId.value = localStorage.getItem('qusetionnaireId')
  console.log(qusetionnaireId.value)
  if(qusetionnaireId.value === null) {
    const res = await userSendQuestionnaireList(questionnaireType, 
                                        questionnaireTitle,
                                        '', 
                                        surveyData.value)
    qusetionnaireId.value = res.data
    localStorage.setItem('qusetionnaireId', qusetionnaireId.value)
  }
})

</script>


<template>
  <div>
    <!-- 由上级传递 -->
    <h2>问卷名称</h2>
    
    <!-- 显示添加的问题 -->
    <div class="question-list">
      <div v-for="(question, index) in questions" :key="index" class="question-item">        
        <!-- 根据问题类型渲染不同的题目 -->
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
               v-model="question.description" style="width: 600px;"  placeholder="请填入问题描述" :prefix-icon="EditPen"/>
          <ul>
            <li v-for="(option, optIndex) in question.options" :key="optIndex">
              <el-input @focus="isEditing = true" 
                @blur="isEditing = false" 
               style="width: 550px;" v-model="question.options[optIndex]" placeholder="选项内容" >
                <template #append>
                <el-button @focus="isEditing = true" 
                @blur="isEditing = false" 
                 @click="removeOption(question, optIndex)"><el-icon><RemoveFilled /></el-icon>删除选项</el-button>
                <el-button @focus="isEditing = true" 
                @blur="isEditing = false" 
                  :dark="isDark" plain @click="addOption(question)"><el-icon><Plus /></el-icon>添加选项</el-button>
                </template>
              </el-input>
            </li>
          </ul> 
          <div class="question-header">
          <!-- 上移、下移按钮 -->
          <button @click="moveUp(index)" v-if="index !== 0 && (!isEditing)" style="margin: 10px;"><el-icon><ArrowUp /></el-icon>上移</button>
          <button @click="moveDown(index)" v-if="index !== questions.length - 1 && !isEditing" style="margin: 10px;" ><el-icon><ArrowDownBold /></el-icon>下移</button>
          <!-- 删除问题按钮 -->
          <button @click="removeQuestion(index)" v-if="!isEditing" style="margin: 10px;"><el-icon><CloseBold /></el-icon>删除问题</button>
          <!-- 是否必填按钮 -->
          <button @click="question.required = true" v-if="!isEditing" style="margin: 10px;"><el-icon><QuestionFilled /></el-icon>是否必填</button>
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
            
          </div>
          <el-input @focus="isEditingTianKong = true" style="width: 500px;"
                @blur="isEditingTianKong = false" v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
                <div class="question-header">
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

        <template v-if="question.type === 'file'">
          <h4>{{ question.required == true ? "*" : "" }}Q{{ index + 1 }}
            <el-input @focus="isEditingWenjian = true" 
                @blur="isEditingWenjian = false" style="width: 400px;"  v-model="question.title" placeholder="请输入问题内容" :prefix-icon="Tickets" />
            <span v-if="!isEditingWenjian">[文件上传题] </span> 
           </h4>
          <div>
            
          </div>
          <div>上传文件大小(MB):&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<el-input-number 
            @focus="isEditingWenjian = true" 
                @blur="isEditingWenjian = false"
              v-model="question.maxFileSize" 
              placeholder="上传文件大小"
              :min="1" 
              :max="100"  
            /></div>
            
          <el-input @focus="isEditingWenjian = true" style="width: 500px;"
                @blur="isEditingWenjian = false" v-model="question.description" placeholder="请填入问题描述" :prefix-icon="EditPen" />
          <div class="question-header">
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
        <!-- <div class="question-header">
      
          <button @click="moveUp(index)" v-if="index !== 0 && (!isEditing)" style="margin: 10px;"><el-icon><ArrowUp /></el-icon>上移</button>
          <button @click="moveDown(index)" v-if="index !== questions.length - 1 && !isEditing" style="margin: 10px;" ><el-icon><ArrowDownBold /></el-icon>下移</button>
      
          <button @click="removeQuestion(index)" v-if="!isEditing" style="margin: 10px;"><el-icon><CloseBold /></el-icon>删除问题</button>
         
          <button @click="question.required = true" v-if="!isEditing" style="margin: 10px;"><el-icon><QuestionFilled /></el-icon>是否必填</button>
          <button @click="isEditing=flase" v-if="isEditing" style="margin: 10px;">完成编辑</button>
        </div> -->
      </div>
    </div>
    <h3 style="">点击添加问题</h3>
      <!-- 问题类型按钮 -->
      <div class="question-types">
        <button @click="addQuestion('single_choice')" ><el-icon><HelpFilled /></el-icon>单选题</button>
        
        <button @click="addQuestion('multiple_choice')"><el-icon><CircleCheckFilled /></el-icon>多选题</button>
        
        <button @click="addQuestion('fill_blank')"><el-icon><Edit /></el-icon>填空题</button>
        
        <button @click="addQuestion('file')"><el-icon><Edit /></el-icon>文件上传题</button>
      </div>

      <!-- 保存按钮 -->
      <button @click="saveSurvey" ><el-icon><Checked /></el-icon>保存问卷</button>

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
  background-color: gree;
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

.survey-data {
  margin-top: 20px;
}

.input-with-select .el-input-group__prepend {
  background-color: var(--el-fill-color-blank);
}
.tableHead{
  width: 800px;
  border: 1px solid black; 
  margin-bottom: 30px;
}
.custom-buttonDark {
  border-color: #d3d3d3; /* 灰暗边框颜色 */
  position: relative;
  width: 80px;
  height: 35px;
  text-align: center;
  line-height: 60px;
  color: #666; /* 灰暗字体颜色 */
  font-size: 18px;
  text-decoration: none;
  font-family: sans-serif;
  border-radius: 30px;
  background-color: #d3d3d3; /* 灰暗颜色 */
  background-size: 400%;
  transition: background-position 0.5s;
  margin-bottom: 10px;
}
.custom-button {
  position: relative;
  width: 80px;
  height: 35px;
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
  margin-bottom: 10px;
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
