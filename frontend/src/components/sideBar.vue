<script>
import { ref } from 'vue';
import { User, Document, PieChart, EditPen, Tickets, ChatDotRound } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

export default {
  components: {
    User,
    Document,
    PieChart,
    EditPen,
    Tickets,
    ChatDotRound
  },
  setup() {
    // 响应式变量
    const isExpanded = ref(false);

    // 展开侧边栏
    const expandSideBar = () => {
      isExpanded.value = true;
    };

    // 折叠侧边栏
    const collapseSideBar = () => {
      isExpanded.value = false;
    };

    const router = useRouter()
    const createQuestionnaire = () => {
      router.push({ path: '/questionnaire/createQuestionnaire' })
    }
    const showSquare = () => {
      // router.push({ path: '/questionnaire/questionnaireSquare' })
      router.push({ path: '/viewResult'})
    }
    const fillQuestionnaire = () => {
      router.push({ path: '/questionnaire/fillQuestionnaire' })
    }
    const userCenter = () => {
      router.push({ path: '/userCenter' })
    }

    return {
      isExpanded,
      expandSideBar,
      collapseSideBar,
      createQuestionnaire,
      showSquare,
      fillQuestionnaire,
      userCenter
    };
  }
};
</script>

<template>
  <div
    class="menu-wrapper"
    :class="{ expanded: isExpanded }"
    @mouseenter="expandSideBar"
    @mouseleave="collapseSideBar"
  >
    <div class="menu-content">
      <ul>
        <li class="menu-item" @click="createQuestionnaire">
          <el-icon class="menu-icon"><Tickets /></el-icon>
          <span v-if="isExpanded">主页</span>
        </li>
        <li class="menu-item" @click="fillQuestionnaire">
          <el-icon class="menu-icon"><EditPen /></el-icon>
          <span v-if="isExpanded">历史</span>
        </li>
        <!-- <li class="menu-item" @click="showSquare">
          <el-icon class="menu-icon"><ChatDotRound /></el-icon>
          <span v-if="isExpanded">广场</span>
        </li> -->
        <li class="menu-item" @click="userCenter">
          <el-icon class="menu-icon"><User /></el-icon>
          <span v-if="isExpanded">用户</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.menu-wrapper {
  width: 80px; /* 折叠状态的宽度 */
  height: 225px; /* 侧边栏高度 */
  background-color: white; /* 背景颜色 */
  border-radius: 0 10px 10px 0; /* 圆角 */
  transition: width 0.5s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin-top: 20px;
}

.menu-wrapper.expanded {
  width: 100px; /* 展开状态的宽度 */
}

.menu-content {
  padding: 10px;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: center; /* 使内容在横向居中 */
  padding: 25px 0;
  color: black; /* 菜单项文字颜色 */
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.menu-item.active {
  color: black;
  font-weight: bold;
}

.menu-item:hover {
  background-color: #d9e2e390; /* 悬停背景颜色 */
}

.menu-icon {
  margin-right: 10px; /* 图标与文本之间的间距 */
  display: flex;
  align-items: center; /* 图标垂直居中 */
  justify-content: center; /* 图标水平居中 */
}

.menu-item span {
  transition: opacity 0.3s ease; /* 添加渐变效果 */
  opacity: 1; /* 默认可见 */
}

.menu-wrapper:not(.expanded) .menu-item span {
  opacity: 0; /* 收缩时隐藏文本 */
  width: 0; /* 确保文本占用空间为0 */
}
</style>
