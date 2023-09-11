<template>
  <a-card
    class="general-card"
    :title="$t('workplace.recently.visited')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ paddingTop: '26px' }"
  >
    <div style="margin-bottom: -1rem">
      <a-row :gutter="8">
        <a-col
          v-for="link in links"
          :key="link.title"
          :span="8"
          class="wrapper"
        >
          <div @click="router.replace({ path: link.path })">
            <div class="icon">
              <svg-icon :icon-class="link.icon" />
            </div>
            <a-typography-paragraph class="text">
              {{ link.title }}
            </a-typography-paragraph>
          </div>
        </a-col>
      </a-row>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { DashboardRecentlyVisitedRecord } from '@/api/common/dashboard';

  const router = useRouter();
  const links = ref<DashboardRecentlyVisitedRecord[]>();

  /**
   * 加载最近访问菜单列表
   */
  onMounted(() => {
    const recentlyVisitedList = window.localStorage.getItem('recently-visited');
    if (recentlyVisitedList === null) {
      links.value = [];
    } else {
      links.value = JSON.parse(recentlyVisitedList);
    }
  });
</script>

<style lang="less" scoped>
  :deep(.arco-card-header-title) {
    line-height: inherit;
  }
</style>
