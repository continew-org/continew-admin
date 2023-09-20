<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.config']" />
    <a-card class="general-card" :title="$t('menu.system.config')">
      <a-tabs default-active-key="1" type="rounded">
        <a-tab-pane key="1" title="基础配置（正在开发）">
          <BasicSetting />
        </a-tab-pane>
        <a-tab-pane key="2" title="高级配置（暂未开放）" disabled></a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import BasicSetting from './components/basic-setting.vue';
</script>

<script lang="ts">
  export default {
    name: 'Config',
  };
</script>

<style scoped lang="less">
  :deep(.arco-tabs-content) {
    padding-top: 5px;
  }
</style>
