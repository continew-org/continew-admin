<template>
  <div class="container">
    <div class="logo">
      <img :src="getFile(appStore.getLogo)" alt="logo" height="33" />
      <div class="logo-text">{{ appStore.getTitle }}</div>
    </div>
    <LoginBanner />
    <div class="content">
      <div class="content-inner">
        <LoginForm />
      </div>
      <div class="footer">
        <Footer />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import Footer from '@/components/footer/index.vue';
  import { useAppStore } from '@/store';
  import getFile from '@/utils/file';
  import LoginBanner from './components/banner.vue';
  import LoginForm from './components/login-form.vue';

  const appStore = useAppStore();
</script>

<style lang="less" scoped>
  .container {
    display: flex;
    height: 100vh;
    .banner {
      width: 550px;
      background: linear-gradient(163.85deg, #1d2129 0%, #00308f 100%);
    }
    .content {
      position: relative;
      display: flex;
      flex: 1;
      align-items: center;
      justify-content: center;
      padding-bottom: 40px;
    }
    .footer {
      position: absolute;
      right: 0;
      bottom: 0;
      width: 100%;
    }
  }

  .logo {
    position: fixed;
    top: 24px;
    left: 22px;
    z-index: 1;
    display: inline-flex;
    align-items: center;

    &-text {
      margin-right: 4px;
      margin-left: 4px;
      color: var(--color-fill-1);
      font-size: 20px;
    }
  }

  @media (max-width: @screen-lg) {
    .container {
      .banner {
        width: 25%;
      }
    }
  }
</style>
