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
    <a-list-item>
      <BindSocial />
    </a-list-item>
  </a-list>
</template>

<script lang="ts" setup>
  import UpdatePwd from './security-settings/update-pwd.vue';
  import UpdatePhone from './security-settings/update-phone.vue';
  import UpdateEmail from './security-settings/update-email.vue';
  import BindSocial from './security-settings/bind-social.vue';
</script>

<style scoped lang="less">
  :deep(.arco-list-item) {
    border-bottom: none !important;
    .arco-typography {
      margin-bottom: 20px;
    }
    .arco-list-item-meta-avatar {
      width: 70px;
      margin-right: 24px;
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
      .tip {
        width: 50%;
        color: rgb(var(--gray-6));
        margin-right: 24px;
      }
      .content {
        flex: 1 1 0;
      }
      .operation {
        margin-right: 6px;
      }
    }
  }
</style>
