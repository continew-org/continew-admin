<template>
  <a-grid :cols="24" :row-gap="16" class="panel">
    <a-grid-item
      class="panel-col"
      :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12, xxl: 6 }"
    >
      <a-space>
        <a-avatar :size="54" class="col-avatar">
          <svg-icon icon-class="popularity" />
        </a-avatar>
        <a-statistic
          :title="$t('workplace.pvCount')"
          :value="totalData.pvCount"
          :value-from="0"
          animation
          show-group-separator
        >
          <template #suffix>
            <span class="unit">{{ $t('workplace.unit.times') }}</span>
          </template>
        </a-statistic>
      </a-space>
    </a-grid-item>
    <a-grid-item
      class="panel-col"
      :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12, xxl: 6 }"
    >
      <a-space>
        <a-avatar :size="54" class="col-avatar">
          <svg-icon icon-class="same-city" />
        </a-avatar>
        <a-statistic
          :title="$t('workplace.ipCount')"
          :value="totalData.ipCount"
          :value-from="0"
          animation
          show-group-separator
        >
          <template #suffix>
            <span class="unit">{{ $t('workplace.unit.pecs') }}</span>
          </template>
        </a-statistic>
      </a-space>
    </a-grid-item>
    <a-grid-item
      class="panel-col"
      :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12, xxl: 6 }"
    >
      <a-space>
        <a-avatar :size="54" class="col-avatar">
          <svg-icon icon-class="hot" />
        </a-avatar>
        <a-statistic
          :title="$t('workplace.todayPvCount')"
          :value="totalData.todayPvCount"
          :value-from="0"
          animation
          show-group-separator
        >
          <template #suffix>
            <span class="unit">{{ $t('workplace.unit.times') }}</span>
          </template>
        </a-statistic>
      </a-space>
    </a-grid-item>
    <a-grid-item
      class="panel-col"
      :span="{ xs: 12, sm: 12, md: 12, lg: 12, xl: 12, xxl: 6 }"
      style="border-right: none"
    >
      <a-space>
        <a-avatar :size="54" class="col-avatar">
          <svg-icon icon-class="data" />
        </a-avatar>
        <a-statistic
          :title="$t('workplace.newPvFromYesterday')"
          :value="totalData.newPvFromYesterday"
          :precision="1"
          :value-from="0"
          animation
        >
          <template #suffix>
            %
            <icon-caret-up
              v-if="totalData.newPvFromYesterday > 0"
              class="up-icon"
            />
            <icon-caret-down
              v-if="totalData.newPvFromYesterday < 0"
              class="down-icon"
            />
          </template>
        </a-statistic>
      </a-space>
    </a-grid-item>
    <a-grid-item :span="24">
      <a-divider class="panel-border" />
    </a-grid-item>
  </a-grid>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { DashboardTotalRecord, getTotal } from '@/api/common/dashboard';

  const totalData = ref<DashboardTotalRecord>({
    pvCount: 0,
    ipCount: 0,
    todayPvCount: 0,
    newPvFromYesterday: 0.0,
  });

  /**
   * 查询总计信息
   */
  const getData = async () => {
    try {
      const { data } = await getTotal();
      totalData.value = data;
    } catch (err) {
      // you can report use errorHandler or other
    }
  };
  getData();
</script>

<style lang="less" scoped>
  .arco-grid.panel {
    margin-bottom: 0;
    padding: 16px 20px 0 20px;
  }
  .panel-col {
    padding-left: 43px;
    border-right: 1px solid rgb(var(--gray-2));
  }
  .col-avatar {
    margin-right: 12px;
    background-color: var(--color-fill-2);
  }
  .up-icon {
    color: rgb(var(--red-6));
  }
  .down-icon {
    color: rgb(var(--green-6));
  }
  .unit {
    margin-left: 8px;
    color: rgb(var(--gray-8));
    font-size: 12px;
  }
  :deep(.panel-border) {
    margin: 4px 0 0 0;
  }
</style>
