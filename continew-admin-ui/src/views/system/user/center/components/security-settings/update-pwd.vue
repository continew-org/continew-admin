<template>
  <a-list-item-meta>
    <template #avatar>
      <a-typography-paragraph>
        {{ $t('userCenter.securitySettings.updatePwd.label.password') }}
      </a-typography-paragraph>
    </template>
    <template #description>
      <div class="content">
        <a-typography-paragraph v-if="loginStore.pwdResetTime">
          {{ $t('userCenter.securitySettings.updatePwd.placeholder.success.password') }}
        </a-typography-paragraph>
        <a-typography-paragraph v-else class="tip">
          {{ $t('userCenter.securitySettings.updatePwd.placeholder.error.password') }}
        </a-typography-paragraph>
      </div>
      <div class="operation">
        <a-link @click="toUpdate">
          {{ $t('userCenter.securitySettings.button.update') }}
        </a-link>
      </div>
    </template>
  </a-list-item-meta>

  <a-modal
    v-model:visible="visible"
    :title="$t('userCenter.securitySettings.updatePwd.modal.title')"
    :mask-closable="false"
    @cancel="handleCancel"
    @before-ok="handleUpdate"
  >
    <a-form ref="formRef" :model="formData" :rules="rules">
      <a-form-item
        field="oldPassword"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.securitySettings.updatePwd.form.label.oldPassword')"
      >
        <a-input-password
          v-model="formData.oldPassword"
          :placeholder="$t('userCenter.securitySettings.updatePwd.form.placeholder.oldPassword')"
          size="large"
          allow-clear
          max-length="32"
        >
        </a-input-password>
      </a-form-item>
      <a-form-item
        field="newPassword"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.securitySettings.updatePwd.form.label.newPassword')"
      >
        <a-input-password
          v-model="formData.newPassword"
          :placeholder="$t('userCenter.securitySettings.updatePwd.form.placeholder.newPassword')"
          size="large"
          allow-clear
          max-length="32"
        >
        </a-input-password>
      </a-form-item>
      <a-form-item
        field="rePassword"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.securitySettings.updatePwd.form.label.rePassword')"
      >
        <a-input-password
          v-model="formData.rePassword"
          :placeholder="$t('userCenter.securitySettings.updatePwd.form.placeholder.rePassword')"
          size="large"
          allow-clear
          max-length="32"
        >
        </a-input-password>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useLoginStore } from '@/store';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import useLoading from '@/hooks/loading';
  import { FieldRule, Message } from '@arco-design/web-vue';
  import { updatePassword } from '@/api/system/user-center';
  import { encryptByRsa } from '@/utils/encrypt';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading();
  const loginStore = useLoginStore();
  const visible = ref(false);
  const formRef = ref<FormInstance>();
  const formData = reactive({
    oldPassword: '',
    newPassword: '',
    rePassword: '',
  });
  const rules = computed((): Record<string, FieldRule[]> => {
    return {
      oldPassword: [
        { required: true, message: t('userCenter.securitySettings.updatePwd.form.error.required.oldPassword') }
      ],
      newPassword: [
        { required: true, message: t('userCenter.securitySettings.updatePwd.form.error.required.newPassword') },
        { match: /^(?=.*\d)(?=.*[a-z]).{6,32}$/, message: t('userCenter.securitySettings.updatePwd.form.error.match.newPassword') },
        {
          validator: (value, callback) => {
            if (value === formData.oldPassword) {
              callback(t('userCenter.securitySettings.updatePwd.form.error.validator.newPassword'))
            } else {
              callback()
            }
          }
        }
      ],
      rePassword: [
        { required: true, message: t('userCenter.securitySettings.updatePwd.form.error.required.rePassword') },
        {
          validator: (value, callback) => {
            if (value !== formData.newPassword) {
              callback(t('userCenter.securitySettings.updatePwd.form.error.validator.rePassword'))
            } else {
              callback()
            }
          }
        }
      ],
    }
  });

  // 确定修改
  const handleUpdate = async () => {
    if (loading.value) return false;
    const errors = await formRef.value?.validate();
    if (errors) return false;
    setLoading(true);
    try {
      const res = await updatePassword({
        oldPassword: encryptByRsa(formData.oldPassword) || '',
        newPassword: encryptByRsa(formData.newPassword) || '',
      });
      if (res.success) Message.success(res.msg);
    } finally {
      setLoading(false);
    }
    return true;
  };

  // 取消修改
  const handleCancel = () => {
    visible.value = false;
    formRef.value?.resetFields()
  };

  // 打开修改窗口
  const toUpdate = () => {
    visible.value = true;
  };
</script>

<style scoped lang="less"></style>
