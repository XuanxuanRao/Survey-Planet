<!-- 直接在 <script setup> 中定义的变量和函数会自动暴露给模板，无需显式返回。有setup和无setup辨别 -->
<script setup>
import { PieChart, Promotion,InfoFilled } from '@element-plus/icons-vue'
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import router from "@/router";
import { userGetQuestionnaire, userShareQuestionnaire, userShareQuestionnaireWithEmail } from "@/api/questionnaire";


const route = useRoute();
const sid = route.query.id;
const switchMode = (newMode) => {
  if(newMode == 0) {
    router.push({ path: '/questionnaire/lookQuestionnaire', query: { id: sid } });
  }
  if(newMode == 2){
    router.push({ path: '/responseSurvey', query: { id: sid } });
  }
};

const state = ref(null)
const shareLink = ref('');

onMounted(async () => {
  const res = await userGetQuestionnaire(sid)
  if(res.msg === "success") {
    state.value = res.data.state
    console.log(state.value)
    if(state.value === 'open') {
      const res = await userShareQuestionnaire(sid)
      if(res.msg === 'success') {
        shareLink.value = res.data
      }
    }
  }
})

// 发布问卷逻辑
const publishQuestionnaire = async () => {
  if (state.value === 'close') {
    // 调用接口获取填写链接
    const res = await userShareQuestionnaire(sid);
    if (res.msg === 'success') {
      shareLink.value = res.data
      ElMessage.success('问卷发布成功')
      window.location.reload();
    } else {
      ElMessage.error('发布失败');
    }
  } else {
    ElMessage.info('当前状态为关闭，无法发布问卷');
  }
}


const showDialog = ref(false);
const emails = ref('');
const inviteMessage = ref('');
const needSiteNotification = ref(false);

// 分享问卷
const shareQuestionnaire = async () => {
  // const loading = ElLoading.service({
  //   target: document.body, // 加载的目标区域
  //   text: '正在分享问卷，请稍候...', // 自定义加载文本
  //   spinner: 'el-icon-loading'
  // });
  const emailArray = emails.value.split(',').map(email => email.trim());
  const validEmails = emailArray.filter(email => email !== '');
  const payload = {
    emails: validEmails,
    needSiteNotification: needSiteNotification.value,
    invitationMessage: inviteMessage.value,
  }
  try {
    const res = await userShareQuestionnaireWithEmail(sid, payload);
    if (res.msg === 'success') {
      ElMessage.success('问卷分享成功');
    } else {
      ElMessage.error('分享失败，请重试');
    }
  } catch (error) {
    ElMessage.error('分享失败，请重试');
  } finally {
    //loading.close();
  }
  showDialog.value = false
}

</script>
<template>
  <div style="display: flex;justify-content: center; gap: 10px;">
    <button class="navbar-item" style="background: none; border: none; padding: 0; cursor: pointer;" @click="switchMode(0)">
        <el-icon :size="40"><PieChart /></el-icon>
        <span>统计分析</span>
    </button>
    <button class="navbar-item" style="background: none; border: none; padding: 0; cursor: pointer;" @click="switchMode(1)">
        <el-icon :size="40"><Promotion /></el-icon>
        <span>发布问卷</span>
    </button>
    <button class="navbar-item" style="background: none; border: none; padding: 0; cursor: pointer;" @click="switchMode(2)">
        <el-icon :size="40"><InfoFilled /></el-icon>
        <span>查看问卷</span>
    </button>
  </div>

  <!-- 发布问卷按钮，显示状态为close时不可用 -->
  <button 
    v-if="state === 'close'"
    style="
      background-color: #409EFF; 
      border: none; 
      padding: 10px 20px; 
      cursor: pointer; 
      color: white; 
      font-size: 14px; 
      border-radius: 5px; 
      transition: all 0.3s ease;
    "
    @click="publishQuestionnaire"
    @mouseover="hover = true"
    @mouseleave="hover = false"
  >
    <span :style="hover ? { color: '#fff', textDecoration: 'underline' } : { color: 'white' }">发布问卷</span>
  </button>


  <div v-if="state === 'open'">
    <p>问卷链接：</p>
    <a :href="shareLink" target="_blank">{{ shareLink }}</a>

    <el-button @click="showDialog = true" type="primary">分享问卷</el-button>
    
    <!-- 弹窗 -->
    <el-dialog v-model="showDialog" title="分享问卷">
      <el-form>
        <el-form-item label="邮箱列表">
          <el-input v-model="emails" placeholder="请输入邮箱地址，以逗号分隔" />
        </el-form-item>
        
        <el-form-item label="邀请信息">
          <el-input v-model="inviteMessage" placeholder="请输入邀请信息" />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="needSiteNotification">同步提示</el-checkbox>
        </el-form-item>
        
        <div slot="footer" class="dialog-footer">
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" @click="shareQuestionnaire">分享</el-button>
        </div>
      </el-form>
    </el-dialog>
  </div>


</template>

<style scoped>
/* 顶部导航栏 */
.navbar {
  display: flex; /* 使用 Flexbox 布局 */
  align-items: center; /* 垂直居中 */
  justify-content: space-around; /* 等间距分布 */
  background-color: #f8f8f8; /* 背景色 */
  border-bottom: 1px solid #ddd; /* 下边框 */
  padding: 10px 0; /* 内边距 */
  position: fixed; /* 固定在顶部 */
  top: 0;
  left: 0;
  width: 100%; /* 占满宽度 */
  z-index: 1000; /* 层级最高 */
}

/* 每个按钮的样式 */
.navbar-item {
    display: flex; /* 图标和文字水平排列 */
    flex-direction: column; /* 图标在上，文字在下 */
    align-items: center; /* 居中对齐 */
    text-decoration: none; /* 去掉下划线 */
    color: #333; /* 文本颜色 */
    font-size: 25px; /* 字体大小 */
    padding: 5px 8px; /* 内边距 */
    width: 170px; /* 每个按钮的固定宽度 */
    transition: background-color 0.3s ease, color 0.3s ease; /* 添加过渡效果 */
}

.navbar-item:hover {
  color: #007BFF; /* 悬停时文字变蓝 */
}

.navbar-item:hover, .navbar-item:focus {
    background-color: rgba(0, 123, 255, 0.2); /* 背景色变淡蓝 */
    color: #007BFF; /* 文字颜色变蓝 */
    border-radius: 5px; /* 圆角效果 */
}
</style>