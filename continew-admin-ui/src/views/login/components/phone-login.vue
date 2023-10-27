<template>
  <a-form
    ref="formRef"
    :model="form"
    :rules="rules"
    layout="vertical"
    size="large"
    class="login-form"
    @submit="handleLogin"
  >
    <a-form-item field="phone" hide-label>
      <a-select :options="['+86']" style="flex: 1 1" default-value="+86" />
      <a-input
        v-model="form.phone"
        :placeholder="$t('login.phone.placeholder.phone')"
        :max-length="11"
        allow-clear
      />
    </a-form-item>
    <a-form-item field="captcha" hide-label>
      <a-input
        v-model="form.captcha"
        :placeholder="$t('login.phone.placeholder.captcha')"
        :max-length="4"
        allow-clear
        style="flex: 1 1"
      />
      <a-button
        class="captcha-btn"
        :loading="captchaLoading"
        :disabled="captchaDisable"
        @click="handleSendCaptcha"
      >
        {{ captchaBtnName }}
      </a-button>
    </a-form-item>
    <a-button
      class="btn"
      :loading="loading"
      type="primary"
      html-type="submit"
      :disabled="captchaDisable"
      >{{ $t('login.button') }}（演示不开放）
    </a-button>
  </a-form>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive, computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useRouter } from 'vue-router';
  import { ValidatedError } from '@arco-design/web-vue';
  import { useUserStore } from '@/store';
  import { PhoneLoginReq } from '@/api/auth';
  import { getSmsCaptcha } from '@/api/common/captcha';

  const { proxy } = getCurrentInstance() as any;
  const { t } = useI18n();
  const router = useRouter();
  const userStore = useUserStore();
  const loading = ref(false);
  const captchaLoading = ref(false);
  const captchaDisable = ref(true);
  const captchaTime = ref(60);
  const captchaTimer = ref();
  const captchaBtnNameKey = ref('login.captcha.get');
  const captchaBtnName = computed(() => t(captchaBtnNameKey.value));
  const data = reactive({
    form: {
      phone: '',
      captcha: '',
    } as PhoneLoginReq,
    rules: {
      phone: [
        { required: true, message: t('login.phone.error.required.phone') },
        {
          match: /^1[3-9]\d{9}$/,
          message: t('login.phone.error.match.phone'),
        },
      ],
      captcha: [
        { required: true, message: t('login.phone.error.required.captcha') },
      ],
    },
  });
  const { form, rules } = toRefs(data);

  /**
   * 重置验证码
   */
  const resetCaptcha = () => {
    window.clearInterval(captchaTimer.value);
    captchaTime.value = 60;
    captchaBtnNameKey.value = 'login.captcha.get';
    captchaDisable.value = false;
  };

  /**
   * 发送验证码
   */
  const handleSendCaptcha = () => {
    if (captchaLoading.value) return;
    proxy.$refs.formRef.validateField('phone', (valid: any) => {
      if (!valid) {
        captchaLoading.value = true;
        captchaBtnNameKey.value = 'login.captcha.ing';
        getSmsCaptcha(form.value.phone)
          .then((res) => {
            captchaLoading.value = false;
            captchaDisable.value = true;
            captchaBtnNameKey.value = `${t(
              'login.captcha.get'
            )}(${(captchaTime.value -= 1)}s)`;
            captchaTimer.value = window.setInterval(() => {
              captchaTime.value -= 1;
              captchaBtnNameKey.value = `${t('login.captcha.get')}(${
                captchaTime.value
              }s)`;
              if (captchaTime.value <= 0) {
                resetCaptcha();
              }
            }, 1000);
            proxy.$message.success(res.msg);
          })
          .catch(() => {
            resetCaptcha();
            captchaLoading.value = false;
          });
      }
    });
  };

  /**
   * 登录
   *
   * @param errors 表单验证错误
   * @param values 表单数据
   */
  const handleLogin = ({
    errors,
    values,
  }: {
    errors: Record<string, ValidatedError> | undefined;
    values: Record<string, any>;
  }) => {
    if (loading.value) return;
    if (!errors) {
      loading.value = true;
      userStore
        .phoneLogin({
          phone: values.phone,
          captcha: values.captcha,
        })
        .then(() => {
          const { redirect, ...othersQuery } = router.currentRoute.value.query;
          router.push({
            name: (redirect as string) || 'Workplace',
            query: {
              ...othersQuery,
            },
          });
          proxy.$notification.success(t('login.success'));
        })
        .catch(() => {
          form.value.captcha = '';
        })
        .finally(() => {
          loading.value = false;
        });
    }
  };
</script>

<style lang="less" scoped>
  .login-form {
    box-sizing: border-box;
    padding: 0 5px;
    margin-top: 16px;
    .arco-input-wrapper,
    :deep(.arco-select-view-single) {
      background-color: var(--color-bg-white);
      border: 1px solid var(--color-border-3);
      height: 40px;
      border-radius: 4px;
      font-size: 13px;
    }
    .arco-input-wrapper.arco-input-error {
      background-color: var(--color-danger-light-1);
      border-color: var(--color-danger-light-4);
    }

    .captcha-btn {
      height: 40px;
      margin-left: 12px;
      min-width: 98px;
      border-radius: 4px;
    }

    .arco-btn-secondary:not(.arco-btn-disabled) {
      background-color: #f6f8fa;
      border: 1px solid #dde2e9;
      color: #41464f;
    }
    .arco-btn-secondary:not(.arco-btn-disabled):hover {
      background-color: transparent;
      border: 1px solid rgb(var(--primary-6));
    }

    .btn {
      border-radius: 4px;
      box-shadow: 0 0 0 1px #05f, 0 2px 1px rgba(0, 0, 0, 0.15);
      font-size: 14px;
      font-weight: 500;
      height: 40px;
      line-height: 22px;
      margin: 20px 0 12px;
      width: 100%;
    }
  }
</style>
