<template>
  <a-carousel
    indicator-type="slider"
    show-arrow="hover"
    auto-play
    style="width: 100%; height: 282px; border-radius: 4px; overflow: hidden"
  >
    <a-carousel-item v-for="(src, idx) in imageSrc" :key="idx">
      <div style="padding: 10px 30px 15px">
        <div style="margin-bottom: 5px; text-align: center"
          >相互 (huá shuǐ) 交流 (mō yú)</div
        >
        <img :src="src" style="width: 100%" alt="QrCode" />
        <div style="text-align: center">扫码加VX，备注 cnadmin 进群</div>
      </div>
    </a-carousel-item>
  </a-carousel>
</template>

<script lang="ts" setup>
  const imageSrc = [
    `https://doc.charles7c.top/qrcode.jpg?${new Date().getTime()}`,
  ];
</script>
