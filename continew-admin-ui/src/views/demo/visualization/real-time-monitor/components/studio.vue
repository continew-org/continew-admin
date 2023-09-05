<template>
  <a-card class="general-card" :title="$t('realTimeMonitor.title.studioPreview')">
    <template #extra>
      <icon-more />
    </template>
    <div class="studio-wrapper">
      <img
        src="http://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/c788fc704d32cf3b1136c7d45afc2669.png~tplv-uwbnlip3yd-webp.webp"
        class="studio-preview"
      />
      <div class="studio-bar">
        <div v-if="userInfo">
          <a-space :size="12">
            <a-avatar :size="24">
              <img :src="getAvatar(userInfo.avatar, userInfo.gender)" />
            </a-avatar>
            <a-typography-text>
              {{ userInfo.nickname }} {{ $t('realTimeMonitor.studioPreview.studio') }}
            </a-typography-text>
          </a-space>
        </div>
        <a-typography-text type="secondary">
          36,000 {{ $t('realTimeMonitor.studioPreview.watching') }}
        </a-typography-text>
      </div>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  import { useLoginStore } from '@/store';
  import getAvatar from '@/utils/avatar';

  const userInfo = useLoginStore();
</script>

<style scoped lang="less">
  .studio {
    &-preview {
      display: block;
      max-width: 600px;
      margin: 0 auto;
      width: 100%;
    }

    &-bar {
      display: flex;
      justify-content: space-between;
      margin-top: 16px;
    }
  }
</style>
