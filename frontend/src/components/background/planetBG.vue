<script setup>
import { onMounted } from 'vue';
import Parallax from 'parallax-js';

onMounted(() => {
  const scene = document.getElementById('scene');
  new Parallax(scene);
});
</script>

<template>
  <div id="scene">
    <div data-depth="0.1" class="bg">
      <img src="../../assets/img/planet/background.png" alt="" />
    </div>
    <div data-depth="0.2" class="rock1">
      <img src="../../assets/img/planet/rock.png" alt="" />
    </div>
    <div data-depth="1.2" class="earth">
      <img src="../../assets/img/planet/earth.png" alt="" />
    </div>
    <div data-depth="0.4" class="mid">
      <img src="../../assets/img/planet/mid.png" alt="" />
    </div>
    <div data-depth="0.1" class="fore">
      <img src="../../assets/img/planet/foreground.png" alt="" />
    </div>
  </div>
</template>

<style scoped>
@font-face {
  font-family: PlastoTrial_ExtraLight;
  font-weight: 700;
  src: url(~/assets/font/PlastoTrial-ExtraLight.otf) format("truetype");
  text-rendering: optimizeLegibility;
}

@font-face {
  font-family: PlastoTrial_ExtraBold;
  font-weight: 700;
  src: url(~/assets/font/PlastoTrial-ExtraBold.otf) format("truetype");
  text-rendering: optimizeLegibility;
}



#scene {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: black;
  display: flex;
  align-items: center;
}
.bg img {
  width: 110vw;
  height: 110vh;
  filter: brightness(0.6);
}

.rock1 {
  position: absolute !important;
  left: 10vw !important;
  top: 20vh !important;
}

.rock1 img {
  width: 40%;
  filter: brightness(0.5);
}

.text h1 {
  font-family: "AlphaCentauri" ;
  font-size: 5vw;
  color: #fff;
  letter-spacing: 20px;
  position: absolute;
  top: 35vh;
  left: 34vw;
}

.earth img {
  width: 400px;
  height: 400px;
  position: absolute;
  left: 34vw;
  top: 25vh;
  z-index: 1;
  filter: brightness(0.7);
}

.mid {
  left: -150px !important;
  bottom: -100px !important;
  top: unset !important;
}

.mid img {
  width: 100%;
  z-index: 2;
  left: -20px;
  top: 20vh !important;
  filter: brightness(0.8);
}

.fore {
  right: -50px !important;
  bottom: -200px !important;
  top: 0px !important;
  left: unset !important;
}

.fore img {
  width: 400px;
  height: 110vh;
  z-index: 3;
  filter: brightness(0.5);
}
</style>