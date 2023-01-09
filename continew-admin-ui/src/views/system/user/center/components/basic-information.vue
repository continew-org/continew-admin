<template>
  <a-form
    ref="formRef"
    :model="formData"
    class="form"
    :label-col-props="{ span: 8 }"
    :wrapper-col-props="{ span: 16 }"
  >
    <a-form-item
      :label="$t('userCenter.basicInfo.form.label.username')"
      :rules="[
        {
          required: true,
          message: $t('userCenter.form.error.username.required'),
        },
      ]"
      disabled
    >
      <a-input
        v-model="formData.username"
        :placeholder="$t('userCenter.basicInfo.placeholder.username')"
        max-length="50"
      />
    </a-form-item>
    <a-form-item
      field="nickname"
      :label="$t('userCenter.basicInfo.form.label.nickname')"
      :rules="[
        {
          required: true,
          message: $t('userCenter.form.error.nickname.required'),
        },
      ]"
    >
      <a-input
        v-model="formData.nickname"
        :placeholder="$t('userCenter.basicInfo.placeholder.nickname')"
        max-length="32"
      />
    </a-form-item>
    <a-form-item
      field="gender"
      :label="$t('userCenter.basicInfo.form.label.gender')"
    >
      <a-radio-group v-model="formData.gender">
        <a-radio :value="1">男</a-radio>
        <a-radio :value="2">女</a-radio>
        <a-radio :value="0" disabled>未知</a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-button type="primary" :loading="loading" @click="save">
          {{ $t('userCenter.save') }}
        </a-button>
        <a-button type="secondary" @click="reset">
          {{ $t('userCenter.reset') }}
        </a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useLoginStore } from '@/store';
  import { updateBasicInfo } from '@/api/system/user-center';
  import useLoading from '@/hooks/loading';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { BasicInfoModel } from '@/api/system/user-center';
  import { Message } from "@arco-design/web-vue";

  const { loading, setLoading } = useLoading();
  const loginStore = useLoginStore();
  const formRef = ref<FormInstance>();
  const formData = ref<BasicInfoModel>({
    username: loginStore.username,
    nickname: loginStore.nickname,
    gender: loginStore.gender,
  });

  // 保存
  const save = async () => {
    const errors = await formRef.value?.validate();
    if (loading.value) return;
    if (!errors) {
      setLoading(true);
      try {
        const res = await updateBasicInfo({
          nickname: formData.value.nickname,
          gender: formData.value.gender,
        });
        await loginStore.getInfo();
        if (res.success) Message.success(res.msg);
      } finally {
        setLoading(false);
      }
    }
  };

  // 重置
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
