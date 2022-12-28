<template>
  <a-card
    class="general-card"
    :title="$t('userSetting.certification.title.enterprise')"
    :header-style="{ padding: '0px 20px 16px 20px' }"
  >
    <template #extra>
      <a-link>{{ $t('userSetting.certification.extra.enterprise') }}</a-link>
    </template>
    <a-descriptions
      class="card-content"
      :data="renderData"
      :column="3"
      align="right"
      layout="inline-horizontal"
      :label-style="{ fontWeight: 'normal' }"
      :value-style="{
        width: '200px',
        paddingLeft: '8px',
        textAlign: 'left',
      }"
    >
      <template #label="{ label }">{{ $t(label) }} :</template>
      <template #value="{ value, data }">
        <a-tag
          v-if="data.label === 'userSetting.certification.label.status'"
          color="green"
          size="small"
        >
          已认证
        </a-tag>
        <span v-else>{{ value }}</span>
      </template>
    </a-descriptions>
  </a-card>
</template>

<script lang="ts" setup>
  import { PropType, computed } from 'vue';
  import { EnterpriseCertificationModel } from '@/api/user-center';
  import type { DescData } from '@arco-design/web-vue/es/descriptions/interface';

  const props = defineProps({
    enterpriseInfo: {
      type: Object as PropType<EnterpriseCertificationModel>,
      required: true,
    },
  });
  const renderData = computed(() => {
    const {
      accountType,
      status,
      time,
      legalPerson,
      certificateType,
      authenticationNumber,
      enterpriseName,
      enterpriseCertificateType,
      organizationCode,
    } = props.enterpriseInfo;
    return [
      {
        label: 'userSetting.certification.label.accountType',
        value: accountType,
      },
      {
        label: 'userSetting.certification.label.status',
        value: status,
      },
      {
        label: 'userSetting.certification.label.time',
        value: time,
      },
      {
        label: 'userSetting.certification.label.legalPerson',
        value: legalPerson,
      },
      {
        label: 'userSetting.certification.label.certificateType',
        value: certificateType,
      },
      {
        label: 'userSetting.certification.label.authenticationNumber',
        value: authenticationNumber,
      },
      {
        label: 'userSetting.certification.label.enterpriseName',
        value: enterpriseName,
      },
      {
        label: 'userSetting.certification.label.enterpriseCertificateType',
        value: enterpriseCertificateType,
      },
      {
        label: 'userSetting.certification.label.organizationCode',
        value: organizationCode,
      },
    ] as DescData[];
  });
</script>

<style scoped lang="less">
  .card-content {
    width: 100%;
    padding: 20px;
    background-color: rgb(var(--gray-1));
  }
  .item-label {
    min-width: 98px;
    text-align: right;
    color: var(--color-text-8);
    &:after {
      content: ':';
    }
  }
</style>
