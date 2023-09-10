<template>
  <a-card
    class="general-card"
    :title="$t('realTimeMonitor.studioStatus.title.studioStatus')"
  >
    <template #extra>
      <a-tag color="green">{{
        $t('realTimeMonitor.studioStatus.smooth')
      }}</a-tag>
    </template>
    <a-descriptions layout="horizontal" :data="dataStatus" :column="2">
      <template #label="{ label }">
        <span
          v-if="['mainstream', 'hotStandby', 'coldStandby'].includes(label)"
        >
          <a-typography-text style="padding-right: 8px">
            {{ $t(`realTimeMonitor.studioStatus.${label}`) }}
          </a-typography-text>
          {{ $t('realTimeMonitor.studioStatus.bitRate') }}
        </span>
        <span v-else>{{ label }}</span>
      </template>
    </a-descriptions>
    <a-typography-title style="margin-bottom: 16px" :heading="6">
      {{ $t('realTimeMonitor.studioStatus.title.pictureInfo') }}
    </a-typography-title>
    <a-descriptions layout="horizontal" :data="dataPicture" :column="2" />
  </a-card>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const dataStatus = computed(() => [
    {
      label: 'mainstream',
      value: '6 Mbps',
    },
    {
      label: t('realTimeMonitor.studioStatus.frameRate'),
      value: '60',
    },
    {
      label: 'hotStandby',
      value: '6 Mbps',
    },
    {
      label: t('realTimeMonitor.studioStatus.frameRate'),
      value: '60',
    },
    {
      label: 'coldStandby',
      value: '6 Mbps',
    },
    {
      label: t('realTimeMonitor.studioStatus.frameRate'),
      value: '60',
    },
  ]);
  const dataPicture = computed(() => [
    {
      label: t('realTimeMonitor.studioStatus.line'),
      value: '热备',
    },
    {
      label: 'CDN',
      value: 'KS',
    },
    {
      label: t('realTimeMonitor.studioStatus.play'),
      value: 'FLV',
    },
    {
      label: t('realTimeMonitor.studioStatus.pictureQuality'),
      value: '原画',
    },
  ]);
</script>

<style scoped lang="less">
  :deep(.arco-descriptions-item-label) {
    padding-right: 6px;
  }
</style>
