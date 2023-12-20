<template>
  <GiOption :class="{ option: showClassStyle }">
    <GiOptionItem @click="onClickItem('rename')">
      <template #icon><GiSvgIcon name="menu-edit"></GiSvgIcon> </template>
      <span>重命名</span>
    </GiOptionItem>
    <GiOptionItem @click="onClickItem('move')">
      <template #icon><GiSvgIcon name="menu-move"></GiSvgIcon> </template>
      <span>移动到</span>
    </GiOptionItem>
    <GiOptionItem @click="onClickItem('download')">
      <template #icon><GiSvgIcon name="menu-download"></GiSvgIcon> </template>
      <span>下载</span>
    </GiOptionItem>
    <a-popover
      v-if="props.fileInfo.extendName === 'zip'"
      position="right"
      :content-style="{ padding: 0, overflow: 'hidden', width: '150px' }"
      :arrow-style="{ display: 'none' }"
    >
      <GiOptionItem more>
        <template #icon><GiSvgIcon name="menu-zip"></GiSvgIcon> </template>
        <span>解压</span>
      </GiOptionItem>
      <template #content>
        <GiOption>
          <GiOptionItem @click="onClickItem('zip1')">
            <template #icon><GiSvgIcon name="file-rar"></GiSvgIcon> </template>
            <span>解压到当前目录</span>
          </GiOptionItem>
          <GiOptionItem @click="onClickItem('zip2')">
            <template #icon><GiSvgIcon name="file-rar"></GiSvgIcon> </template>
            <span>解压到其他目录</span>
          </GiOptionItem>
        </GiOption>
      </template>
    </a-popover>
    <GiOptionItem @click="onClickItem('detail')">
      <template #icon><GiSvgIcon name="menu-detail"></GiSvgIcon> </template>
      <span>详情</span>
    </GiOptionItem>
    <GiOptionItem @click="onClickItem('delete')">
      <template #icon><GiSvgIcon name="menu-delete"></GiSvgIcon> </template>
      <span>删除</span>
    </GiOptionItem>
  </GiOption>
</template>

<script setup lang="ts">
  import type { FileItem } from '@/api/system/file';
  // import GiOption from '@/components/GiOption/index.vue';
  // import GiOptionItem from '@/components/GiOptionItem/index.vue';

  interface Props {
    fileInfo?: FileItem;
    showClassStyle?: boolean;
  }

  const props = withDefaults(defineProps<Props>(), {
    fileInfo: () => ({
      id: '',
      type: '',
      name: '',
      extendName: '',
      src: '',
      updateTime: '',
      isDir: false,
      filePath: '',
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
