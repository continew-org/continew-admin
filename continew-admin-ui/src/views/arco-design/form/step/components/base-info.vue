<template>
  <a-form
    ref="formRef"
    :model="formData"
    class="form"
    :label-col-props="{ span: 6 }"
    :wrapper-col-props="{ span: 18 }"
  >
    <a-form-item
      field="activityName"
      :label="$t('stepForm.form.label.activityName')"
      :rules="[
        {
          required: true,
          message: $t('stepForm.form.error.activityName.required'),
        },
        {
          match: /^[a-zA-Z0-9\u4e00-\u9fa5]{1,20}$/,
          message: $t('stepForm.form.error.activityName.pattern'),
        },
      ]"
    >
      <a-input
        v-model="formData.activityName"
        :placeholder="$t('stepForm.placeholder.activityName')"
      />
    </a-form-item>
    <a-form-item
      field="channelType"
      :label="$t('stepForm.form.label.channelType')"
      :rules="[
        {
          required: true,
          message: $t('stepForm.form.error.channelType.required'),
        },
      ]"
    >
      <a-select
        v-model="formData.channelType"
        :placeholder="$t('stepForm.placeholder.channelType')"
      >
        <a-option>APP通用渠道</a-option>
      </a-select>
    </a-form-item>
    <a-form-item
      field="promotionTime"
      :label="$t('stepForm.form.label.promotionTime')"
      :rules="[
        {
          required: true,
          message: $t('stepForm.form.error.promotionTime.required'),
        },
      ]"
    >
      <a-range-picker v-model="formData.promotionTime" />
    </a-form-item>
    <a-form-item
      field="promoteLink"
      :label="$t('stepForm.form.label.promoteLink')"
      :rules="[
        {
          required: true,
          message: $t('stepForm.form.error.promoteLink.required'),
        },
        {
          type: 'url',
          message: $t('stepForm.form.error.promoteLink.pattern'),
        },
      ]"
      row-class="keep-margin"
    >
      <a-input
        v-model="formData.promoteLink"
        :placeholder="$t('stepForm.placeholder.promoteLink')"
      />
      <template #help>
        <span>{{ $t('stepForm.form.tip.promoteLink') }}</span>
      </template>
    </a-form-item>
    <a-form-item>
      <a-button type="primary" @click="onNextClick">
        {{ $t('stepForm.button.next') }}
      </a-button>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { BaseInfoModel } from '@/api/form';

  const emits = defineEmits(['changeStep']);
  const formRef = ref<FormInstance>();
  const formData = ref<BaseInfoModel>({
    activityName: '',
    channelType: '',
    promotionTime: [],
    promoteLink: 'https://arco.design',
  });

  const onNextClick = async () => {
    const res = await formRef.value?.validate();
    if (!res) {
      emits('changeStep', 'forward', { ...formData.value });
    }
  };
</script>

<style scoped lang="less">
  .container {
    padding: 20px;
    .keep-margin {
      margin-bottom: 20px;
    }
  }

  .wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 64px 0;
    background-color: var(--color-bg-2);
  }

  .steps {
    margin-bottom: 36px;
  }

  .form {
    width: 500px;
  }

  .form-content {
    padding: 8px 50px 0 30px;
  }
</style>
