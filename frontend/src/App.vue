<template>
  <div id="app">
    <div id="BG">
      <!-- 自定义组件，名为planet-b-g -->
      <!-- 使用了Vue的条件渲染指令v-if。条件是$route.meta.requiresParallax，意味着只有当路由的元信息（meta）中requiresParallax为真时，这个组件才会被渲染。 -->
      <planet-b-g v-if="$route.meta.requiresParallax"/>
      <!-- 自定义组件，名为gradient-background，也使用了v-if条件渲染指令。条件是$route.meta.requiresGradient，只有当路由的元信息中requiresGradient为真时，这个组件才会被渲染。 -->
      <gradient-background v-if="$route.meta.requiresGradient"/>
    </div>

    <router-view/>
  </div>
</template>

<script>
// 通过这些导入和注册，Vue.js可以在模板部分（<template>）中直接使用这些组件
import PlanetBG from "@/components/background/planetBG2.vue";
import GradientBackground from "@/components/background/gradient-background.vue";

export default {
  name: 'App',
  components: {
    GradientBackground,
    PlanetBG,
  },
};
</script>

<style>
#BG{
  position: fixed;
  width: 100%;
  height: 100vh;
  z-index: -10;
}

</style>