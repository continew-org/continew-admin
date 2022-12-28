<template>
  <a-form
    ref="formRef"
    :model="formData"
    class="form"
    :label-col-props="{ span: 8 }"
    :wrapper-col-props="{ span: 16 }"
  >
    <a-form-item
      field="email"
      :label="$t('userSetting.basicInfo.form.label.email')"
      :rules="[
        {
          required: true,
          message: $t('userSetting.form.error.email.required'),
        },
      ]"
    >
      <a-input
        v-model="formData.email"
        :placeholder="$t('userSetting.basicInfo.placeholder.email')"
      />
    </a-form-item>
    <a-form-item
      field="nickname"
      :label="$t('userSetting.basicInfo.form.label.nickname')"
      :rules="[
        {
          required: true,
          message: $t('userSetting.form.error.nickname.required'),
        },
      ]"
    >
      <a-input
        v-model="formData.nickname"
        :placeholder="$t('userSetting.basicInfo.placeholder.nickname')"
      />
    </a-form-item>
    <a-form-item
      field="countryRegion"
      :label="$t('userSetting.basicInfo.form.label.countryRegion')"
      :rules="[
        {
          required: true,
          message: $t('userSetting.form.error.countryRegion.required'),
        },
      ]"
    >
      <a-select
        v-model="formData.countryRegion"
        :placeholder="$t('userSetting.basicInfo.placeholder.area')"
      >
        <a-option value="China">中国</a-option>
      </a-select>
    </a-form-item>
    <a-form-item
      field="area"
      :label="$t('userSetting.basicInfo.form.label.area')"
      :rules="[
        {
          required: true,
          message: $t('userSetting.form.error.area.required'),
        },
      ]"
    >
      <a-cascader
        v-model="formData.area"
        :placeholder="$t('userSetting.basicInfo.placeholder.area')"
        :options="[
          {
            label: '北京',
            value: 'beijing',
            children: [
              {
                label: '北京',
                value: 'beijing',
                children: [
                  {
                    label: '朝阳',
                    value: 'chaoyang',
                  },
                ],
              },
            ],
          },
        ]"
        allow-clear
      />
    </a-form-item>
    <a-form-item
      field="address"
      :label="$t('userSetting.basicInfo.form.label.address')"
    >
      <a-input
        v-model="formData.address"
        :placeholder="$t('userSetting.basicInfo.placeholder.address')"
      />
    </a-form-item>
    <a-form-item
      field="profile"
      :label="$t('userSetting.basicInfo.form.label.profile')"
      :rules="[
        {
          maxLength: 200,
          message: $t('userSetting.form.error.profile.maxLength'),
        },
      ]"
      row-class="keep-margin"
    >
      <a-textarea
        v-model="formData.profile"
        :placeholder="$t('userSetting.basicInfo.placeholder.profile')"
      />
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-button type="primary" @click="validate">
          {{ $t('userSetting.save') }}
        </a-button>
        <a-button type="secondary" @click="reset">
          {{ $t('userSetting.reset') }}
        </a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { BasicInfoModel } from '@/api/user-center';

  const formRef = ref<FormInstance>();
  const formData = ref<BasicInfoModel>({
    email: '',
    nickname: '',
    countryRegion: '',
    area: '',
    address: '',
    profile: '',
  });
  const validate = async () => {
    const res = await formRef.value?.validate();
    if (!res) {
      // do some thing
      // you also can use html-type to submit
    }
  };
  const reset = async () => {
    await formRef.value?.resetFields();
  };
</script>

<style scoped lang="less">
  .form {
    width: 540px;
    margin: 0 auto;
  }
</style>
