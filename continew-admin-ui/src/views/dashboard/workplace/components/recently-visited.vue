<template>
  <a-card
    class="general-card"
    :title="$t('workplace.recently.visited')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ paddingTop: '26px' }"
  >
    <div style="margin-bottom: -1rem">
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
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const links = [
    { text: '在线用户', icon: 'anonymity', path: '/monitor/online' },
    { text: '代码生成', icon: 'code', path: '/tool/generator' },
    { text: '角色管理', icon: 'safe', path: '/system/role' },
  ];
</script>

<style lang="less" scoped>
  :deep(.arco-card-header-title) {
    line-height: inherit;
  }
</style>
