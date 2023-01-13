<template>
  <a-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    class="form"
    :label-col-props="{ span: 8 }"
    :wrapper-col-props="{ span: 16 }"
  >
    <a-form-item
      :label="$t('userCenter.basicInfo.form.label.username')"
      disabled
    >
      <a-input
        v-model="formData.username"
        :placeholder="$t('userCenter.basicInfo.form.placeholder.username')"
        size="large"
        max-length="50"
      />
    </a-form-item>
    <a-form-item
      field="nickname"
      :label="$t('userCenter.basicInfo.form.label.nickname')"
    >
      <a-input
        v-model="formData.nickname"
        :placeholder="$t('userCenter.basicInfo.form.placeholder.nickname')"
        size="large"
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
          {{ $t('userCenter.basicInfo.form.save') }}
        </a-button>
        <a-button type="secondary" @click="reset">
          {{ $t('userCenter.basicInfo.form.reset') }}
        </a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useLoginStore } from '@/store';
  import { updateBasicInfo } from '@/api/system/user-center';
  import useLoading from '@/hooks/loading';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { BasicInfoModel } from '@/api/system/user-center';
  import { FieldRule, Message } from '@arco-design/web-vue';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading();
  const loginStore = useLoginStore();
  const formRef = ref<FormInstance>();
  const formData = ref<BasicInfoModel>({
    username: loginStore.username,
    nickname: loginStore.nickname,
    gender: loginStore.gender,
  });
  const rules = computed((): Record<string, FieldRule[]> => {
    return {
      username: [
        { required: true, message: t('userCenter.basicInfo.form.error.required.username') }
      ],
      nickname: [
        { required: true, message: t('userCenter.basicInfo.form.error.required.nickname') }
      ],
    }
  });

  // 保存
  const save = async () => {
    if (loading.value) return;
    const errors = await formRef.value?.validate();
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
