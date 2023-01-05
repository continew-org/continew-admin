<template>
  <a-list :bordered="false">
    <a-list-item>
      <a-list-item-meta>
        <template #avatar>
          <a-typography-paragraph>
            {{ $t('userCenter.SecuritySettings.form.label.password') }}
          </a-typography-paragraph>
        </template>
        <template #description>
          <div class="content">
            <a-typography-paragraph v-if="loginStore.pwdResetTime">
              已设置
            </a-typography-paragraph>
            <a-typography-paragraph v-else class="tip">
              {{ $t('userCenter.SecuritySettings.placeholder.password') }}
            </a-typography-paragraph>
          </div>
          <div class="operation">
            <a-link>
              {{ $t('userCenter.SecuritySettings.button.update') }}
            </a-link>
          </div>
        </template>
      </a-list-item-meta>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta>
        <template #avatar>
          <a-typography-paragraph>
            {{ $t('userCenter.SecuritySettings.form.label.phone') }}
          </a-typography-paragraph>
        </template>
        <template #description>
          <div class="content">
            <a-typography-paragraph v-if="loginStore.phone">
              已绑定：{{ loginStore.phone }}
            </a-typography-paragraph>
            <a-typography-paragraph v-else class="tip">
              {{ $t('userCenter.SecuritySettings.placeholder.phone') }}
            </a-typography-paragraph>
          </div>
          <div class="operation">
            <a-link>
              {{ $t('userCenter.SecuritySettings.button.update') }}
            </a-link>
          </div>
        </template>
      </a-list-item-meta>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta>
        <template #avatar>
          <a-typography-paragraph>
            {{ $t('userCenter.SecuritySettings.form.label.email') }}
          </a-typography-paragraph>
        </template>
        <template #description>
          <div class="content">
            <a-typography-paragraph v-if="loginStore.email">
              已绑定：{{ loginStore.email }}
            </a-typography-paragraph>
            <a-typography-paragraph v-else class="tip">
              {{ $t('userCenter.SecuritySettings.placeholder.email') }}
            </a-typography-paragraph>
          </div>
          <div class="operation">
            <a-link>
              {{ $t('userCenter.SecuritySettings.button.update') }}
            </a-link>
          </div>
        </template>
      </a-list-item-meta>
    </a-list-item>
  </a-list>
</template>

<script lang="ts" setup>
  import { useLoginStore } from '@/store';

  const loginStore = useLoginStore();
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
