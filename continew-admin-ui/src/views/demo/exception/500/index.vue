<template>
  <div class="container">
    <Breadcrumb :items="['menu.exception', 'menu.exception.500']" />
    <div class="content">
      <a-result
        class="result"
        status="500"
        :subtitle="$t('exception.result.500.description')"
      />
      <a-button key="back" type="primary">
        {{ $t('exception.result.500.back') }}
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup></script>

<script lang="ts">
  export default {
    name: '500',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
    height: calc(100% - 40px);
    :deep(.content) {
      position: relative;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      text-align: center;
      background-color: var(--color-bg-1);
      border-radius: 4px;
    }
  }
</style>
