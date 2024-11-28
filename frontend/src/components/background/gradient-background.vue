<script setup>

</script>

<template>
  <body>

  </body>
</template>

<style scoped>
body{
  /* 初始化 取消内外边距 */
  margin: 0;
  padding: 0;
  /* 100%窗口高度 */
  height: 100vh;
  /* 弹性布局 水平垂直居中 */
  display: flex;
  justify-content: center;
  align-items: center;
  /* 渐变背景 */
  /* background: linear-gradient(125deg, #ffffff, #ffe0e6, #ffcccb); */
  background: linear-gradient(125deg, white, #bbdefb, #ffffff);
  /* background: linear-gradient(125deg, white, #e1bee7, #bbdefb, #ffffff); */
  /* background: linear-gradient(125deg, #ffebee, #e1bee7, #bbdefb); */
  /* background: linear-gradient(125deg,#2c3e50,#27ae60,#2980b9,#e74c3c,#8e44ad); */
  /* background: linear-gradient(125deg, white, #ffe0e6, #ffffff); */
  /* background: linear-gradient(125deg, #ffffff, #e6f7ff, #d9f0ff, #b3e6ff, #80d9ff); */
  /* background: linear-gradient(125deg, #ffffff, #ffebcc, #ffd6a0, #ffbf80, #ffab60); */
  /* background: linear-gradient(125deg, #ffffff, #d9eaff, #b3d9ff, #80c6ff, #4daaff); */
  /* background: linear-gradient(125deg, #ffffff, #ffccff, #ffcc99, #ffff99, #ccffcc); */
  /* background: linear-gradient(125deg, #ffffff, #ffccff, #ffcc99, #ffff99, #ccffcc, #ccffff); */
  /* background: linear-gradient(125deg, #ffffff 0%, #ffccff 25%, #ffcc99 50%, #ffff99 75%, #ccffcc 100%); */
  /* background: linear-gradient(125deg, #ffffff 0%, #ffccff 20%, #ffcc99 40%, #ffff99 60%, #ccffcc 80%, #ccffff 100%); */
  /* 指定背景图像的大小 */
  background-size: 500%;
  /* 执行动画：动画名 时长 线性的 无限次播放 */
  animation: bgAnimation 40s linear infinite;
}

/* 定义动画 */
@keyframes bgAnimation {
  0%{
    background-position: 0% 50%;
  }
  50%{
    background-position: 100% 50%;
  }
  100%{
    background-position: 0% 50%;
  }
}
</style>