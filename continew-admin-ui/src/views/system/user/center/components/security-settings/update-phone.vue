<template>
  <a-list-item-meta>
    <template #avatar>
      <a-typography-paragraph>
        {{ $t('userCenter.securitySettings.updatePhone.label.phone') }}
      </a-typography-paragraph>
    </template>
    <template #description>
      <div class="content">
        <a-typography-paragraph v-if="userStore.phone">
          {{
            $t(
              'userCenter.securitySettings.updatePhone.placeholder.success.phone'
            )
          }}：{{ userStore.phone }}
        </a-typography-paragraph>
        <a-typography-paragraph v-else class="tip">
          {{
            $t(
              'userCenter.securitySettings.updatePhone.placeholder.error.phone'
            )
          }}
        </a-typography-paragraph>
      </div>
      <div class="operation">
        <a-link :title="$t('userCenter.securitySettings.button.update')">
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
