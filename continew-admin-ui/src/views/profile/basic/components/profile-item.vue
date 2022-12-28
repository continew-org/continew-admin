<template>
  <div class="item-container">
    <a-space :size="16" direction="vertical" fill>
      <a-descriptions
        v-for="(item, idx) in blockDataList"
        :key="idx"
        :label-style="{
          textAlign: 'right',
          width: '200px',
          paddingRight: '10px',
          color: 'rgb(var(--gray-8))',
        }"
        :value-style="{ width: '400px' }"
        :title="item.title"
        :data="item.data"
      >
        <template #value="{ value }">
          <a-skeleton v-if="loading" :animation="true">
            <a-skeleton-line :widths="['200px']" :rows="1" />
          </a-skeleton>
          <span v-else>{{ value }}</span>
        </template>
      </a-descriptions>
    </a-space>
  </div>
</template>

<script lang="ts" setup>
  import { computed, PropType } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { ProfileBasicRes } from '@/api/profile';

  type BlockList = {
    title: string;
    data: {
      label: string;
      value: string;
    }[];
  }[];

  const props = defineProps({
    type: {
      type: String,
      default: '',
    },
    renderData: {
      type: Object as PropType<ProfileBasicRes>,
      required: true,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  });
  const { t } = useI18n();
  const blockDataList = computed<BlockList>(() => {
    const { renderData } = props;
    const result = [];
    result.push({
      title:
        props.type === 'pre'
          ? t('basicProfile.title.preVideo')
          : t('basicProfile.title.video'),
      data: [
        {
          label: t('basicProfile.label.video.mode'),
          value: renderData?.video?.mode || '-',
        },
        {
          label: t('basicProfile.label.video.acquisition.resolution'),
          value: renderData?.video?.acquisition.resolution || '-',
        },
        {
          label: t('basicProfile.label.video.acquisition.frameRate'),
          value: `${renderData?.video?.acquisition.frameRate || '-'} fps`,
        },
        {
          label: t('basicProfile.label.video.encoding.resolution'),
          value: renderData?.video?.encoding.resolution || '-',
        },
        {
          label: t('basicProfile.label.video.encoding.rate.min'),
          value: `${renderData?.video?.encoding.rate.min || '-'} bps`,
        },
        {
          label: t('basicProfile.label.video.encoding.rate.max'),
          value: `${renderData?.video?.encoding.rate.max || '-'} bps`,
        },
        {
          label: t('basicProfile.label.video.encoding.rate.default'),
          value: `${renderData?.video?.encoding.rate.default || '-'} bps`,
        },
        {
          label: t('basicProfile.label.video.encoding.frameRate'),
          value: `${renderData?.video?.encoding.frameRate || '-'} fpx`,
        },
        {
          label: t('basicProfile.label.video.encoding.profile'),
          value: renderData?.video?.encoding.profile || '-',
        },
      ],
    });

    result.push({
      title:
        props.type === 'pre'
          ? t('basicProfile.title.preAudio')
          : t('basicProfile.title.audio'),
      data: [
        {
          label: t('basicProfile.label.audio.mode'),
          value: renderData?.audio?.mode || '-',
        },
        {
          label: t('basicProfile.label.audio.acquisition.channels'),
          value: `${renderData?.audio?.acquisition.channels || '-'} ${t(
            'basicProfile.unit.audio.channels'
          )}`,
        },
        {
          label: t('basicProfile.label.audio.encoding.channels'),
          value: `${renderData?.audio?.encoding.channels || '-'} ${t(
            'basicProfile.unit.audio.channels'
          )}`,
        },
        {
          label: t('basicProfile.label.audio.encoding.rate'),
          value: `${renderData?.audio?.encoding.rate || '-'} kbps`,
        },
        {
          label: t('basicProfile.label.audio.encoding.profile'),
          value: renderData?.audio?.encoding.profile || '-',
        },
      ],
    });

    return result;
  });
</script>

<style scoped lang="less">
  .item-container {
    padding-top: 20px;
    :deep(.arco-descriptions-item-label) {
      font-weight: normal;
    }
  }
</style>
