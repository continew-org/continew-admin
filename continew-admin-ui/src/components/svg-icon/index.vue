<template>
  <svg :class="svgClass" aria-hidden="true">
    <use :xlink:href="iconName" :fill="color" />
  </svg>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  const props = defineProps({
    iconClass: {
      type: String,
      required: true,
    },
    className: {
      type: String,
      default: '',
    },
    color: {
      type: String,
      default: '',
    },
  });

  const iconName = computed(() => `#icon-${props.iconClass}`);
  const svgClass = computed(() => {
    if (props.className) {
      return `svg-icon ${props.className}`;
    }
    return 'svg-icon';
  });
</script>

<script lang="ts">
  export default {
    name: 'SvgIcon',
  };
</script>

<style scoped lang="less">
  .sub-el-icon,
  .nav-icon {
    display: inline-block;
    font-size: 15px;
    margin-right: 12px;
    position: relative;
  }
  .svg-icon {
    width: 1em;
    height: 1em;
    position: relative;
    fill: currentColor;
    vertical-align: -2px;
  }
</style>
