<script>
import { ref } from 'vue';
import { User, Document, PieChart, EditPen, Tickets, ChatDotRound,SwitchButton } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
export default {
  components: {
    User,
    Document,
    PieChart,
    EditPen,
    Tickets,
    ChatDotRound,
    SwitchButton
  },
  setup() {
    // 响应式变量
    const isExpanded = ref(false);
    const userStore = useUserStore();
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
    const logout = () => {
      userStore.removeToken(); // 调用 removeToken 方法
      router.push({ path: '/login' })
      
    }
    

    return {
      isExpanded,
      expandSideBar,
      collapseSideBar,
      createQuestionnaire,
      showSquare,
      fillQuestionnaire,
      logout,
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
          <span v-if="isExpanded" class="item-name">问卷中心</span>
        </li>
        <li class="menu-item" @click="fillQuestionnaire">
          <el-icon class="menu-icon" ><EditPen /></el-icon>
          <span v-if="isExpanded" class="item-name">填写历史</span>
        </li>
        <li class="menu-item" @click="userCenter">
          <el-icon class="menu-icon"><User /></el-icon>
          <span v-if="isExpanded" class="item-name">用户中心</span>
        </li>
        <li class="menu-item" @click="logout">
          <el-icon class="menu-icon"><SwitchButton /></el-icon>
          <span v-if="isExpanded" class="item-name">退出登录</span>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.menu-wrapper {
  width: 70px; /* 折叠状态的宽度 */
  height: 300px; /* 侧边栏高度 */
  background-color: #f0f0f0; /* 淡灰色背景 */
  border-radius: 30px;
  transition: width 0.5s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin-top: 20px;
  border: 1px solid lightblue;;
}

.item-name {
    
    display: inline-block;
    margin-left: 10px;
    font-size: 15px;
    font-weight: 100;
    transition: opacity 0.6s;
    opacity: 0;
  }

.menu-wrapper.expanded {
  width: 150px; /* 展开状态的宽度 */
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
  transition: background-color 0.8s ease;
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
