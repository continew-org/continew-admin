<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{
        padding: '0 20px',
      }"
    >
      <template #title>
        {{ $t('workplace.geoDistribution') }}
      </template>
      <Chart height="480px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import useChartOption from '@/hooks/chart-option';
  import {
    DashboardGeoDistributionRecord,
    getGeoDistribution,
  } from '@/api/common/dashboard';

  const { loading, setLoading } = useLoading();
  const statisticsData = ref<DashboardGeoDistributionRecord>({
    locations: [],
    locationIpStatistics: [],
  });

  /**
   * 查询访客地域分布信息
   */
  const getData = async () => {
    try {
      setLoading(true);
      const { data } = await getGeoDistribution();
      statisticsData.value = data;
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };
  getData();

  const { chartOption } = useChartOption((isDark) => {
    // echarts support https://echarts.apache.org/zh/theme-builder.html
    // It's not used here
    return {
      legend: {
        left: 'center',
        data: statisticsData.value.locations,
        bottom: -5,
        icon: 'circle',
        itemStyle: {
          borderWidth: 0,
        },
      },
      tooltip: {
        show: true,
        trigger: 'item',
      },
      series: [
        {
          type: 'pie',
          radius: '70%',
          label: {
            formatter: '{d}%',
            fontSize: 14,
            color: isDark ? 'rgba(255, 255, 255, 0.7)' : '#4E5969',
          },
          itemStyle: {
            borderColor: isDark ? '#232324' : '#fff',
            borderWidth: 1,
          },
          data: statisticsData.value.locationIpStatistics,
        },
      ],
    };
  });
</script>

<style scoped lang="less">
  .general-card {
    min-height: 566px;
  }
</style>
