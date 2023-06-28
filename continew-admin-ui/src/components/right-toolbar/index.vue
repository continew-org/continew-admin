<template>
  <div class="header-operation-right">
    <a-button-group>
      <a-tooltip :content="showQuery ? '隐藏搜索栏' : '显示搜索栏'">
        <a-button @click="toggleSearch"
          ><template #icon><icon-search /></template
        ></a-button>
      </a-tooltip>
      <a-tooltip content="刷新">
        <a-button @click="handleRefresh"
          ><template #icon><icon-refresh /></template
        ></a-button>
      </a-tooltip>
    </a-button-group>
  </div>
</template>

<script lang="ts" setup>
  const props = defineProps({
    showQuery: {
      type: Boolean,
      default: true,
    },
  });

  const emits = defineEmits(['update:showQuery', 'refresh']);

  /**
   * 切换搜索栏（显示或隐藏）
   */
  const toggleSearch = () => {
    emits('update:showQuery', !props.showQuery);
  };

  /**
   * 刷新
   */
  const handleRefresh = () => {
    emits('refresh');
  };
</script>

<script lang="ts">
  export default {
    name: 'RightToolbar',
  };
</script>

<style scoped lang="less"></style>
