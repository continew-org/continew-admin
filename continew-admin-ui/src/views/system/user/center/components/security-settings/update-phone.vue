<template>
  <a-list-item-meta>
    <template #avatar>
      <a-typography-paragraph>
        {{ $t('userCenter.securitySettings.phone.label') }}
      </a-typography-paragraph>
    </template>
    <template #description>
      <div class="tip">
        {{ $t('userCenter.securitySettings.phone.tip') }}
      </div>
      <div class="content">
        <a-typography-paragraph v-if="userStore.phone">
          {{ userStore.phone }}
        </a-typography-paragraph>
        <a-typography-paragraph v-else class="tip">
          {{ $t('userCenter.securitySettings.phone.content') }}
        </a-typography-paragraph>
      </div>
      <div class="operation">
        <a-link disabled :title="$t('userCenter.securitySettings.button.update')">
          {{ $t('userCenter.securitySettings.button.update') }}
        </a-link>
      </div>
    </template>
  </a-list-item-meta>
</template>

<script lang="ts" setup>
  import { useUserStore } from '@/store';

  const userStore = useUserStore();
</script>

<style scoped lang="less"></style>
