<template>
  <a-spin :loading="loading" style="width: 100%">
    <a-card
      class="general-card"
      :header-style="{ paddingBottom: '0' }"
      :body-style="{ padding: '17px 20px 20px 20px' }"
    >
      <template #title>
        {{ $t('workplace.popularModule') }}
      </template>
      <a-space direction="vertical" :size="10" fill>
        <a-table
          :data="dataList"
          :pagination="false"
          :bordered="false"
          :scroll="{ x: '100%', y: '484px' }"
        >
          <template #columns>
            <a-table-column title="排名">
              <template #cell="{ rowIndex }">
                {{ rowIndex + 1 }}
              </template>
            </a-table-column>
            <a-table-column title="模块">
              <template #cell="{ record }">
                <a-typography-paragraph
                  :ellipsis="{
                    rows: 1,
                  }"
                >
                  {{ record.module }}
                </a-typography-paragraph>
              </template>
            </a-table-column>
            <a-table-column title="总浏览量" data-index="pvCount" />
            <a-table-column
              title="日涨幅"
              :sortable="{
                sortDirections: ['ascend', 'descend'],
              }"
            >
              <template #cell="{ record }">
                <div class="increases-cell">
                  <span>{{ record.newPvFromYesterday }}%</span>
                  <icon-caret-up
                    v-if="record.newPvFromYesterday > 0"
                    style="color: rgb(var(--red-6)); font-size: 8px"
                  />
                  <icon-caret-down
                    v-if="record.newPvFromYesterday < 0"
                    style="color: rgb(var(--green-6)); font-size: 8px"
                  />
                </div>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </a-space>
    </a-card>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { listPopularModule } from '@/api/common/dashboard';
  import type { TableData } from '@arco-design/web-vue/es/table/interface';

  const { loading, setLoading } = useLoading();
  const dataList = ref<TableData[]>();

  /**
   * 查询热门模块列表
   */
  const getList = async () => {
    try {
      setLoading(true);
      const { data } = await listPopularModule();
      dataList.value = data;
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };
  getList();
</script>

<style scoped lang="less">
  .general-card {
    min-height: 400px;
  }
  :deep(.arco-table-tr) {
    height: 44px;
    .arco-typography {
      margin-bottom: 0;
    }
  }
  .increases-cell {
    display: flex;
    align-items: center;
    span {
      margin-right: 4px;
    }
  }
</style>
