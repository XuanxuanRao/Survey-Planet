<script setup>
import { ref, onMounted } from 'vue'
import { Search,Delete, Edit,  Share, Upload, Download } from '@element-plus/icons-vue'
import { getCreatedQuestionnaireList, userDeleteQuestionnaire, userShareQuestionnaire, userCloseQuestionnaire, userExportResult, userGetUnreadmessage, userGetMessageDetail, userSetMessageUnread, userDeleteMessage } from '@/api/questionnaire'
import { useRouter } from 'vue-router'
import { VideoPause, VideoPlay, Bell,InfoFilled} from '@element-plus/icons-vue'
import { useUserStore } from "@/stores/user"
import quest_square from "@/components/quest_square.vue";
const activeName = ref('')
const createdQuestionnaireList = ref([]);  // 响应式变量，存储用户数据
const surveyTypeText = ref('');  // 问卷类型的文本
// 控制 Dialog 显示状态的变量
const dialogVisible = ref(false);
// Dialog 中显示的消息内容
const dialogMessage = ref('');
const isRead = ref(false)
// 定义折叠状态
const collapsedIndexes = ref([]); // 存储折叠的索引
// 切换折叠状态
function toggleCollapse(index) {
  const idx = collapsedIndexes.value.indexOf(index);
  if (idx > -1) {
    collapsedIndexes.value.splice(idx, 1); // 收起
  } else {
    collapsedIndexes.value.push(index); // 展开
  }
}
function expand(index) {
  if (!collapsedIndexes.value.includes(index)) {
    collapsedIndexes.value.push(index);
  }
}

function collapse(index) {
  const idx = collapsedIndexes.value.indexOf(index);
  if (idx > -1) {
    collapsedIndexes.value.splice(idx, 1);
  }
}
// 判断当前索引是否折叠
function isCollapsed(index) {
  return collapsedIndexes.value.includes(index);
}
const activeNames = ref(['1'])
const handleChange = (val) => {
  console.log(val)
}
// 显示 Dialog 的函数
function showDialog(data) {
  dialogMessage.value = '请复制链接即可填写: ' + data;
  dialogVisible.value = true; // 显示对话框
}

const unreadMessage = ref([])
const getUnreadmessage = async () => {
  const res = await userGetUnreadmessage(isRead.value)
  console.log(res.msg)
  if (res.msg === 'success') {
    unreadMessage.value = res.data
  } else {
    ElMessage.error('获取未读消息失败')
  }
  console.log(res.msg)
}

const dialogVisible1 = ref(false);  // 控制弹窗显示与隐藏
const dialogContent = ref('');     // 用于存储弹窗的内容

// 创建 WebSocket 连接
const socket = new WebSocket('ws://59.110.163.198:8088/ws?token=' + useUserStore().token);
const socketData = ref('');  // 用于存储 WebSocket 返回的数据

// 监听 WebSocket 消息
socket.addEventListener('message', async function (event) {
  const data = JSON.parse(event.data);  // 假设服务器返回的消息是 JSON 格式
  console.log("data",data)
  socketData.value = data
  await getUnreadmessage()
  console.log(unreadMessage.value)
  showPopup(data);  // 传递包含 mid 和 content 的数据
});

// 显示弹窗函数
function showPopup(data) {
  dialogContent.value = data.content;   // 设置弹窗的内容
  dialogVisible1.value = true;           // 显示弹窗
}

// 关闭弹窗时的处理函数
function handleClose() {
  console.log('Dialog closed');
  dialogVisible1.value = false;
}

async function handleClose2() {
  await getUnreadmessage();
  dialogVisible2.value = false;
}

const detailsData = ref(null)
const dialogVisible2 = ref(false)

// 点击“显示详情”按钮触发的函数
async function showDetails() {
  dialogVisible1.value = false;
  if(socketData.value.mid) {
    const res = await userGetMessageDetail(socketData.value.mid)
    if(res.msg === 'success') {
      res.data.createTime = res.data.createTime.replace('T', ' ');
      detailsData.value = res.data
      dialogVisible2.value = true
    } else {
      ElMessage.error('获取消息详情失败')
    }
  } else {
    await getUnreadmessage();
    unreadMessage.value.forEach(item => {
      item.createTime = item.createTime.replace('T', ' ');
    })
    detailsData.value = unreadMessage.value
    // detailsData.value.isRead = true;
    dialogVisible2.value = true
  }
}

async function loadMessageDetails(mid) {
  const res = await userGetMessageDetail(mid);
  surveyTypeText.value = res.data.surveyType === 'normal' ? '调查' : '考试';
  res.data.createTime = res.data.createTime.replace('T', ' ');
  if (res.data.type == 'NEW_SUBMISSION') {
    res.data.submitTime = res.data.submitTime.replace('T', ' ');
  }
  if (res.msg === 'success') {
    detailsData.value = res.data
  } else {
    ElMessage.error('Failed to load message details')
  }
}

async function openMessagesDialog() {
  await getUnreadmessage();
  unreadMessage.value.forEach(item => {
    item.createTime = item.createTime.replace('T', ' ');
  })
  detailsData.value = unreadMessage.value
  dialogVisible2.value = true;
}

// 处理复选框变化的事件
async function handleReadStatusChange() {
  const checked = detailsData.value.isRead;
  const mid = detailsData.value.mid;

  await userSetMessageUnread(mid, checked ? 0 : 1);
}

// 在组件挂载时获取数据
// onMounted(async () => {
//     const res = await getCreatedQuestionnaireList();
//     res.data.forEach(item => {
//       item.createTime = item.createTime.replace('T', ' ');
//     })
//     createdQuestionnaireList.value = res.data;
// })

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
  const dataToSend = { title: questionnaireTitle.value, type: questionnaireType.value };
  if(questionnaireType.value === 'exam') {
    router.push({ path: '/questionnaire/createExamQuestion', query: dataToSend })
  } else {
    router.push({ path: '/questionnaire/createQuestion', query: dataToSend })
  }
}

const view = (id) => {
  const dataToSend = {id: id};
  router.push({ path: '/questionnaire/viewQuestionnaire', query: dataToSend })
}

const modify = (id) => {
  const dataToSend = {id: id};
  router.push({ path: '/questionnaire/modifyQuestionnaire', query: dataToSend })
}

// const deleteQuestionnaire = async (id) => {
//   ElMessageBox.confirm(
//         '确认删除?', 
//         '提示', 
//         {
//           confirmButtonText: '删除',
//           cancelButtonText: '取消',
//           type: 'warning',
//         }
//       ).then(async() => {
//         const info = await userDeleteQuestionnaire(id);
//         console.log(info)
//         if (info.msg === 'success') {
//           ElMessage.success('删除成功')
//           const res = await getCreatedQuestionnaireList()
//           res.data.forEach(item => {
//             item.createTime = item.createTime.replace('T', ' ');
//           })
//           createdQuestionnaireList.value = res.data
//         } else {
//           ElMessage.error('删除失败');
//         }
//       }).catch(() => {
//         ElMessage.info('已取消删除');
//       })
// }

// const shareOrCloseQuestionnaire = async (id, state) => {
//   if(state === 'open') {
//     const res = await userCloseQuestionnaire(id);
//     if (res.msg === 'success') {
//       ElMessage.success('关闭成功');
//     } else {
//       ElMessage.error('关闭失败');
//     }
//   } else if (state === 'close') {
//     const res = await userShareQuestionnaire(id);
//     if (res.msg === 'success') {
//         showDialog(res.data)
//     } else {
//       ElMessage.error('分享失败');
//     }
//   }
//   const res = await getCreatedQuestionnaireList();
//   res.data.forEach(item => {
//     item.createTime = item.createTime.replace('T', ' ');
//   })
//   createdQuestionnaireList.value = res.data;
// }

// const exportResult = async(id) => {
//   userExportResult(id);
// }
// const analyseResult = (id) => {
//   const dataToSend = {id: id};
//   router.push({ path: '/questionnaire/lookQuestionnaire', query: dataToSend })
// }

const isReadComputed = ref(!isRead.value)

const handleReadChange = async() => {
  isReadComputed.value = !isRead.value
  await getUnreadmessage()
  showDetails()
}

const handleReadChange1 = async() => {
  isRead.value = !isReadComputed.value
  await getUnreadmessage()
  showDetails()
}

const deleteMessage = async(mid) => {
  const res = await userDeleteMessage(mid)
  if (res.msg === 'success') {
    ElMessage.success('删除成功')
    await getUnreadmessage()
    showDetails()
  } else {
    ElMessage.error('删除失败')
  }
}

</script>

<template>
  <div>
    <!-- WebSocket 连接 -->
    <el-dialog
      v-model="dialogVisible1"
      :title="'New Message'"
      width="400px"
      @close="handleClose"
    >
      <p>{{ dialogContent }}</p> <!-- 弹窗内容 -->
      <template #footer>
        <el-button @click="showDetails">ShowDetails</el-button> <!-- 显示详情按钮 -->
        <el-button @click="dialogVisible1 = false">Close</el-button> <!-- 关闭按钮 -->
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="dialogVisible2"
      :title="'Details Data'"
      width="600px"
      @close="handleClose2"
    >
      <template v-if="detailsData">
        <!-- if 数据渲染 -->
        <div v-if="Array.isArray(detailsData)" class="scroll-container">

          <el-checkbox v-model="isReadComputed" @change="handleReadChange1()">
            未读消息
          </el-checkbox>
          
          <el-checkbox v-model="isRead" @change="handleReadChange()">
            已读消息
          </el-checkbox>

          <el-table
            :data="detailsData"
            style="width: 100%"
            height="400px"
            border
          >
            <el-table-column prop="type" label="主题" width="200"></el-table-column>
            <el-table-column prop="createTime" label="时间" width="200"/>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="loadMessageDetails(scope.row.mid)"
                >
                  查看
                </el-button>

                <el-button
                  type="danger"
                  size="small"
                  @click="deleteMessage(scope.row.mid)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- else 数据渲染 -->
        <div v-else>
          <div class="container" v-if="detailsData.surveyReportLink">
            <h1>问卷有新答卷</h1>
              <p class="info">接收时间: <span class="highlight">{{detailsData.createTime}}</span></p>
              <p class="message">
                您发布的{{surveyTypeText}}问卷 <a :href="detailsData.surveyReportLink" class="highlight-link">{{detailsData.surveyTitle}}</a> 在
                <span class="highlight">{{detailsData.submitTime}}</span> 收到一份新提交。
              </p>
              <a :href="detailsData.submitLink" class="action-button">查看详情</a>
          </div>

          <div class="container" v-if="detailsData.senderName">
            <h1>问卷填写邀请</h1>
            <p class="info">接收时间: <span class="highlight">{{detailsData.createTime}}</span></p>
            <p class="info">邀请人: <span class="highlight">{{detailsData.senderName}}</span></p>
            <p class="message">
                你被邀请填写一份{{surveyTypeText}}问卷：<span class="highlight">{{detailsData.surveyTitle}}</span>。
            </p>
            <div class="invitation-box">
                {{detailsData.invitationMessage}}
            </div>
            <a :href="detailsData.surveyFillLink" target="_blank" class="action-button">填写问卷</a>
          </div>

          <div>
          <el-checkbox v-model="detailsData.isRead" @change="handleReadStatusChange()">
            标记为未读
          </el-checkbox>
          </div>
        </div>

        <!-- 添加复选框 -->
      </template>

      <template #footer>
        <el-button @click="dialogVisible2 = false">Close</el-button>
      </template>
    </el-dialog>

  </div>

  
  <div class="box">
    <div class="header">
    <div class="head-text">创建问卷</div>

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
    style="width: 45vw; height: 5.5vh;"
    placeholder="请输入问卷名"
    clearable
    />

    <el-button
    class="custom-button"
    type="primary"
    size="large"
    style="width: 15vw"
    @click="createQuestionnaire"
    >
    创建问卷
    </el-button>

        <!-- 消息图标和未读消息数 -->
    <el-badge
      :value="unreadMessage.length"
      class="message-badge"
      type="danger"
      :max="99"
      @click="openMessagesDialog"
    >
      <el-icon class="message-icon"><Bell /></el-icon>
    </el-badge>
  </div>
  <div id="square">
      <quest_square/>
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
.custom-collapse-item {
  background: url('@/assets/img/2.jpg') no-repeat center center ; /* 背景图路径 */
  background-size: cover; /* 背景图覆盖 */
  background-position: center; /* 背景图居中 */
  border-radius: 8px; /* 圆角 */
  padding: 10px; /* 内边距 */
  margin: 10px 0; /* 外边距 */
  color: lightblue; /* 根据背景图片调整字体颜色 */
}
.demo-collapse {
  background-color: transparent; /* 保证背景透明 */
}

.message-badge {
  cursor: pointer;
  margin-left: 10px;
  display: inline-block;
}

.message-icon {
  font-size: 30px; /* 增加图标的大小，默认大小是 1rem，设置为 36px */
  color: #a5aeb8;
}
.box {
  margin-left: 2%; /* 给侧边栏留出空间 */
  margin-top: 20px;
  height: 90vh;
  width: 80vw;
  background-color: #f0f0f0; 
  border: 1px solid #ccc; 
  border-radius: 5px; 
  padding: 15px; /* 内边距 */
  box-shadow: 0 5px 10px rgba(34, 30, 30, 0.877);
}

.showQues {
  margin-top: 20px;
}



.name-description {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color:black;
  
}

.button-name-description {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
}
/* 当内容展开时，max-height 和 padding 会恢复 */
.collapse-enter-active,
.collapse-leave-active {
  transition: max-height 0.3s ease-in-out, padding-top 0.3s ease-in-out, padding-bottom 0.3s ease-in-out;
}

.collapse-enter, .collapse-leave-to {
  max-height: 500/700vh;  /* 设置一个足够大的值，让它展开时不会被限制 */
  padding-top: 10px;
  padding-bottom: 10px;
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
.questionnaire-type-normal {
  font-size: 20px; /* 设置字体大小 */
  color: blue; /* 设置普通问卷的颜色为浅蓝色 */
}

.questionnaire-type-exam {
  font-size: 20px; /* 设置字体大小 */
  color: red; /* 设置考试问卷的颜色为红色 */
}
.dot-green {
  display: inline-block;
  width: 8px; /* 点的宽度 */
  height: 8px; /* 点的高度 */
  background-color: green; /* 绿色 */
  border-radius: 50%; /* 圆形 */
  margin-right: 5px; /* 右侧间距 */
}

.dot-gray {
  display: inline-block;
  width:8px; /* 点的宽度 */
  height: 8px; /* 点的高度 */
  background-color: gray; /* 灰色 */
  border-radius: 50%; /* 圆形 */
  margin-right: 5px; /* 右侧间距 */
}
.head-text{
      font-size: 30px;
      align-items: flex-end;
      margin-bottom: 20px;
      border-bottom: 2px solid black;
}

.custom-button {
  position: relative;
  width: 100px;
  height: 45px;
  text-align: center;
  line-height: 60/700vh;
  color: #fff;
  font-size: 18px;
  text-decoration: none;
  font-family: sans-serif;
  border-radius: 30px;
  background: linear-gradient(90deg, #03a9f4, #f441a5, #ffeb3b, #03a9f4);
  background-size: 400%;
  transition: background-position 0.5s;
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
h1 {
    color: #0056b3;
    font-size: 24px;
    margin-bottom: 20px;
}

.info {
    font-size: 14px;
    color: #666;
    margin-bottom: 15px;
}

.highlight {
    font-weight: bold;
    color: #d9534f;
}

.message {
    font-size: 16px;
    color: #333;
    margin-bottom: 20px;
    line-height: 1.8;
}

.invitation-box {
    padding: 15px;
    border-left: 4px solid #007bff;
    background: #f1f9ff;
    font-size: 16px;
    color: #0056b3;
    margin-bottom: 20px;
    line-height: 1.6;
    border-radius: 8px;
    box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.1);
}

.action-button {
    display: inline-block;
    font-size: 16px;
    padding: 12px 25px;
    background: linear-gradient(135deg, #007bff, #0056b3);
    color: #ffffff;
    border-radius: 25px;
    text-decoration: none;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    transition: background 0.3s, transform 0.2s;
}

.action-button:hover {
    background: linear-gradient(135deg, #0056b3, #004494);
    transform: translateY(-3px);
}
</style>