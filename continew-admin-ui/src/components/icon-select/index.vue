<template>
  <div class="icon-body">
    <a-input-search
      v-model="iconName"
      placeholder="请输入图标名称"
      allow-clear
      style="position: relative"
      @clear="filterIcons"
      @input="filterIcons"
    />
    <div class="icon-list">
      <div
        v-for="(icon, index) in iconList"
        :key="index"
        @click="handleSelectedIcon(icon)"
      >
        <svg-icon :icon-class="icon" style="height: 32px; width: 16px" />
        <span>{{ icon }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import icons from './requireIcons';

  const iconName = ref('');
  const iconList = ref(icons);

  const emits = defineEmits(['selected']);

  /**
   * 过滤图标
   */
  const filterIcons = () => {
    iconList.value = icons;
    if (iconName.value) {
      iconList.value = icons.filter(
        (icon) => icon.indexOf(iconName.value) !== -1
      );
    }
  };

  /**
   * 选中图标
   *
   * @param name 图标名称
   */
  const handleSelectedIcon = (name: string) => {
    emits('selected', name);
    document.body.click();
  };

  /**
   * 重置
   */
  const reset = () => {
    iconName.value = '';
    iconList.value = icons;
  };

  defineExpose({
    reset,
  });
</script>

<script lang="ts">
  export default {
    name: 'IconSelect',
  };
</script>

<style scoped lang="less">
  .icon-body {
    width: 100%;
    padding: 10px;
    .icon-list {
      height: 200px;
      width: 420px;
      overflow-y: scroll;
      div {
        height: 30px;
        line-height: 35px;
        margin-bottom: -5px;
        cursor: pointer;
        width: 33%;
        float: left;
      }
      span {
        display: inline-block;
        margin-left: 10px;
        vertical-align: -0.15em;
        fill: currentColor;
        overflow: hidden;
      }
    }
  }
</style>
