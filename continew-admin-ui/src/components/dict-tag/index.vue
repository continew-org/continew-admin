<template>
  <span v-if="!dictItem"></span>
  <span v-else-if="!dictItem.color">{{ dictItem.label }}</span>
  <a-tag v-else-if="dictItem.color === 'primary'" color="arcoblue">{{
    dictItem.label
  }}</a-tag>
  <a-tag v-else-if="dictItem.color === 'success'" color="green">{{
    dictItem.label
  }}</a-tag>
  <a-tag v-else-if="dictItem.color === 'warning'" color="orangered">{{
    dictItem.label
  }}</a-tag>
  <a-tag v-else-if="dictItem.color === 'error'" color="red">{{
    dictItem.label
  }}</a-tag>
  <a-tag v-else-if="dictItem.color === 'default'" color="gray">{{
    dictItem.label
  }}</a-tag>
  <a-tag v-else :color="dictItem.color">{{ dictItem.label }}</a-tag>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { LabelValueState } from '@/store/modules/dict/types';

  const props = defineProps({
    dict: {
      type: Array<LabelValueState>,
      required: true,
    },
    value: {
      type: [Number, String],
      required: true,
    },
  });

  const dictItem = computed(() =>
    props.dict.find(
      (d) => d.value === String(props.value) || d.value === Number(props.value),
    ),
  );
</script>

<script lang="ts">
  export default {
    name: 'DictTag',
  };
</script>

<style scoped lang="less"></style>
