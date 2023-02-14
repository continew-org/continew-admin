<template>
  <a-form
    ref="formRef"
    :model="formData"
    class="form"
    :label-col-props="{ span: 6 }"
    :wrapper-col-props="{ span: 18 }"
  >
    <a-form-item
      field="advertisingSource"
      :label="$t('stepForm.form.label.advertisingSource')"
      :rules="[
        {
          required: true,
          message: $t('stepForm.form.error.advertisingSource.required'),
        },
      ]"
    >
      <a-input
        v-model="formData.advertisingSource"
        :placeholder="$t('stepForm.placeholder.advertisingSource')"
      />
    </a-form-item>
    <a-form-item
      field="advertisingMedia"
      :label="$t('stepForm.form.label.advertisingMedia')"
      :rules="[
        {
          required: true,
          message: $t('stepForm.form.error.advertisingMedia.required'),
        },
      ]"
    >
      <a-input
        v-model="formData.advertisingMedia"
        :placeholder="$t('stepForm.placeholder.advertisingMedia')"
      />
    </a-form-item>
    <a-form-item
      field="keyword"
      :label="$t('stepForm.form.label.keyword')"
      :rules="[
        { required: true, message: $t('stepForm.form.error.keyword.required') },
      ]"
    >
      <a-select
        v-model="formData.keyword"
        :placeholder="$t('stepForm.placeholder.keyword')"
        multiple
      >
        <a-option>今日头条</a-option>
        <a-option>火山</a-option>
      </a-select>
    </a-form-item>
    <a-form-item
      field="pushNotify"
      :label="$t('stepForm.form.label.pushNotify')"
      :rules="[{ required: true }]"
    >
      <a-switch v-model="formData.pushNotify" />
    </a-form-item>
    <a-form-item
      field="advertisingContent"
      :label="$t('stepForm.form.label.advertisingContent')"
      :rules="[
        {
          required: true,
          message: $t('stepForm.form.error.advertisingContent.required'),
        },
        {
          maxLength: 200,
          message: $t('stepForm.form.error.advertisingContent.maxLength'),
        },
      ]"
      row-class="keep-margin"
    >
      <a-textarea
        v-model="formData.advertisingContent"
        :placeholder="$t('stepForm.placeholder.advertisingContent')"
      />
    </a-form-item>
    <a-form-item>
      <!-- <a-button type="primary" @click="onNextClick">
        {{ $t('stepForm.button.next') }}
      </a-button> -->
      <a-space>
        <a-button type="secondary" @click="goPrev">
          {{ $t('stepForm.button.prev') }}
        </a-button>
        <a-button type="primary" @click="onNextClick">
          {{ $t('stepForm.button.next') }}
        </a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { ChannelInfoModel } from '@/api/form';

  const emits = defineEmits(['changeStep']);

  const formRef = ref<FormInstance>();
  const formData = ref<ChannelInfoModel>({
    advertisingSource: '',
    advertisingMedia: '',
    keyword: [],
    pushNotify: true,
    advertisingContent: '',
  });

  const onNextClick = async () => {
    const res = await formRef.value?.validate();
    if (!res) {
      emits('changeStep', 'submit', { ...formData.value });
    }
  };
  const goPrev = () => {
    emits('changeStep', 'backward');
  };
</script>

<style scoped lang="less">
  .container {
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
    width: 540px;
  }

  .form-content {
    padding: 8px 50px 0 30px;
  }
</style>
