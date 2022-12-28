<template>
  <a-card class="general-card" :title="$t('userInfo.title.latestActivity')">
    <template #extra>
      <a-link>{{ $t('userInfo.viewAll') }}</a-link>
    </template>
    <a-list :bordered="false">
      <a-list-item
        v-for="activity in activityList"
        :key="activity.id"
        action-layout="horizontal"
      >
        <a-skeleton
          v-if="loading"
          :loading="loading"
          :animation="true"
          class="skeleton-item"
        >
          <a-row :gutter="6">
            <a-col :span="2">
              <a-skeleton-shape shape="circle" />
            </a-col>
            <a-col :span="22">
              <a-skeleton-line :widths="['40%', '100%']" :rows="2" />
            </a-col>
          </a-row>
        </a-skeleton>
        <a-list-item-meta
          v-else
          :title="activity.title"
          :description="activity.description"
        >
          <template #avatar>
            <a-avatar>
              <img :src="activity.avatar" />
            </a-avatar>
          </template>
        </a-list-item-meta>
      </a-list-item>
    </a-list>
  </a-card>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { queryLatestActivity, LatestActivity } from '@/api/user-center';
  import useLoading from '@/hooks/loading';

  const { loading, setLoading } = useLoading(true);
  const activityList = ref<LatestActivity[]>(new Array(7).fill({}));
  const fetchData = async () => {
    try {
      const { data } = await queryLatestActivity();
      activityList.value = data;
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };
  fetchData();
</script>

<style scoped lang="less">
  .latest-activity {
    &-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }
  .general-card :deep(.arco-list-item) {
    padding-left: 0;
    border-bottom: none;
    .arco-list-item-meta-content {
      flex: 1;
      padding-bottom: 27px;
      border-bottom: 1px solid var(--color-neutral-3);
    }
    .arco-list-item-meta-avatar {
      padding-bottom: 27px;
    }
    .skeleton-item {
      margin-top: 10px;
      padding-bottom: 20px;
      border-bottom: 1px solid var(--color-neutral-3);
    }
  }
</style>
