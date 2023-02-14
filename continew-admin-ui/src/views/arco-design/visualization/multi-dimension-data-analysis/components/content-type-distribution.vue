<template>
  <a-card
    class="general-card"
    :title="$t('multiDAnalysis.card.title.contentTypeDistribution')"
    :header-style="{ paddingBottom: 0 }"
  >
    <Chart style="height: 222px" :option="chartOption" />
  </a-card>
</template>

<script lang="ts" setup>
  import useChartOption from '@/hooks/chart-option';

  const { chartOption } = useChartOption((isDark) => {
    return {
      grid: {
        left: 0,
        right: 0,
        top: 0,
        bottom: 20,
      },
      legend: {
        show: true,
        top: 'center',
        right: '0',
        orient: 'vertical',
        icon: 'circle',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 20,
        textStyle: {
          color: isDark ? '#ffffff' : '#4E5969',
        },
      },
      radar: {
        center: ['40%', '50%'],
        radius: 80,
        indicator: [
          { name: '国际', max: 6500 },
          { name: '财经', max: 22000 },
          { name: '科技', max: 30000 },
          { name: '其他', max: 38000 },
          { name: '体育', max: 52000 },
          { name: '娱乐', max: 25000 },
        ],
        axisName: {
          color: isDark ? '#ffffff' : '#1D2129',
        },
        axisLine: {
          lineStyle: {
            color: isDark ? '#484849' : '#E5E6EB',
          },
        },
        splitLine: {
          lineStyle: {
            color: isDark ? '#484849' : '#E5E6EB',
          },
        },
        splitArea: {
          areaStyle: {
            color: [],
          },
        },
      },
      series: [
        {
          type: 'radar',
          areaStyle: {
            opacity: 0.2,
          },
          data: [
            {
              value: [4850, 19000, 19000, 29500, 35200, 20000],
              name: '纯文本',
              symbol: 'none',
              itemStyle: {
                color: isDark ? '#6CAAF5' : '#249EFF',
              },
            },
            {
              value: [2250, 17000, 21000, 23500, 42950, 22000],
              name: '图文类',
              symbol: 'none',
              itemStyle: {
                color: isDark ? '#A079DC' : '#313CA9',
              },
            },
            {
              value: [5850, 11000, 26000, 27500, 46950, 18000],
              name: '视频类',
              symbol: 'none',
              itemStyle: {
                color: isDark ? '#3D72F6' : '#21CCFF',
              },
            },
          ],
        },
      ],
    };
  });
</script>

<style scoped lang="less"></style>
