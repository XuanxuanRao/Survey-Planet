<script setup>
import { onMounted } from 'vue';
import Parallax from 'parallax-js';

onMounted(() => {
  const scene = document.getElementById('scene2');
  new Parallax(scene);
});
</script>

<template>
  <div id="scene2">
    <div data-depth="0.1" class="bg">
      <img src="../../assets/img/universe/background.png" alt="" />
    </div>

    <div data-depth="1" class="rocket">
      <img src="../../assets/img/universe/rocket.png" alt="" />
    </div>
    <div data-depth="0.8" class="planets">
      <img src="../../assets/img/universe/planets.png" alt="" />
    </div>
    <div data-depth="0.4" class="astronaut">
      <img src="../../assets/img/universe/astronaut.png" alt="" />
    </div>
    <div data-depth="0.5" class="planet1">
      <img src="../../assets/img/universe/planet1.png" alt="" />
    </div>
    <div data-depth="0.5" class="planet2">
      <img src="../../assets/img/universe/planet2.png" alt="" />
    </div>
    <div data-depth="0.7" class="planet3">
      <img src="../../assets/img/universe/planet3.png" alt="" />
    </div>


  </div>

</template>

<style scoped>
#scene2 {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color:rgb(26,26,26);
  display: flex;
  align-items: center;
}

.bg img {
  width: 110vw;
  height: 110vh;
}

.rocket img{
  width: 250px;
  position: absolute;
  left: 57vw;
  top: 43vh;
  z-index: 5;
}

.planets img{
  width: 700px;
  position: absolute;
  left: 73vw;
  top:65vh;
  z-index:1;
  filter:brightness(0.7);

}

.planet1 img{
  width: 250px;
  position: absolute;
  left:80vw;
  top:25vh;
}

.planet2 img{
  width: 120px;
  position: absolute;
  left:10vw;
  top:85vh;
}


.planet3 img{
  width: 300px;
  position: absolute;
  left:7vw;
  top:15vh;
}

.astronaut img{
  width: 900px;
  position: absolute;
  left: 20vw;
  top:63vh;
  filter:brightness(0.9);
}
</style>