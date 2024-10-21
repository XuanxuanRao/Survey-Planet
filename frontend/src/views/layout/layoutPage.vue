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
  <div class="container">
    <SideBar />
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

</style>
