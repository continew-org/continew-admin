<template>
  <a-list-item-meta>
    <template #avatar>
      <a-typography-paragraph>
        {{ $t('userCenter.securitySettings.updateEmail.label.email') }}
      </a-typography-paragraph>
    </template>
    <template #description>
      <div class="content">
        <a-typography-paragraph v-if="loginStore.email">
          {{ $t('userCenter.securitySettings.updateEmail.placeholder.success.email') }}：{{ loginStore.email }}
        </a-typography-paragraph>
        <a-typography-paragraph v-else class="tip">
          {{ $t('userCenter.securitySettings.updateEmail.placeholder.error.email') }}
        </a-typography-paragraph>
      </div>
      <div class="operation">
        <a-link>
          {{ $t('userCenter.securitySettings.button.update') }}
        </a-link>
      </div>
    </template>
  </a-list-item-meta>
</template>

<script lang="ts" setup>
  import { useLoginStore } from '@/store';

  const loginStore = useLoginStore();
</script>

<style scoped lang="less"></style>
