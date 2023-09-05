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
      return `arco-icon ${props.className}`;
    }
    return 'arco-icon';
  });
</script>

<script lang="ts">
  export default {
    name: 'SvgIcon',
  };
</script>

<style scoped lang="less"></style>
