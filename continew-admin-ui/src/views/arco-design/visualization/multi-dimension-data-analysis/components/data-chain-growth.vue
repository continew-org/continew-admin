<template>
  <div>
    <a-row :gutter="16">
      <a-col :span="6">
        <ChainItem
          :title="$t('multiDAnalysis.card.title.retentionTrends')"
          quota="retentionTrends"
          chart-type="line"
        />
      </a-col>
      <a-col :span="6">
        <ChainItem
          :title="$t('multiDAnalysis.card.title.userRetention')"
          quota="userRetention"
          chart-type="bar"
        />
      </a-col>
      <a-col :span="6">
        <ChainItem
          :title="$t('multiDAnalysis.card.title.contentConsumptionTrends')"
          quota="contentConsumptionTrends"
          chart-type="line"
        />
      </a-col>
      <a-col :span="6">
        <ChainItem
          :title="$t('multiDAnalysis.card.title.contentConsumption')"
          quota="contentConsumption"
          chart-type="bar"
        />
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import ChainItem from './chain-item.vue';
</script>
