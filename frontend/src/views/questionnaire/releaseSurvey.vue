<!-- 直接在 <script setup> 中定义的变量和函数会自动暴露给模板，无需显式返回。有setup和无setup辨别 -->
<script setup>
import { PieChart, Promotion,InfoFilled } from '@element-plus/icons-vue'
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import router from "@/router";
import { useRouter } from 'vue-router'
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
// 初始化加载数据
onMounted(async () => {

  console.log(sid);
});
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