<script setup>
import { ref, onMounted } from 'vue';
import axios from "axios";
import { getCreatedQuestionnaireList1, userDeleteQuestionnaire, userShareQuestionnaire, userCloseQuestionnaire, userExportResult, userGetUnreadmessage, userGetMessageDetail, userSetMessageUnread } from '@/api/questionnaire'
import { useRouter } from 'vue-router';
import { ArrowDown,Search,Document,Position,Star,Delete, Edit,  Share, Upload, Download,VideoPause, VideoPlay, Bell,InfoFilled } from '@element-plus/icons-vue'



const currentPage=ref(1);
const total=ref()
const surveys = ref([]);
const activeIndex = ref(null);
const images = [
  "https://images.unsplash.com/photo-1558979158-65a1eaa08691?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1350&q=80",
  "https://images.unsplash.com/photo-1572276596237-5db2c3e16c5d?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1350&q=80",
  "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1353&q=80",
  "https://images.unsplash.com/photo-1551009175-8a68da93d5f9?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1351&q=80",
  "https://images.unsplash.com/photo-1549880338-65ddcdfd017b?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1350&q=80",
  "https://images.unsplash.com/photo-1558979158-65a1eaa08691?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1350&q=80",
  "https://images.unsplash.com/photo-1572276596237-5db2c3e16c5d?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1350&q=80",
  "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1353&q=80",
  "https://images.unsplash.com/photo-1551009175-8a68da93d5f9?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1351&q=80",
  "https://images.unsplash.com/photo-1549880338-65ddcdfd017b?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1350&q=80",
];

const router = useRouter();
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
  ElMessageBox.confirm(
        '确认删除?', 
        '提示', 
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async() => {
        const info = await userDeleteQuestionnaire(id);
        console.log(info)
        if (info.msg === 'success') {
          ElMessage.success('删除成功')
          getQuePaper(currentPage.value)
        } else {
          ElMessage.error('删除失败');
        }
      }).catch(() => {
        ElMessage.info('已取消删除');
      })
}

const shareOrCloseQuestionnaire = async (id, state) => {
  if(state === 'open') {
    const res = await userCloseQuestionnaire(id);
    console.log("shareOrCloseQuestionnaire",res)
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
  getQuePaper(currentPage.value)
}
// 控制 Dialog 显示状态的变量
const dialogVisible = ref(false);
// Dialog 中显示的消息内容
const dialogMessage = ref('');
// 显示 Dialog 的函数
function showDialog(data) {
  dialogMessage.value = '请复制链接即可填写: ' + data;
  dialogVisible.value = true; // 显示对话框
}
const exportResult = async(id) => {
  console.log(id)
  userExportResult(id);
}
const analyseResult = (id) => {
  console.log(id)
  const dataToSend = {id: id};
  router.push({ path: '/questionnaire/lookQuestionnaire', query: dataToSend })
}
onMounted(() => {
  getQuePaper(currentPage.value);
});

async function getQuePaper(page) {
  try {
    surveys.value.length = 0; // 清空 surveys.value 数组
    // 每次只展示10条
    const res = await getCreatedQuestionnaireList1(page);
    const quePapersFromResponse = res.data;
    total.value = quePapersFromResponse.total;
    console.log("quePapersFromResponse",quePapersFromResponse)
    quePapersFromResponse.records.forEach((quePaper, index) => {
        const newSurvey = {
          ...quePaper // 使用扩展运算符将 quePaper 的所有属性复制到 newSurvey
        };
        surveys.value.push(newSurvey);
    });
    console.log("surveys.value",surveys.value);
  } catch (error) {
    console.error('请求失败:', error);
    alert('请求失败，请稍后重试或联系管理员。');
  }
}

const handleCurrentChange = async(pageNum) =>{
  currentPage.value=pageNum;
  await getQuePaper(pageNum);
}

function setActive(index) {
  activeIndex.value = index;
}
</script>

<template>
  <div class="question-square">
    <div class="square-box">
      <div class="box-head">
            我的问卷列表
      </div>
      <el-main>
          <div class="survey-list-container">
            <el-scrollbar>
              <div class="survey-list">
                <div
                    class="survey-item"
                    v-for="(survey, index) in surveys"
                    :key="index"
                    :class="{ active: activeIndex === index }"
                    :style="{ backgroundImage: `url(${images[index]})`, '--animation-order': index }"
                    @mouseover="setActive(index)"
                    @mouseleave="setActive(null)"
                >
                  <h2 class="survey-name" @click="view(survey.sid)">{{ survey.title }}
                    <!-- <span v-if="survey.type=='exam'"><el-icon><Document /></el-icon></span>
                    <span v-if="survey.type=='normal'"><el-icon :size="20"><Position /></el-icon></span> -->
                    &nbsp;&nbsp;
                    <span :class="survey.state === 'open' ? 'dot-green' : 'dot-gray'"></span>
                    <span style="font-size: 18px;">{{ survey.state === 'open' ? '已发布' : '未发布' }}</span></h2>
                  <h3 >问卷数：{{survey.fillNum}}</h3>
                  <h4>创建时间：{{survey.createTime}}</h4>
                  <!-- 填写逻辑 -->
                  <button class="fill-button" @click="analyseResult(survey.sid)">查看</button>
                  <button class="fill-button1" @click="shareOrCloseQuestionnaire(survey.sid, survey.state)">发布</button>
                  <button class="fill-button2" @click="modify(survey.sid)">修改</button>
                  <el-dropdown class="drop">
                    <span  style="font-size: 3vh;">
                      ...
                      
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu @mouseover="setActive(index)"
                      @mouseleave="setActive(null)">
                        <el-dropdown-item class="drop1" ><el-button :icon="Download"  @click="exportResult(survey.sid)">下载</el-button></el-dropdown-item>
                        <el-dropdown-item class="drop1" ><el-button :icon="Star"  >关注</el-button></el-dropdown-item>
                        <el-dropdown-item class="drop1"><el-button :icon="Delete" @click="deleteQuestionnaire(survey.sid)">删除</el-button></el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>              
              </div>
                  <div class="example-pagination-block">                 
                    <el-pagination style="margin-left: 120vh;"
                      v-model:current-page="currentPage"
                      :page-size="10"
                      layout="total,  prev, pager, next, jumper"
                      :total="total"
                      @current-change="(page) => handleCurrentChange(page )"
                    />
                  </div>
            </el-scrollbar>
                  
          </div>
      </el-main>
    </div>
    <el-dialog v-model="dialogVisible" title="提示信息" width="30%" @close="dialogVisible = false">
      <span>{{ dialogMessage }}</span> <br>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>

.square-box{
  border-radius: 20px;
  //height: 400px;
  //width: auto;
  background: var(--pane-color);
  padding: 20px;

}

.box-head{
  font-size: 20px;
  color: var(--theme-info-text-color);
}
.survey-list-container {
  height: 55vh;
  width: auto;
  //border: 1px solid #ccc; /* 添加边框 */
  //align-items: center;
  //background: black;

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
el-scrollbar{
  direction: ltr;
}
.example-pagination-block + .example-pagination-block {
  margin-top: 0px;
  
}
.example-pagination-block .example-demonstration {
  margin-bottom: 0px;
}
.survey-list {

  display: flex;
  flex-direction: row;
  align-items: center;
  margin-top: 10px;


}

.survey-item {
  //display: flex;
  //justify-content: space-between; /* 两端对齐 */
  ////align-items: center;
  //padding: 10px;
  //border: 1px solid var(--border-color);
  //margin: 5px;
  //border-radius: 20px;
  //align-items: center;
  position: relative;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  height: 45vh;
  border-radius: 30px;
  color: #fff;
  cursor: pointer;
  min-width: 70px;
  flex:0.3;
  margin: 10px;
  position: relative;
  transition: all 300ms ease-in;
  transform: translateX(100%); /* 初始状态从右侧外面开始 */
  opacity: 0; /* 开始时不可见 */
  animation: slideInFromRight 0.5s ease-out forwards;
}
.survey-item.active{
    min-width: 350px;
    //flex:5;
    transform: translateX(-230px); // 向左移动，230px是宽度差(300px - 70px)

}

.survey-name {
  font-size: 28px;
  position: absolute;
  bottom: 80px;
  left: 30px;
  margin: 0;
  opacity: 0;

}
.survey-item h3{
  font-size: 14px;
  position: absolute; //位置将相对于最近的定位祖先元素（即设置了 position 属性的父元素）进行定位。
  bottom: 60px;   //将 h3 元素的底部距离其定位祖先的底部 50 像素。
  left: 30px;   //将 h3 元素的左侧距离其定位祖先的左侧 30 像素。
  margin: 0;   //将 h3 元素的外边距设置为 0，确保没有额外的空间。
  opacity: 0;
}
.survey-item h4{
  font-size: 14px;
  position: absolute; //位置将相对于最近的定位祖先元素（即设置了 position 属性的父元素）进行定位。
  bottom: 40px;   //将 h3 元素的底部距离其定位祖先的底部 50 像素。
  left: 30px;   //将 h3 元素的左侧距离其定位祖先的左侧 30 像素。
  margin: 0;   //将 h3 元素的外边距设置为 0，确保没有额外的空间。
  opacity: 0;
}
.drop{
  color:#fff;
  border: none;
  cursor: pointer;
  position: absolute; //位置将相对于最近的定位祖先元素（即设置了 position 属性的父元素）进行定位。
  top:10px;   //将 h3 元素的底部距离其定位祖先的底部 50 像素。
  right: 10px;   //将 h3 元素的左侧距离其定位祖先的左侧 30 像素。
  margin: 0;   //将 h3 元素的外边距设置为 0，确保没有额外的空间。
  opacity: 0;
}

.survey-item.active .drop{
  opacity: 1;
  transition: opacity 0.2s ease-in 0.3s;
}
.survey-item.active .drop1{
  opacity: 1;
  transition: opacity 0.2s ease-in 0.3s;
}
.example-showcase .el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
.survey-item.active h2  {
  opacity: 1;
  transition: opacity 0.2s ease-in 0.3s;
}
.survey-item.active h3{
  opacity: 1;
  transition: opacity 0.2s ease-in 0.3s;
}
.survey-item.active h4{
  opacity: 1;
  transition: opacity 0.2s ease-in 0.3s;
}
@media (max-width: 480px) {
  .survey-list {
    width: 100vw;
  }

  .survey-item:nth-of-type(4),
  .survey-item:nth-of-type(5) {
    display: none;
  }
}
@keyframes slideInFromRight {
  0% {
    transform: translateX(100%);
    opacity: 0;
  }
  100% {
    transform: translateX(0);
    opacity: 1;
  }
}


.fill-button {
  position: absolute;
  background-color: var(--button-color);
  color: var(--theme-info-text-color);
  border: none;
  padding: 5px 10px;
  border-radius: 10px;
  cursor: pointer;
  left:30px;
  bottom: 10px;
  opacity: 0;
}
.fill-button1 {
  position: absolute;
  background-color: var(--button-color);
  color: var(--theme-info-text-color);
  border: none;
  padding: 5px 10px;
  border-radius: 10px;
  cursor: pointer;
  left:80px;
  bottom: 10px;
  opacity: 0;
}
.fill-button2 {
  position: absolute;
  background-color: var(--button-color);
  color: var(--theme-info-text-color);
  border: none;
  padding: 5px 10px;
  border-radius: 10px;
  cursor: pointer;
  left:130px;
  bottom: 10px;
  opacity: 0;
}
el-button{
  position: absolute;
  background-color: var(--button-color);
  color: var(--theme-info-text-color);
  border: none;
  padding: 5px 10px;
  border-radius: 10px;
  cursor: pointer;
  left:130px;
  bottom: 10px;
  opacity: 0;
}
.fill-button:hover {
  background-color: var(--button-hover-color);
}
.fill-button1:hover {
  background-color: var(--button-hover-color);
}
.fill-button2:hover {
  background-color: var(--button-hover-color);
}
.survey-item.active button{
  opacity: 1;
  transition: opacity 0.2s ease-in 0.3s;
}

</style>