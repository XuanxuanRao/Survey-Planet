<script setup>
import { ref, onMounted } from 'vue'
import { getCreatedQuestionnaireList } from '@/api/questionnaire'
import { useRouter } from 'vue-router';

const createdQuestionnaireList = ref([]);  // 响应式变量，存储用户数据

// 在组件挂载时获取数据
onMounted(async () => {
    const res = await getCreatedQuestionnaireList();
    createdQuestionnaireList.value = res.data;
    // console.log(createdQuestionnaireList.value[0])
})

const questionnaireTitle = ref('')  // 输入框的值
const questionnaireType = ref('')  // 下拉框的值

const options = [
  {
    value: 'normal',
    label: 'normal'
  },
  {
    value: 'exam',
    label: 'exam'
  },
]

const router = useRouter()
const createQuestionnaire = () => {
  if (questionnaireTitle.value === '' || questionnaireType.value === '') {
      ElMessage.warning('问卷名和问卷类型不能为空');
    return
  }
  console.log(questionnaireTitle.value)
  console.log(questionnaireType.value)
  const dataToSend = { title: questionnaireTitle.value, type: questionnaireType.value };
  router.push({ path: '/questionnaire/questionnaireSquare', query: dataToSend })
}

</script>

<template>
  <div class="box">
    <div class="header">
    <h1>创建问卷</h1>
    <el-select
    v-model="questionnaireType"
    placeholder="请选择问卷类型"
    size="large"
    style="width: 15vw"
    >
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>

    <el-input
    v-model="questionnaireTitle"
    style="width: 50vw; height: 40px;"
    placeholder="请输入问卷名"
    clearable
    />

    <el-button
    type="primary"
    size="large"
    style="width: 15vw"
    @click="createQuestionnaire"
    >
    创建问卷
    </el-button>
  </div>

  <div class="showQues">
    <ul class="infinite-list" style="overflow: auto">
      <li v-for="(questionnaire, index) in createdQuestionnaireList" :key="index" class="infinite-list-item">
        {{ questionnaire.title }}
        {{ questionnaire.description }}
        {{ questionnaire.createTime }}
        {{ questionnaire.updateTime }}
        {{ questionnaire.status }}
        {{ questionnaire.fillNum }}
        按钮
        <!-- 迟点做渲染 -->
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

.infinite-list {
  border-radius: 10px;
  background-color: black;
  height: 350px;
  padding: 20px;
  margin: 0;
  list-style: none;
}
.infinite-list .infinite-list-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  background: var(--el-color-primary-light-9); /* 背景颜色 */
  margin: 10px;
  color: var(--el-color-primary); /* 字体颜色 */
}
.infinite-list .infinite-list-item + .list-item {
  margin-top: 10px;
}
</style>