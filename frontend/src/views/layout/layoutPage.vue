<!-- <script setup lang="ts">
import SideBar from '@/components/sideBar.vue';
</script>

<template>
    <SideBar></SideBar>
    <router-view></router-view>
</template> -->

<script setup lang="ts">
import SideBar from '@/components/sideBar.vue';
</script>

<template>
  <div>
    <!-- <h1>Survey Planet</h1> -->
  </div>
  <div class="container">
    <SideBar style="margin-top: 20px;"/>
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex; /* 使用 flexbox 布局 */
  /* background-color: rgb(229, 234, 234); */
}

.content {
  margin-left: 20px; /* 给侧边栏留出空间 */
  
  padding: 0px;
  flex-grow: 1; /* 让内容区域填满剩余空间 */
  transition: margin-left 0.5s ease; /* 过渡效果 */
}

/* .menu-wrapper.expanded ~ .content {
  margin-left: 20px; /* 当菜单展开时，右侧内容区域左边距调整
} */
/* h1{
      position: relative;
      letter-spacing:15px;
      text-transform:uppercase;
      text-align: center;
      line-height: 0.7em;
      outline: none;
      -webkit-box-reflect: below 1px linear-gradient(transparent,#0008);
      color: #0e3742;
      animation:animate 5s linear infinite;
    }
    @keyframes animate{
      0%,21%,31%,51%,60%,80%,90%,92%{
        color:#0e3742;
      }

      20%,30%,50%,60%,80%,90%,100%{
      color: #fff;
      text-shadow:00 1px #03bcf4,
      0 0 2px #03bcf4,
      0 0 4px #03bcf4,
      0 0 8px #03bcf4;
      }
    } */
</style>
