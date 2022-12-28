<template>
  <div class="container">
    <Breadcrumb :items="['menu.result', 'menu.result.error']" />
    <div class="wrapper">
      <a-result
        class="result"
        status="error"
        :title="$t('error.result.title')"
        :subtitle="$t('error.result.subTitle')"
      >
        <template #extra>
          <a-space class="operation-wrap" :size="16">
            <a-button key="again" type="secondary">
              {{ $t('error.result.goBack') }}
            </a-button>
            <a-button key="back" type="primary">
              {{ $t('error.result.retry') }}
            </a-button>
          </a-space>
        </template>
      </a-result>

      <div class="details-wrapper">
        <a-typography-title :heading="6" style="margin-top: 0">
          {{ $t('error.detailTitle') }}
        </a-typography-title>
        <a-typography-paragraph style="margin-bottom: 0">
          <ol>
            <li>
              {{ $t('error.detailLine.record') }}
              <a-link>
                <IconLink />
                {{ $t('error.detailLine.record.link') }}
              </a-link>
            </li>
            <li>{{ $t('error.detailLine.auth') }}</li>
          </ol>
        </a-typography-paragraph>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup></script>

<script lang="ts">
  export default {
    name: 'Error',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }
  .wrapper {
    padding: 24px 150px;
    background-color: var(--color-bg-2);
    border-radius: 4px;
  }

  .result {
    margin: 150px 0 36px 0;
  }

  .operation-wrap {
    margin-bottom: 40px;
    text-align: center;
  }

  .details-wrapper {
    width: 100%;
    margin-bottom: 150px;
    padding: 20px;
    background-color: rgb(var(--gray-1));
  }
</style>
