<template>
  <a-list :bordered="false">
    <a-list-item>
      <UpdatePwd />
    </a-list-item>
    <a-list-item>
      <UpdatePhone />
    </a-list-item>
    <a-list-item>
      <UpdateEmail />
    </a-list-item>
  </a-list>
</template>

<script lang="ts" setup>
  import UpdatePwd from './security-settings/update-pwd.vue';
  import UpdatePhone from './security-settings/update-phone.vue';
  import UpdateEmail from './security-settings/update-email.vue';
</script>

<style scoped lang="less">
  :deep(.arco-list-item) {
    border-bottom: none !important;
    .arco-typography {
      margin-bottom: 20px;
    }
    .arco-list-item-meta-avatar {
      margin-bottom: 1px;
    }
    .arco-list-item-meta {
      padding: 0;
    }
  }

  :deep(.arco-list-item-meta-content) {
    flex: 1;
    border-bottom: 1px solid var(--color-neutral-3);
    .arco-list-item-meta-description {
      display: flex;
      flex-flow: row;
      justify-content: space-between;
      .tip {
        color: rgb(var(--gray-6));
      }
      .operation {
        margin-right: 6px;
      }
    }
  }
</style>
