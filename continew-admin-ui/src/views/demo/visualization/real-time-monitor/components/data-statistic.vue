<template>
  <a-card :bordered="false" :body-style="{ padding: '20px' }">
    <a-tabs default-active-tab="liveMethod">
      <a-tab-pane
        key="liveMethod"
        :title="$t('realTimeMonitor.tab.title.liveMethod')"
      />
      <a-tab-pane
        key="onlinePopulation"
        :title="$t('realTimeMonitor.tab.title.onlinePopulation')"
      />
    </a-tabs>
    <div class="data-statistic-content">
      <a-radio-group :default-value="3" type="button">
        <a-radio :value="1">{{
          $t('realTimeMonitor.liveMethod.normal')
        }}</a-radio>
        <a-radio :value="2">{{
          $t('realTimeMonitor.liveMethod.flowControl')
        }}</a-radio>
        <a-radio :value="3">{{
          $t('realTimeMonitor.liveMethod.video')
        }}</a-radio>
        <a-radio :value="4">{{ $t('realTimeMonitor.liveMethod.web') }}</a-radio>
      </a-radio-group>

      <div class="data-statistic-list-wrapper">
        <div class="data-statistic-list-header">
          <a-button type="text">{{
            $t('realTimeMonitor.editCarousel')
          }}</a-button>
          <a-button disabled>{{
            $t('realTimeMonitor.startCarousel')
          }}</a-button>
        </div>
        <div class="data-statistic-list-content">
          <DataStatisticList />
        </div>
      </div>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
  import DataStatisticList from './data-statistic-list.vue';
</script>

<style scoped lang="less">
  .data-statistic {
    &-content {
      padding: 20px 0;
    }

    &-list {
      &-header {
        display: flex;
        justify-content: space-between;
        margin-top: 16px;
      }

      &-content {
        margin-top: 16px;
      }
    }
  }
</style>
