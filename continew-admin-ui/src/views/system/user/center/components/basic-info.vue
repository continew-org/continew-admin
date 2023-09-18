<template>
  <a-form
    ref="formRef"
    :model="form"
    :rules="rules"
    :label-col-props="{ span: 8 }"
    :wrapper-col-props="{ span: 16 }"
    size="large"
    class="form"
  >
    <a-form-item
      :label="$t('userCenter.basicInfo.form.label.username')"
      disabled
    >
      <a-input
        v-model="form.username"
        :placeholder="$t('userCenter.basicInfo.form.placeholder.username')"
        :max-length="64"
      />
    </a-form-item>
    <a-form-item
      :label="$t('userCenter.basicInfo.form.label.nickname')"
      field="nickname"
    >
      <a-input
        v-model="form.nickname"
        :placeholder="$t('userCenter.basicInfo.form.placeholder.nickname')"
        :max-length="30"
      />
    </a-form-item>
    <a-form-item
      :label="$t('userCenter.basicInfo.form.label.gender')"
      field="gender"
    >
      <a-radio-group v-model="form.gender">
        <a-radio :value="1">男</a-radio>
        <a-radio :value="2">女</a-radio>
        <a-radio :value="0" disabled>未知</a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-button :loading="loading" type="primary" @click="handleSave">
          {{ $t('userCenter.basicInfo.form.save') }}
        </a-button>
        <a-button @click="handleReset">
          {{ $t('userCenter.basicInfo.form.reset') }}
        </a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive, computed } from 'vue';
  import { FieldRule } from '@arco-design/web-vue';
  import { BasicInfoModel, updateBasicInfo } from '@/api/system/user-center';
  import { useI18n } from 'vue-i18n';
  import { useLoginStore } from '@/store';

  const { proxy } = getCurrentInstance() as any;

  const { t } = useI18n();
  const loginStore = useLoginStore();
  const loading = ref(false);

  const data = reactive({
    // 表单数据
    form: {
      username: loginStore.username,
      nickname: loginStore.nickname,
      gender: loginStore.gender,
    } as BasicInfoModel,
    // 表单验证规则
    rules: computed((): Record<string, FieldRule[]> => {
      return {
        username: [
          {
            required: true,
            message: t('userCenter.basicInfo.form.error.required.username'),
          },
          {
            match: /^[a-zA-Z][a-zA-Z0-9_]{3,15}$/,
            message: t('userCenter.basicInfo.form.error.match.username'),
          },
        ],
        nickname: [
          {
            required: true,
            message: t('userCenter.basicInfo.form.error.required.nickname'),
          },
          {
            match: /^[\u4e00-\u9fa5a-zA-Z0-9_-]{1,20}$/,
            message: t('userCenter.basicInfo.form.error.match.nickname'),
          },
        ],
      };
    }),
  });
  const { form, rules } = toRefs(data);

  /**
   * 保存
   */
  const handleSave = () => {
    if (loading.value) return;
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        loading.value = true;
        updateBasicInfo({
          nickname: form.value.nickname,
          gender: form.value.gender,
        })
          .then((res) => {
            loginStore.getInfo();
            proxy.$message.success(t('userCenter.basicInfo.form.save.success'));
          })
          .finally(() => {
            loading.value = false;
          });
      }
    });
  };

  /**
   * 重置
   */
  const handleReset = () => {
    proxy.$refs.formRef.resetFields();
  };
</script>

<style scoped lang="less">
  .form {
    width: 540px;
    margin: 0 auto;
  }
</style>
