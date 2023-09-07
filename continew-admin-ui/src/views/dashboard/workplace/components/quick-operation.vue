<template>
  <a-card
    class="general-card"
    :title="$t('workplace.quick.operation')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '24px 20px 0 20px' }"
  >
    <a-row :gutter="8">
      <a-col v-for="link in links" :key="link.text" :span="8" class="wrapper">
        <div @click="router.replace({ path: link.path })">
          <div class="icon">
            <svg-icon :icon-class="link.icon" />
          </div>
          <a-typography-paragraph class="text">
            {{ link.text }}
          </a-typography-paragraph>
        </div>
      </a-col>
    </a-row>
    <a-divider class="split-line" style="margin: 0" />
  </a-card>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const links = [
    { text: '用户管理', icon: 'user', path: '/system/user' },
    { text: '角色管理', icon: 'safe', path: '/system/role' },
    { text: '部门管理', icon: 'user-group', path: '/system/dept' },
    { text: '代码生成', icon: 'code', path: '/tool/generator' },
    { text: '在线用户', icon: 'anonymity', path: '/monitor/online' },
    { text: '操作日志', icon: 'history', path: '/monitor/log/operation' },
  ];
</script>

<style scoped lang="less"></style>
