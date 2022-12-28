<template>
  <div>
    <a-table
      :columns="columns"
      :data="data"
      row-key="id"
      :row-selection="{
        type: 'checkbox',
        showCheckedAll: true,
      }"
      :border="false"
      :pagination="false"
    />
    <a-typography-text type="secondary" class="data-statistic-list-tip">
      {{ $t('monitor.list.tip.rotations') }} {{ data.length }}
      {{ $t('monitor.list.tip.rest') }}
    </a-typography-text>
  </div>
</template>

<script lang="ts" setup>
  import { computed, h, compile } from 'vue';
  import { useI18n } from 'vue-i18n';
  import type {
    TableColumnData,
    TableData,
  } from '@arco-design/web-vue/es/table/interface.d';

  interface PreviewRecord {
    cover: string;
    name: string;
    duration: string;
    id: string;
    status: number;
  }
  const { t } = useI18n();
  const data: PreviewRecord[] = [
    {
      cover:
        'http://p1-arco.byteimg.com/tos-cn-i-uwbnlip3yd/c788fc704d32cf3b1136c7d45afc2669.png~tplv-uwbnlip3yd-webp.webp',
      name: '视频直播',
      duration: '00:05:19',
      id: '54e23ade',
      status: -1,
    },
  ];
  const renderTag = (status: number) => {
    if (status === -1) {
      return `<a-tag  color="red" class='data-statistic-list-cover-tag'>
            ${t('monitor.list.tag.auditFailed')}
        </a-tag>`;
    }
    return '';
  };
  // Using the Render function is more flexible than using templates.
  // But, cannot bind context and local scopes are also lost

  const columns = computed(() => {
    return [
      {
        title: t('monitor.list.title.order'),
        render({
          rowIndex,
        }: {
          record: TableData;
          column: TableColumnData;
          rowIndex: number;
        }) {
          const tmp = `<span>${rowIndex + 1}</span>`;
          return h(compile(tmp));
        },
      },
      {
        title: t('monitor.list.title.cover'),
        render({
          record,
        }: {
          record: TableData;
          column: TableColumnData;
          rowIndex: number;
        }) {
          const tmp = `<div class='data-statistic-list-cover-wrapper'>
              <img src=${record.cover} />
              ${renderTag(record.status)}
            </div>`;
          return h(compile(tmp));
        },
      },
      {
        title: t('monitor.list.title.name'),
        dataIndex: 'name',
      },
      {
        dataIndex: 'duration',
        title: t('monitor.list.title.duration'),
      },
      {
        dataIndex: 'id',
        title: t('monitor.list.title.id'),
      },
    ];
  });
</script>

<style lang="less">
  // Warning: Here is the global style
  .data-statistic {
    &-list {
      &-cover {
        &-wrapper {
          position: relative;
          height: 68px;

          img {
            height: 100%;
          }
        }

        &-tag {
          position: absolute;
          top: 6px;
          left: 6px;
        }
      }

      &-tip {
        display: block;
        margin-top: 16px;
        text-align: center;
      }
    }
  }
</style>
