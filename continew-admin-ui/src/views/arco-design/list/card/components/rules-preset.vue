<template>
  <div class="list-wrap">
    <a-typography-title class="block-title" :heading="6">
      {{ $t('cardList.tab.title.preset') }}
    </a-typography-title>
    <a-row class="list-row" :gutter="24">
      <a-col
        v-for="item in renderData"
        :key="item.id"
        :xs="12"
        :sm="12"
        :md="12"
        :lg="6"
        :xl="6"
        :xxl="6"
        class="list-col"
        style="min-height: 140px"
      >
        <CardWrap
          :loading="loading"
          :title="item.title"
          :description="item.description"
          :default-value="item.enable"
          :action-type="item.actionType"
          :tag-text="$t('cardList.preset.tag')"
        >
          <template #skeleton>
            <a-skeleton :animation="true">
              <a-skeleton-line :widths="['100%', '40%']" :rows="2" />
              <a-skeleton-line :widths="['40%']" :rows="1" />
            </a-skeleton>
          </template>
        </CardWrap>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import { queryRulesPresetList, ServiceRecord } from '@/api/list';
  import useRequest from '@/hooks/request';
  import CardWrap from './card-wrap.vue';

  const defaultValue: ServiceRecord[] = new Array(6).fill({});
  const { loading, response: renderData } = useRequest<ServiceRecord[]>(
    queryRulesPresetList,
    defaultValue
  );
</script>

<style scoped lang="less"></style>
