<script setup>
import { PieChart, Promotion, InfoFilled, DocumentCopy } from '@element-plus/icons-vue'
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import router from "@/router";
import { userGetQuestionnaire, userShareQuestionnaire, userShareQuestionnaireWithEmail, userCloseQuestionnaire } from "@/api/questionnaire";

const route = useRoute();
const sid = route.query.id;
const switchMode = (newMode) => {
  if (newMode == 0) {
    router.push({ path: '/questionnaire/lookQuestionnaire', query: { id: sid } });
  }
  if (newMode == 2) {
    router.push({ path: '/responseSurvey', query: { id: sid } });
  }
};

const state = ref(null);
const shareLink = ref('');

onMounted(async () => {
  const res = await userGetQuestionnaire(sid);
  if (res.msg === "success") {
    state.value = res.data.state;
    if (state.value === 'open') {
      const res = await userShareQuestionnaire(sid);
      if (res.msg === 'success') {
        shareLink.value = res.data;
      }
    }
  }
});

const publishQuestionnaire = async () => {
  if (state.value === 'close') {
    const res = await userShareQuestionnaire(sid);
    if (res.msg === 'success') {
      shareLink.value = res.data;
      ElMessage.success('问卷发布成功');
      window.location.reload();
    } else {
      ElMessage.error('发布失败');
    }
  } else {
    ElMessage.info('当前状态为关闭，无法发布问卷');
  }
};

const copyLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制');
  });
};

const openLink = () => {
  window.open(shareLink.value, '_blank');
};

const emails = ref('');
const inviteMessage = ref('');
const needSiteNotification = ref(false);

const shareQuestionnaire = async () => {
  const emailArray = emails.value.split(',').map(email => email.trim());
  const validEmails = emailArray.filter(email => email !== '');
  const payload = {
    emails: validEmails,
    needSiteNotification: needSiteNotification.value,
    invitationMessage: inviteMessage.value,
  };
  try {
    const res = await userShareQuestionnaireWithEmail(sid, payload);
    if (res.msg === 'success') {
      ElMessage.success('问卷分享成功');
    } else {
      ElMessage.error('分享失败，请重试');
    }
  } catch (error) {
    ElMessage.error('分享失败，请重试');
  }
};

const closeQuestionnaire = async () => {
  const res = await userCloseQuestionnaire(sid);
  if (res.msg === 'success') {
    ElMessage.success('问卷关闭成功');
    state.value = 'close'; // 更新为关闭状态
  } else {
    ElMessage.error('关闭失败');
  }
};
</script>

<template>
  <div class="main-container">
    <div style="display: flex; justify-content: center; gap: 10px;">
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

    <!-- 问卷链接和关闭问卷区域 -->
    <div v-if="state === 'open'" class="questionnaire-link-container" style="margin-top: 20px;">
      <el-card>
        <p>问卷链接：</p>
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-input v-model="shareLink" readonly style="width: 500px;" />
          <el-button type="primary" @click="copyLink">复制链接</el-button>
          <el-button type="success" @click="openLink">打开链接</el-button> 
        </div>
      </el-card>
      <el-button @click="closeQuestionnaire" type="danger" style="margin-top: 20px;">关闭问卷</el-button>
    </div>

    <!-- 发布问卷按钮区域，仅在问卷状态为 "close" 时显示 -->
    <div v-if="state === 'close'" class="publish-container" style="margin-top: 20px;">
      <el-button 
        style="background-color: #409EFF; border: none; padding: 10px 20px; cursor: pointer; color: white; font-size: 14px; border-radius: 5px;" 
        @click="publishQuestionnaire">
        发布问卷
      </el-button>
    </div>

    <!-- 通过邮件发送区域 -->
    <div v-if="state === 'open'" class="email-share-container" style="margin-top: 40px;">
      <h3>通过邮件发布</h3>
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

        <div style="margin-top: 20px;">
          <el-button type="primary" @click="shareQuestionnaire">分享</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.main-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.navbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #333;
  font-size: 25px;
  padding: 5px 8px;
  width: 170px;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.navbar-item:hover {
  color: #007BFF;
}

.el-input {
  font-size: 14px;
}

.el-button {
  font-size: 14px;
}

.questionnaire-link-container,
.email-share-container,
.publish-container {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
