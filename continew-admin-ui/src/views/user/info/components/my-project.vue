<template>
  <a-card class="general-card" :title="$t('userInfo.title.myProject')">
    <template #extra>
      <a-link>{{ $t('userInfo.showMore') }}</a-link>
    </template>
    <a-row :gutter="16">
      <a-col
        v-for="(project, index) in projectList"
        :key="index"
        :xs="12"
        :sm="12"
        :md="12"
        :lg="12"
        :xl="8"
        :xxl="8"
        class="my-project-item"
      >
        <a-card>
          <a-skeleton v-if="loading" :loading="loading" :animation="true">
            <a-skeleton-line :rows="3" />
          </a-skeleton>
          <a-space v-else direction="vertical">
            <a-typography-text bold>{{ project.name }}</a-typography-text>
            <a-typography-text type="secondary">
              {{ project.description }}
            </a-typography-text>
            <a-space>
              <a-avatar-group :size="24">
                {{ project.contributors }}
                <a-avatar
                  v-for="(contributor, idx) in project.contributors"
                  :key="idx"
                  :size="32"
                >
                  <img alt="avatar" :src="contributor.avatar" />
                </a-avatar>
              </a-avatar-group>
              <a-typography-text type="secondary">
                等{{ project.peopleNumber }}人
              </a-typography-text>
            </a-space>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </a-card>
</template>

<script lang="ts" setup>
  import { queryMyProjectList, MyProjectRecord } from '@/api/user-center';
  import useRequest from '@/hooks/request';

  const defaultValue = Array(6).fill({} as MyProjectRecord);
  const { loading, response: projectList } = useRequest<MyProjectRecord[]>(
    queryMyProjectList,
    defaultValue
  );
</script>

<style scoped lang="less">
  :deep(.arco-card-body) {
    min-height: 128px;
    padding-bottom: 0;
  }
  .my-project {
    &-header {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
    }

    &-title {
      margin-top: 0 !important;
      margin-bottom: 18px !important;
    }

    &-list {
      display: flex;
      justify-content: space-between;
    }

    &-item {
      // padding-right: 16px;
      margin-bottom: 16px;

      &:last-child {
        padding-right: 0;
      }
    }
  }
</style>
