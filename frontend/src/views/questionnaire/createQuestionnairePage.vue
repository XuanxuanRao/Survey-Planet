<script setup>
import { ref, onMounted } from 'vue'
import { getCreatedQuestionnaireList, userDeleteQuestionnaire, userShareQuestionnaire, userCloseQuestionnaire, userExportResult } from '@/api/questionnaire'
import { useRouter } from 'vue-router'
//import { ElButton, ElMessage } from 'element-plus'
import { VideoPause, VideoPlay} from '@element-plus/icons-vue'
const createdQuestionnaireList = ref([]);  // 响应式变量，存储用户数据

// 控制 Dialog 显示状态的变量
const dialogVisible = ref(false);

// Dialog 中显示的消息内容
const dialogMessage = ref('');

// 显示 Dialog 的函数
function showDialog(data) {
  dialogMessage.value = '请复制链接即可填写: ' + data;
  dialogVisible.value = true; // 显示对话框
}


// 在组件挂载时获取数据
onMounted(async () => {
    const res = await getCreatedQuestionnaireList();
    createdQuestionnaireList.value = res.data;
})

const questionnaireTitle = ref('')  // 输入框的值
const questionnaireType = ref('')  // 下拉框的值

const options = [
  {
    value: 'normal',
    label: '普通问卷'
  },
  {
    value: 'exam',
    label: '考试问卷'
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
  if(questionnaireType.value === 'exam') {
    router.push({ path: '/questionnaire/createExamQuestion', query: dataToSend })
  } else {
    router.push({ path: '/questionnaire/createQuestion', query: dataToSend })
  }
}

const view = (id) => {
  console.log(id)
  const dataToSend = {id: id};
  router.push({ path: '/questionnaire/viewQuestionnaire', query: dataToSend })
}

const modify = (id) => {
  console.log(id)
  const dataToSend = {id: id};
  router.push({ path: '/questionnaire/modifyQuestionnaire', query: dataToSend })
}

const deleteQuestionnaire = async (id) => {
  const res = await userDeleteQuestionnaire(id);
  console.log(res)
  if (res.msg === 'success') {
    ElMessageBox.confirm(
          '确认删除?', 
          '提示', 
          {
            confirmButtonText: '删除',
            cancelButtonText: '取消',
            type: 'warning',
          }
        ).then(async() => {
          const res = await getCreatedQuestionnaireList();
          createdQuestionnaireList.value = res.data;
        }).catch(() => {
          ElMessage.info('已取消删除');
        })
  } else {
    ElMessage.error('删除失败');
  }
}

const shareOrCloseQuestionnaire = async (id, state) => {
  if(state === 'open') {
    const res = await userCloseQuestionnaire(id);
    console.log(res)
    if (res.msg === 'success') {
      ElMessage.success('关闭成功');
    } else {
      ElMessage.error('关闭失败');
    }
  } else if (state === 'close') {
    const res = await userShareQuestionnaire(id);
    console.log(res);
    if (res.msg === 'success') {
        showDialog(res.data)
    } else {
      ElMessage.error('分享失败');
    }
  }
  const res = await getCreatedQuestionnaireList();
  createdQuestionnaireList.value = res.data;
}

const exportResult = async(id) => {
  console.log(id)
  userExportResult(id);
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
          <div class="name-description">
            <div class="left-content">
              <span class="title" @click="view(questionnaire.sid)">{{ questionnaire.title, questionnaire.type }}</span>
            </div>
            <div class="spacing"></div>
            <div class="right-content">
              问卷描述：{{ questionnaire.description }}
              {{ questionnaire.createTime }}
              {{ questionnaire.updateTime }}
              {{ questionnaire.fillNum }}
              {{ index }}
            </div>
          </div>
          
          <div class="button-name-description">
            <div class="button-left-content">
              <el-button>查看</el-button>
              <el-button @click="exportResult(questionnaire.sid)">下载</el-button>
              <el-button @click="deleteQuestionnaire(questionnaire.sid)">删除</el-button>
              <el-button @click="modify(questionnaire.sid)">修改</el-button>
            </div>
            <div class="spacing2"></div>
            <div class="button-right-content">
              <el-button v-if="questionnaire.state === 'close'" :icon="VideoPause" @click="shareOrCloseQuestionnaire(questionnaire.sid, questionnaire.state)">点击发布</el-button>
              <el-button v-else :icon="VideoPlay" @click="shareOrCloseQuestionnaire(questionnaire.sid, questionnaire.state)">点击停止</el-button>
            </div>
          </div>
      </li>
    </ul>
  </div>
      <!-- Dialog 弹框 -->
    <el-dialog v-model="dialogVisible" title="提示信息" width="30%" @close="dialogVisible = false">
      <span>{{ dialogMessage }}</span> <br>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
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

.left-content {
  text-align: left;
}

.right-content {
  text-align: right;
}

.button-name-description {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.button-left-content {
  text-align: left;
}

.button-right-content {
  text-align: right;
}

.title {
  font-size: 24px;
  font-weight: bold;
  transition: color 0.3s ease;
  cursor: pointer;
}

.title:hover {
  color: #007bff; /* 悬停时变为蓝色 */
}


.infinite-list .infinite-list-item + .list-item {
  margin-top: 10px;
}
.spacing {
  margin-right:600px; /* 调整这个值以满足实际需求 */
}
.spacing2 {
  margin-right:900px; /* 调整这个值以满足实际需求 */
}
.button-group {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}
.questionnaire-name {
  font-weight: bold;
  margin-bottom: 10px;
}
</style>