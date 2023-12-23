<template>
  <GiOption :class="{ option: showClassStyle }">
    <GiOptionItem @click="onClickItem('rename')">
      <template #icon><svg-icon icon-class="menu-edit" /></template>
      <span>重命名</span>
    </GiOptionItem>
    <GiOptionItem @click="onClickItem('download')">
      <template #icon><svg-icon icon-class="menu-download" /></template>
      <span>下载</span>
    </GiOptionItem>
    <a-popover
      v-if="props.fileInfo.extension === 'zip'"
      position="right"
      :content-style="{ padding: 0, overflow: 'hidden', width: '150px' }"
      :arrow-style="{ display: 'none' }"
    >
      <GiOptionItem more>
        <template #icon><svg-icon icon-class="menu-zip" /></template>
        <span>解压</span>
      </GiOptionItem>
      <template #content>
        <GiOption>
          <GiOptionItem @click="onClickItem('zip1')">
            <template #icon><svg-icon icon-class="file-rar" /></template>
            <span>解压到当前目录</span>
          </GiOptionItem>
          <GiOptionItem @click="onClickItem('zip2')">
            <template #icon><svg-icon icon-class="file-rar" /></template>
            <span>解压到其他目录</span>
          </GiOptionItem>
        </GiOption>
      </template>
    </a-popover>
    <GiOptionItem @click="onClickItem('detail')">
      <template #icon><svg-icon icon-class="menu-detail" /></template>
      <span>详情</span>
    </GiOptionItem>
    <GiOptionItem @click="onClickItem('delete')">
      <template #icon><svg-icon icon-class="menu-delete" /></template>
      <span>删除</span>
    </GiOptionItem>
  </GiOption>
</template>

<script setup lang="ts">
  import type { FileItem } from '@/api/system/file';
  import GiOption from '@/components/gi-option/index.vue';
  import GiOptionItem from '@/components/gi-option-item/index.vue';

  interface Props {
    fileInfo?: FileItem;
    showClassStyle?: boolean;
  }

  const props = withDefaults(defineProps<Props>(), {
    fileInfo: () => ({
      id: '',
      name: '',
      size: 0,
      url: '',
      extension: '',
      type: '',
      updateTime: '',
    }), // 文件数据
    showClassStyle: true,
  });

  const emit = defineEmits(['click']);

  const onClickItem = (mode: string) => {
    emit('click', mode);
  };
</script>

<style lang="less" scoped>
  .option {
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.12),
      0 0 6px rgba(0, 0, 0, 0.04);
    border-radius: 4px;
    overflow: hidden;
    border: 1px solid var(--color-border-2);
    box-sizing: border-box;
    background: var(--color-bg-popup);
  }
</style>
