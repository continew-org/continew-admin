<template>
  <a-carousel
    indicator-type="slider"
    show-arrow="hover"
    auto-play
    style="width: 100%; height: 300px; border-radius: 4px; overflow: hidden"
  >
    <a-carousel-item v-for="(src, idx) in imageSrc" :key="idx">
      <div>
        <img :src="src" style="width: 100%" alt="QrCode" />
      </div>
    </a-carousel-item>
  </a-carousel>
</template>

<script lang="ts" setup>
  const imageSrc = ['https://doc.charles7c.top/qrcode.jpg'];
</script>
