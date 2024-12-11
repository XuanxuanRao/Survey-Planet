<script>
import { onMounted, ref } from 'vue';
import { User, Document, PieChart, EditPen, Tickets, ChatDotRound,SwitchButton } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getUserInfo, updateUserInfo } from '@/api/user'
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
    const avatar = ref();
    const username = ref();
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
    const fetchUserInfo = async () => {
      try {
        const res = await getUserInfo()
        if (res.msg === 'success') {
          avatar.value = res.data.avatar
          username.value = res.data.username
          username
        } else {
          ElMessage.error('获取用户信息失败')
        }
      } catch (error) {
        console.error("获取用户信息出错：", error)
        ElMessage.error('获取用户信息时发生错误')
      }
    }

    onMounted(() => {
      fetchUserInfo()
    })

    return {
      isExpanded,
      expandSideBar,
      collapseSideBar,
      createQuestionnaire,
      showSquare,
      fillQuestionnaire,
      logout,
      userCenter,
      fetchUserInfo,
      avatar,
      username
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
    <div class="content">   
      <div class="person-info">
        <img :src="avatar"  alt="" />
        <div id="avatarUploader">
          <avatar-uploader1 @changed="fetchAvatar"/>
        </div>
        <div class="person-name">
          <div class="name">{{username}}</div>
          <!-- <span class="detail">这是你来问卷星球的第{{registerDays}}天</span> -->
        </div>
      </div>
      <div class="menu-content">
        <div class="menu-list">
          <div class="menu-list-item" @click="createQuestionnaire">
            <div class="block"></div>
            <span class="iconfont" ><el-icon class="menu-icon"><Tickets /></el-icon></span>
            <div class="item-name">问卷中心</div>
          </div>
          <div class="menu-list-item" @click="fillQuestionnaire">
            <div class="block"></div>
            <span class="iconfont" ><el-icon class="menu-icon" ><EditPen /></el-icon></span>
            <div class="item-name">填写历史</div>
          </div>
          <div class="menu-list-item" @click="userCenter">
            <div class="block"></div>
            <span class="iconfont" ><el-icon class="menu-icon"><User /></el-icon></span>
            <div class="item-name">用户中心</div>
          </div>
          <div class="menu-list-item" @click="logout">
            <div class="block"></div>
            <span class="iconfont" ><el-icon class="menu-icon"><SwitchButton /></el-icon></span>
            <div class="item-name">退出登录</div>  
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.menu-wrapper {
  margin-left: 25px;
  width: 70px;
  height: 50%;
  border-radius: 20px;
  background-color: rgb(240, 240, 240,0.6);
  padding: 10px;
  box-sizing: border-box;
  transition: width 0.6s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.menu-wrapper.expanded {
  width: 140px;
}

.content .person-info {
  margin-top: 20px;
  white-space: nowrap;
  img {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    border: 2px solid #fff;
    box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.2);
    vertical-align: middle;
  }
  #avatarUploader{
    position: absolute;
    z-index: 20;
    left:20px;
    top:33px;
    opacity: 0;
  }
  .person-name {
    margin-left: 15px;
    vertical-align: middle;
    transition: opacity 0.6s;
    opacity: 0;
    color: var(--theme-info-text-color);
    display: inline-block;
    .name {
      font-size: 20px;
      font-weight: 600;
    }
    .detail {
      font-size: 12px;
    }
  }
}
.menu-wrapper.expanded .content .person-info .person-name {
  opacity: 1;
}

.menu-content {
  border-top: 1px solid rgb(131, 128, 155);
  margin-top: 10px;
}

.menu-content .menu-list .menu-list-item {
  font-family: Marmelad_Regular, SansSerif;
  font-weight: 700;
  cursor: pointer;
  width: 100%;
  height: 50px;
  position: relative;
  border-radius: 10px;
  padding-left: 15px;
  white-space: nowrap;
  .block {
    width: 6px;
    height: 25px;
    background-color: #4c8eb4;
    position: absolute;
    right: 5px;
    top: 13px;
    transition: 0.5s;
    border-top-left-radius: 4px;
    border-bottom-left-radius: 4px;
    opacity: 0;
  }

  .item-name {
    line-height: 50px;
    display: inline-block;
    margin-left: 10px;
    font-size: 15px;
    color: rgb(131, 128, 155);
    font-weight: 100;
    transition: opacity 0.6s;
    opacity: 0;
  }

  &:hover {
    background-color: rgb(248, 247, 255,0.6);
    .item-name {
      color: #4c8eb4;
    }
    .iconfont {
      color: #4c8eb4;
    }
    .block {
      opacity: 1;
    }
  }


}
.menu-wrapper.expanded .menu-content .menu-list .menu-list-item .item-name,
.menu-wrapper.expanded .menu-content .menu-list .menu-list-item .btn {
  opacity: 1;
}
</style>
