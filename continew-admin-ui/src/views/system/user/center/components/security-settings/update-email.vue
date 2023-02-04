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
        <a-link :title="$t('userCenter.securitySettings.button.update')" @click="toUpdate">
          {{ $t('userCenter.securitySettings.button.update') }}
        </a-link>
      </div>
    </template>
  </a-list-item-meta>

  <a-modal
    :title="$t('userCenter.securitySettings.updateEmail.modal.title')"
    :visible="visible"
    :mask-closable="false"
    @ok="handleUpdate"
    @cancel="handleCancel"
  >
    <a-form ref="formRef" :model="form" :rules="rules">
      <a-form-item :label="$t('userCenter.securitySettings.updateEmail.form.label.newEmail')" field="newEmail">
        <a-input
          v-model="form.newEmail"
          :placeholder="$t('userCenter.securitySettings.updateEmail.form.placeholder.newEmail')"
          allow-clear
          size="large"
        />
      </a-form-item>
      <a-form-item :label="$t('userCenter.securitySettings.updateEmail.form.label.captcha')" field="captcha">
        <a-input
          v-model="form.captcha"
          :placeholder="$t('userCenter.securitySettings.updateEmail.form.placeholder.captcha')"
          max-length="6"
          allow-clear
          size="large"
          style="width: 80%"
        />
        <a-button
          :loading="captchaLoading"
          type="primary"
          size="large"
          :disabled="captchaDisable"
          class="captcha-btn"
          @click="handleSendCaptcha"
        >
          {{ captchaBtnName }}
        </a-button>
      </a-form-item>
      <a-form-item :label="$t('userCenter.securitySettings.updateEmail.form.label.currentPassword')" field="currentPassword">
        <a-input-password
          v-model="form.currentPassword"
          :placeholder="$t('userCenter.securitySettings.updateEmail.form.placeholder.currentPassword')"
          max-length="32"
          allow-clear
          size="large"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, reactive, computed } from 'vue';
  import { FieldRule } from '@arco-design/web-vue';
  import { getMailCaptcha } from '@/api/common/captcha';
  import { updateEmail } from '@/api/system/user-center';
  import { useI18n } from 'vue-i18n';
  import { useLoginStore } from '@/store';
  import { encryptByRsa } from '@/utils/encrypt';

  const { proxy } = getCurrentInstance() as any;

  const { t } = useI18n();
  const loginStore = useLoginStore();
  const captchaTime = ref(60);
  const captchaTimer = ref();
  const captchaLoading = ref(false);
  const captchaDisable = ref(false);
  const visible = ref(false);
  const captchaBtnNameKey = ref('userCenter.securitySettings.updateEmail.form.sendCaptcha');
  const captchaBtnName = computed(() => t(captchaBtnNameKey.value));

  // 表单数据
  const form = reactive({
    newEmail: '',
    captcha: '',
    currentPassword: '',
  });
  // 表单验证规则
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
    };
  });

  /**
   * 重置验证码
   */
  const resetCaptcha = () => {
    window.clearInterval(captchaTimer.value);
    captchaTime.value = 60;
    captchaBtnNameKey.value = 'userCenter.securitySettings.updateEmail.form.sendCaptcha';
    captchaDisable.value = false;
  }

  /**
   * 发送验证码
   */
  const handleSendCaptcha = () => {
    if (captchaLoading.value) return;
    proxy.$refs.formRef.validateField('newEmail', (valid: any) => {
      if (!valid) {
        captchaLoading.value = true;
        captchaBtnNameKey.value = 'userCenter.securitySettings.updateEmail.form.loading.sendCaptcha';
        getMailCaptcha({
          email: form.newEmail
        }).then((res) => {
          captchaLoading.value = false;
          captchaDisable.value = true;
          captchaBtnNameKey.value = `${t('userCenter.securitySettings.updateEmail.form.reSendCaptcha')}(${captchaTime.value -= 1}s)`;
          captchaTimer.value = window.setInterval(function() {
            captchaTime.value -= 1;
            captchaBtnNameKey.value = `${t('userCenter.securitySettings.updateEmail.form.reSendCaptcha')}(${captchaTime.value}s)`;
            if (captchaTime.value < 0) {
              window.clearInterval(captchaTimer.value);
              captchaTime.value = 60;
              captchaBtnNameKey.value = t('userCenter.securitySettings.updateEmail.form.reSendCaptcha');
              captchaDisable.value = false;
            }
          }, 1000);
          proxy.$message.success(res.msg);
        }).catch(() => {
          resetCaptcha();
          captchaLoading.value = false;
        });
      }
    });
  };

  /**
   * 取消
   */
  const handleCancel = () => {
    visible.value = false;
    proxy.$refs.formRef.resetFields();
    resetCaptcha();
  };

  /**
   * 修改
   */
  const handleUpdate = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        updateEmail({
          newEmail: form.newEmail,
          captcha: form.captcha,
          currentPassword: encryptByRsa(form.currentPassword) || '',
        }).then((res) => {
          handleCancel();
          loginStore.getInfo();
          proxy.$message.success(res.msg);
        });
      }
    });
  };

  /**
   * 打开修改对话框
   */
  const toUpdate = () => {
    visible.value = true;
  };
</script>

<style scoped lang="less">
  .captcha-btn {
    margin-left: 5px;
  }
</style>
