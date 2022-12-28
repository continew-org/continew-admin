<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :title="$t('multiDAnalysis.card.title.dataOverview')"
    >
      <a-row justify="space-between">
        <a-col v-for="(item, idx) in renderData" :key="idx" :span="6">
          <a-statistic
            :title="item.title"
            :value="item.value"
            show-group-separator
            :value-from="0"
            animation
          >
            <template #prefix>
              <span
                class="statistic-prefix"
                :style="{ background: item.prefix.background }"
              >
                <component
                  :is="item.prefix.icon"
                  :style="{ color: item.prefix.iconColor }"
                />
              </span>
            </template>
          </a-statistic>
        </a-col>
      </a-row>
      <Chart style="height: 328px; margin-top: 20px" :option="chartOption" />
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { LineSeriesOption } from 'echarts';
  import { queryDataOverview } from '@/api/visualization';
  import useLoading from '@/hooks/loading';
  import { ToolTipFormatterParams } from '@/types/echarts';
  import useThemes from '@/hooks/themes';
  import useChartOption from '@/hooks/chart-option';

  const tooltipItemsHtmlString = (items: ToolTipFormatterParams[]) => {
    return items
      .map(
        (el) => `<div class="content-panel">
        <p>
          <span style="background-color: ${
            el.color
          }" class="tooltip-item-icon"></span><span>${el.seriesName}</span>
        </p>
        <span class="tooltip-value">${el.value.toLocaleString()}</span>
      </div>`
      )
      .reverse()
      .join('');
  };

  const generateSeries = (
    name: string,
    lineColor: string,
    itemBorderColor: string,
    data: number[]
  ): LineSeriesOption => {
    return {
      name,
      data,
      stack: 'Total',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 10,
      itemStyle: {
        color: lineColor,
      },
      emphasis: {
        focus: 'series',
        itemStyle: {
          color: lineColor,
          borderWidth: 2,
          borderColor: itemBorderColor,
        },
      },
      lineStyle: {
        width: 2,
        color: lineColor,
      },
      showSymbol: false,
      areaStyle: {
        opacity: 0.1,
        color: lineColor,
      },
    };
  };
  const { t } = useI18n();
  const { loading, setLoading } = useLoading(true);
  const { isDark } = useThemes();
  const renderData = computed(() => [
    {
      title: t('multiDAnalysis.dataOverview.contentProduction'),
      value: 1902,
      prefix: {
        icon: 'icon-edit',
        background: isDark.value ? '#593E2F' : '#FFE4BA',
        iconColor: isDark.value ? '#F29A43' : '#F77234',
      },
    },
    {
      title: t('multiDAnalysis.dataOverview.contentClick'),
      value: 2445,
      prefix: {
        icon: 'icon-thumb-up',
        background: isDark.value ? '#3D5A62' : '#E8FFFB',
        iconColor: isDark.value ? '#6ED1CE' : '#33D1C9',
      },
    },
    {
      title: t('multiDAnalysis.dataOverview.contentExposure'),
      value: 3034,
      prefix: {
        icon: 'icon-heart',
        background: isDark.value ? '#354276' : '#E8F3FF',
        iconColor: isDark.value ? '#4A7FF7' : '#165DFF',
      },
    },
    {
      title: t('multiDAnalysis.dataOverview.activeUsers'),
      value: 1275,
      prefix: {
        icon: 'icon-user',
        background: isDark.value ? '#3F385E' : '#F5E8FF',
        iconColor: isDark.value ? '#8558D3' : '#722ED1',
      },
    },
  ]);
  const xAxis = ref<string[]>([]);
  const contentProductionData = ref<number[]>([]);
  const contentClickData = ref<number[]>([]);
  const contentExposureData = ref<number[]>([]);
  const activeUsersData = ref<number[]>([]);
  const { chartOption } = useChartOption((dark) => {
    return {
      grid: {
        left: '2.6%',
        right: '4',
        top: '40',
        bottom: '40',
      },
      xAxis: {
        type: 'category',
        offset: 2,
        data: xAxis.value,
        boundaryGap: false,
        axisLabel: {
          color: '#4E5969',
          formatter(value: number, idx: number) {
            if (idx === 0) return '';
            if (idx === xAxis.value.length - 1) return '';
            return `${value}`;
          },
        },
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        splitLine: {
          show: false,
        },
        axisPointer: {
          show: true,
          lineStyle: {
            color: '#23ADFF',
            width: 2,
          },
        },
      },
      yAxis: {
        type: 'value',
        axisLine: {
          show: false,
        },
        axisLabel: {
          formatter(value: number, idx: number) {
            if (idx === 0) return String(value);
            return `${value / 1000}k`;
          },
        },
        splitLine: {
          lineStyle: {
            color: dark ? '#2E2E30' : '#F2F3F5',
          },
        },
      },
      tooltip: {
        trigger: 'axis',
        formatter(params) {
          const [firstElement] = params as ToolTipFormatterParams[];
          return `<div>
            <p class="tooltip-title">${firstElement.axisValueLabel}</p>
            ${tooltipItemsHtmlString(params as ToolTipFormatterParams[])}
          </div>`;
        },
        className: 'echarts-tooltip-diy',
      },
      graphic: {
        elements: [
          {
            type: 'text',
            left: '2.6%',
            bottom: '18',
            style: {
              text: '12.10',
              textAlign: 'center',
              fill: '#4E5969',
              fontSize: 12,
            },
          },
          {
            type: 'text',
            right: '0',
            bottom: '18',
            style: {
              text: '12.17',
              textAlign: 'center',
              fill: '#4E5969',
              fontSize: 12,
            },
          },
        ],
      },
      series: [
        generateSeries(
          '内容生产量',
          '#722ED1',
          '#F5E8FF',
          contentProductionData.value
        ),
        generateSeries(
          '内容点击量',
          '#F77234',
          '#FFE4BA',
          contentClickData.value
        ),
        generateSeries(
          '内容曝光量',
          '#33D1C9',
          '#E8FFFB',
          contentExposureData.value
        ),
        generateSeries(
          '活跃用户数',
          '#3469FF',
          '#E8F3FF',
          activeUsersData.value
        ),
      ],
    };
  });
  const fetchData = async () => {
    setLoading(true);
    try {
      const { data } = await queryDataOverview();
      xAxis.value = data.xAxis;
      data.data.forEach((el) => {
        if (el.name === '内容生产量') {
          contentProductionData.value = el.value;
        } else if (el.name === '内容点击量') {
          contentClickData.value = el.value;
        } else if (el.name === '内容曝光量') {
          contentExposureData.value = el.value;
        }
        activeUsersData.value = el.value;
      });
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };
  fetchData();
</script>

<style scoped lang="less">
  :deep(.arco-statistic) {
    .arco-statistic-title {
      color: rgb(var(--gray-10));
      font-weight: bold;
    }
    .arco-statistic-value {
      display: flex;
      align-items: center;
    }
  }
  .statistic-prefix {
    display: inline-block;
    width: 32px;
    height: 32px;
    margin-right: 8px;
    color: var(--color-white);
    font-size: 16px;
    line-height: 32px;
    text-align: center;
    vertical-align: middle;
    border-radius: 6px;
  }
</style>
