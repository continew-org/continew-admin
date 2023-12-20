<template>
  <div class="file-list">
    <a-table
      row-key="id"
      :scroll="{ x: '100%', y: '100%', minWidth: 600 }"
      :data="props.data"
      :bordered="false"
      :pagination="false"
      :row-selection="isBatchMode ? rowSelection : undefined"
    >
      <template #columns>
        <a-table-column title="名称">
          <template #cell="{ record }">
            <a-trigger
              trigger="contextMenu"
              align-point
              animation-name="slide-dynamic-origin"
              auto-fit-transform-origin
              position="bl"
              update-at-scroll
              scroll-to-close
            >
              <div class="file-name" @click="handleRowClick(record)">
                <div class="file-image">
                  <FileImg :data="record"></FileImg>
                </div>
                <span>{{ record.name }}</span>
              </div>
              <template #content>
                <FileRightMenu
                  :file-info="record"
                  @click="handleRightMenuItemClick($event, record)"
                ></FileRightMenu>
              </template>
            </a-trigger>
          </template>
        </a-table-column>
        <a-table-column
          title="扩展名"
          data-index="extendName"
          :width="100"
        ></a-table-column>
        <a-table-column
          title="更改时间"
          data-index="updateTime"
          :width="200"
        ></a-table-column>
        <a-table-column title="操作" :width="120" align="center">
          <template #cell="{ record }">
            <a-popover
              trigger="click"
              position="bottom"
              content-class="more-option"
              :content-style="{ 'padding': 0, 'margin-top': 0 }"
            >
              <a-button type="text"><icon-more :size="16" /></a-button>
              <template #content>
                <FileRightMenu
                  :file-info="record"
                  :show-class-style="false"
                  @click="handleRightMenuItemClick($event, record)"
                ></FileRightMenu>
              </template>
            </a-popover>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
  import type { TableRowSelection } from '@arco-design/web-vue';
  import type { FileItem } from '@/api/system/file';
  import { reactive } from 'vue';
  import FileImg from './FileImg.vue';
  import FileRightMenu from './FileRightMenu.vue';

  interface Props {
    data?: FileItem[];
    isBatchMode?: boolean;
  }

  const props = withDefaults(defineProps<Props>(), {
    data: () => [], // 文件数据
    isBatchMode: false, // 是否是批量模式
  });

  const rowSelection: TableRowSelection = reactive({
    type: 'checkbox',
    showCheckedAll: true,
  });

  const emit = defineEmits(['click', 'rightMenuClick']);

  // 行点击事件
  const handleRowClick = (row: FileItem) => {
    emit('click', row);
  };

  // 右键菜单点击事件
  const handleRightMenuItemClick = (mode: string, item: FileItem) => {
    emit('rightMenuClick', mode, item);
  };
</script>

<style lang="scss">
  .more-option {
    .arco-popover-content {
      margin-top: 0 !important;
    }
  }
</style>

<style lang="less" scoped>
  :deep(.arco-table-td .arco-table-cell) {
    padding-top: 0;
    padding-bottom: 0;
  }
  .file-list {
    width: 100%;
    padding-top: 12px;
    overflow: hidden;
    .file-name {
      height: 100%;
      display: flex;
      align-items: center;
      padding-top: 6px;
      padding-bottom: 6px;
      cursor: pointer;
      .file-image {
        width: 30px;
        height: 30px;
        margin-right: 10px;
      }
    }
  }
</style>
