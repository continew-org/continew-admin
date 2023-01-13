<template>
  <a-list-item-meta>
    <template #avatar>
      <a-typography-paragraph>
        {{ $t('userCenter.securitySettings.updateEmail.label.email') }}
      </a-typography-paragraph>
    </template>
    <template #description>
      <div class="content">
        <a-typography-paragraph v-if="loginStore.email">
          {{ $t('userCenter.securitySettings.updateEmail.placeholder.success.email') }}：{{ loginStore.email }}
        </a-typography-paragraph>
        <a-typography-paragraph v-else class="tip">
          {{ $t('userCenter.securitySettings.updateEmail.placeholder.error.email') }}
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
    :title="$t('userCenter.securitySettings.updateEmail.modal.title')"
    :mask-closable="false"
    @cancel="handleCancel"
    @before-ok="handleUpdate"
  >
    <a-form ref="formRef" :model="formData" :rules="rules">
      <a-form-item
        field="newEmail"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.securitySettings.updateEmail.form.label.newEmail')"
      >
        <a-input
          v-model="formData.newEmail"
          :placeholder="$t('userCenter.securitySettings.updateEmail.form.placeholder.newEmail')"
          size="large"
          allow-clear
        >
        </a-input>
      </a-form-item>
      <a-form-item
        field="captcha"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.securitySettings.updateEmail.form.label.captcha')"
      >
        <a-input
          v-model="formData.captcha"
          :placeholder="$t('userCenter.securitySettings.updateEmail.form.placeholder.captcha')"
          size="large"
          style="width: 80%"
          allow-clear
          max-length="6"
        >
        </a-input>
        <a-button
          class="captcha-btn"
          type="primary"
          size="large"
          :loading="captchaLoading"
          :disabled="captchaDisable"
          @click="sendCaptcha"
        >
          {{ captchaBtnName }}
        </a-button>
      </a-form-item>
      <a-form-item
        field="currentPassword"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.securitySettings.updateEmail.form.label.currentPassword')"
      >
        <a-input-password
          v-model="formData.currentPassword"
          :placeholder="$t('userCenter.securitySettings.updateEmail.form.placeholder.currentPassword')"
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
  import { getMailCaptcha } from '@/api/common/captcha';
  import { updateEmail } from '@/api/system/user-center';
  import { encryptByRsa } from '@/utils/encrypt';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading();
  const loginStore = useLoginStore();
  const visible = ref(false);
  const captchaBtnNameKey = ref('userCenter.securitySettings.updateEmail.form.sendCaptcha');
  const captchaBtnName = computed(() => t(captchaBtnNameKey.value));
  const captchaLoading = ref(false);
  const captchaDisable = ref(false);
  const captchaTime = ref(60);
  const captchaTimer = ref();
  const formRef = ref<FormInstance>();
  const formData = reactive({
    newEmail: '',
    captcha: '',
    currentPassword: '',
  });
  const rules = computed((): Record<string, FieldRule[]> => {
    return {
      newEmail: [
        { required: true, message: t('userCenter.securitySettings.updateEmail.form.error.required.newEmail') },
        { type: 'email', message: t('userCenter.securitySettings.updateEmail.form.error.match.newEmail') },
        {
          validator: (value, callback) => {
            if (value === loginStore.email) {
              callback(t('userCenter.securitySettings.updateEmail.form.error.validator.newEmail'))
            } else {
              callback()
            }
          }
        }
      ],
      captcha: [
        { required: true, message: t('userCenter.securitySettings.updateEmail.form.error.required.captcha') }
      ],
      currentPassword: [
        { required: true, message: t('userCenter.securitySettings.updateEmail.form.error.required.currentPassword') }
      ]
    }
  });

  // 重置验证码相关
  const resetCaptcha = () => {
    window.clearInterval(captchaTimer.value);
    captchaTime.value = 60;
    captchaBtnNameKey.value = 'userCenter.securitySettings.updateEmail.form.sendCaptcha';
    captchaDisable.value = false;
  }

  // 发送验证码
  const sendCaptcha = async () => {
    if (captchaLoading.value) return;
    const errors = await formRef.value?.validateField('newEmail');
    if (errors) return;
    captchaLoading.value = true;
    captchaBtnNameKey.value = 'userCenter.securitySettings.updateEmail.form.loading.sendCaptcha';
    try {
      const res = await getMailCaptcha({
        email: formData.newEmail
      });
      if (res.success) {
        captchaLoading.value = false;
        captchaDisable.value = true;
        captchaBtnNameKey.value = `${t('userCenter.securitySettings.updateEmail.form.reSendCaptcha')}(${captchaTime.value -= 1}s)`;
        Message.success(res.msg);

        captchaTimer.value = window.setInterval(function() {
          captchaTime.value -= 1;
          captchaBtnNameKey.value = `${t('userCenter.securitySettings.updateEmail.form.reSendCaptcha')}(${captchaTime.value}s)`;
          if (captchaTime.value < 0) {
            window.clearInterval(captchaTimer.value);
            captchaTime.value = 60;
            captchaBtnNameKey.value = t('userCenter.securitySettings.updateEmail.form.reSendCaptcha');
            captchaDisable.value = false;
          }
        }, 1000)
      }
    } catch (err) {
      resetCaptcha();
      captchaLoading.value = false;
      console.log((err as Error));
    }
  };

  // 确定修改
  const handleUpdate = async () => {
    if (loading.value) return false;
    const errors = await formRef.value?.validate();
    if (errors) return false;
    setLoading(true);
    try {
      const res = await updateEmail({
        newEmail: formData.newEmail,
        captcha: formData.captcha,
        currentPassword: encryptByRsa(formData.currentPassword) || '',
      });
      await loginStore.getInfo();
      if (res.success) Message.success(res.msg);
    } finally {
      setLoading(false);
    }
    return true;
  };

  // 取消修改
  const handleCancel = () => {
    visible.value = false;
    formRef.value?.resetFields();
    resetCaptcha();
  };

  // 打开修改窗口
  const toUpdate = () => {
    visible.value = true;
  };
</script>

<style scoped lang="less">
  .captcha-btn {
    margin-left: 5px;
  }
</style>
