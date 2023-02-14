<template>
  <div class="list-wrap">
    <a-typography-title class="block-title" :heading="6">
      {{ $t('cardList.tab.title.content') }}
    </a-typography-title>
    <a-row class="list-row" :gutter="24">
      <a-col
        :xs="12"
        :sm="12"
        :md="12"
        :lg="6"
        :xl="6"
        :xxl="6"
        class="list-col"
      >
        <div class="card-wrap empty-wrap">
          <a-card :bordered="false" hoverable>
            <a-result :status="null" :title="$t('cardList.content.action')">
              <template #icon>
                <icon-plus style="font-size: 20px" />
              </template>
            </a-result>
          </a-card>
        </div>
      </a-col>
      <a-col
        v-for="item in renderData"
        :key="item.id"
        class="list-col"
        :xs="12"
        :sm="12"
        :md="12"
        :lg="6"
        :xl="6"
        :xxl="6"
      >
        <CardWrap
          :loading="loading"
          :title="item.title"
          :description="item.description"
          :default-value="item.enable"
          :action-type="item.actionType"
          :icon="item.icon"
          :open-txt="$t('cardList.content.inspection')"
          :close-txt="$t('cardList.content.delete')"
          :show-tag="false"
        >
          <a-descriptions
            style="margin-top: 16px"
            :data="item.data"
            layout="inline-horizontal"
            :column="2"
          />
          <template #skeleton>
            <a-skeleton :animation="true">
              <a-skeleton-line
                :widths="['50%', '50%', '100%', '40%']"
                :rows="4"
              />
              <a-skeleton-line :widths="['40%']" :rows="1" />
            </a-skeleton>
          </template>
        </CardWrap>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { queryInspectionList, ServiceRecord } from '@/api/list';
  import useRequest from '@/hooks/request';
  import CardWrap from './card-wrap.vue';

  const defaultValue: ServiceRecord[] = new Array(3).fill({});
  const { loading, response: renderData } = useRequest<ServiceRecord[]>(
    queryInspectionList,
    defaultValue
  );
</script>

<style scoped lang="less">
  .card-wrap {
    height: 100%;
    transition: all 0.3s;
    border: 1px solid var(--color-neutral-3);
    &:hover {
      transform: translateY(-4px);
    }
    :deep(.arco-card-meta-description) {
      color: rgb(var(--gray-6));
      .arco-descriptions-item-label-inline {
        font-weight: normal;
        font-size: 12px;
        color: rgb(var(--gray-6));
      }
      .arco-descriptions-item-value-inline {
        color: rgb(var(--gray-8));
      }
    }
  }
  .empty-wrap {
    height: 200px;
    border-radius: 4px;
    :deep(.arco-card) {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      .arco-result-title {
        color: rgb(var(--gray-6));
      }
    }
  }
</style>
