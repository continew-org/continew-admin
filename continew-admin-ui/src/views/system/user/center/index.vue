<template>
  <div class="container">
    <Breadcrumb :items="['menu.user.center']" />
    <a-row style="margin-bottom: 16px">
      <a-col :span="24">
        <UserPanel />
      </a-col>
    </a-row>
    <a-row class="wrapper">
      <a-col :span="24">
        <a-tabs
          v-model:active-key="activeKey"
          default-active-key="1"
          type="rounded"
        >
          <a-tab-pane key="1" :title="$t('userCenter.tab.basicInfo')">
            <BasicInfo />
          </a-tab-pane>
          <a-tab-pane key="2" :title="$t('userCenter.tab.securitySettings')">
            <SecuritySettings />
          </a-tab-pane>
          <a-tab-pane key="3" :title="$t('userCenter.tab.operationLog')">
            <OperationLog />
          </a-tab-pane>
        </a-tabs>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { useRoute } from 'vue-router';
  import UserPanel from './components/user-panel.vue';
  import BasicInfo from './components/basic-info.vue';
  import SecuritySettings from './components/security-settings.vue';
  import OperationLog from './components/operation-log.vue';

  const route = useRoute();
  const activeKey = ref('1');
  const tab = route.query.tab as string;

  onMounted(() => {
    if (tab === 'security-setting') {
      activeKey.value = '2';
    }
  });
</script>

<script lang="ts">
  export default {
    name: 'UserCenter',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }

  .wrapper {
    padding: 20px 0 0 20px;
    min-height: 580px;
    background-color: var(--color-bg-2);
    border-radius: 4px;
  }

  :deep(.section-title) {
    margin-top: 0;
    margin-bottom: 16px;
    font-size: 14px;
  }
</style>
