<template>
  <GiOption :class="{ option: showClassStyle }">
    <GiOptionItem v-permission="['system:file:update']" @click="onClickItem('rename')">
      <template #icon><svg-icon icon-class="menu-edit" /></template>
      <span>重命名</span>
    </GiOptionItem>
    <GiOptionItem v-permission="['system:file:download']" @click="onClickItem('download')">
      <template #icon><svg-icon icon-class="menu-download" /></template>
      <span>下载</span>
    </GiOptionItem>
    <GiOptionItem v-permission="['system:file:list']" @click="onClickItem('detail')">
      <template #icon><svg-icon icon-class="menu-detail" /></template>
      <span>详情</span>
    </GiOptionItem>
    <GiOptionItem v-permission="['system:file:delete']" @click="onClickItem('delete')">
      <template #icon><svg-icon icon-class="menu-delete" /></template>
      <span>删除</span>
    </GiOptionItem>
  </GiOption>
</template>

<script setup lang="ts">
  import GiOption from '@/components/gi-option/index.vue';
  import GiOptionItem from '@/components/gi-option-item/index.vue';

  interface Props {
    showClassStyle?: boolean;
  }

  withDefaults(defineProps<Props>(), {
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
