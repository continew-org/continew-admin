<template>
  <div class="login-form-wrapper">
    <div class="login-form-title">登录 {{ appStore.getTitle }}</div>
    <a-form
      ref="formRef"
      :model="form"
      :rules="rules"
      layout="vertical"
      size="large"
      class="login-form"
      @submit="handleLogin"
    >
      <a-form-item field="username" hide-label>
        <a-input
          v-model="form.username"
          :placeholder="$t('login.form.placeholder.username')"
          :max-length="64"
        >
          <template #prefix><icon-user /></template>
        </a-input>
      </a-form-item>
      <a-form-item field="password" hide-label>
        <a-input-password
          v-model="form.password"
          :placeholder="$t('login.form.placeholder.password')"
          :max-length="32"
          allow-clear
        >
          <template #prefix><icon-lock /></template>
        </a-input-password>
      </a-form-item>
      <a-form-item class="login-form-captcha" field="captcha" hide-label>
        <a-input
          v-model="form.captcha"
          :placeholder="$t('login.form.placeholder.captcha')"
          :max-length="4"
          allow-clear
          style="width: 63%"
        >
          <template #prefix><icon-check-circle /></template>
        </a-input>
        <img
          :src="captchaImgBase64"
          :alt="$t('login.form.captcha')"
          @click="getCaptcha"
        />
      </a-form-item>
      <a-space :size="16" direction="vertical">
        <div class="login-form-remember-me">
          <a-checkbox
            checked="rememberMe"
            :model-value="loginConfig.rememberMe"
            @change="setRememberMe as any"
          >
            {{ $t('login.form.rememberMe') }}
          </a-checkbox>
        </div>
        <a-button :loading="loading" type="primary" long html-type="submit">
          {{ $t('login.form.login') }}
        </a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive, computed } from 'vue';
  import { FieldRule, ValidatedError } from '@arco-design/web-vue';
  import { LoginReq } from '@/api/auth/login';
  import { useI18n } from 'vue-i18n';
  import { useRouter } from 'vue-router';
  import { useStorage } from '@vueuse/core';
  import { useLoginStore, useAppStore } from '@/store';
  import { encryptByRsa } from '@/utils/encrypt';
  // import debug from '@/utils/env';

  const { proxy } = getCurrentInstance() as any;

  const captchaImgBase64 = ref('');
  const loginStore = useLoginStore();
  const appStore = useAppStore();
  const loading = ref(false);
  const { t } = useI18n();
  const router = useRouter();
  const loginConfig = useStorage('login-config', {
    rememberMe: true,
    username: 'admin', // 演示默认值
    password: 'admin123', // 演示默认值
    // username: debug ? 'admin' : '', // 演示默认值
    // password: debug ? 'admin123' : '', // 演示默认值
  });

  const data = reactive({
    // 表单数据
    form: {
      username: loginConfig.value.username,
      password: loginConfig.value.password,
      captcha: '',
      uuid: '',
    } as LoginReq,
    // 表单验证规则
    rules: computed((): Record<string, FieldRule[]> => {
      return {
        username: [
          { required: true, message: t('login.form.error.required.username') },
        ],
        password: [
          { required: true, message: t('login.form.error.required.password') },
        ],
        captcha: [
          { required: true, message: t('login.form.error.required.captcha') },
        ],
      };
    }),
  });
  const { form, rules } = toRefs(data);

  /**
   * 获取验证码
   */
  const getCaptcha = () => {
    loginStore.getImgCaptcha().then((res) => {
      form.value.uuid = res.data.uuid;
      captchaImgBase64.value = res.data.img;
    });
  };
  getCaptcha();

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
      loginStore
        .login({
          username: values.username,
          password: encryptByRsa(values.password) || '',
          captcha: values.captcha,
          uuid: values.uuid,
        })
        .then(() => {
          const { redirect, ...othersQuery } = router.currentRoute.value.query;
          router.push({
            name: (redirect as string) || 'Workplace',
            query: {
              ...othersQuery,
            },
          });
          const { rememberMe } = loginConfig.value;
          const { username } = values;
          loginConfig.value.username = rememberMe ? username : '';
          proxy.$notification.success(t('login.form.login.success'));
        })
        .catch(() => {
          getCaptcha();
          form.value.captcha = '';
        })
        .finally(() => {
          loading.value = false;
        });
    }
  };

  /**
   * 记住我
   *
   * @param value 是否记住我
   */
  const setRememberMe = (value: boolean) => {
    loginConfig.value.rememberMe = value;
  };
</script>

<style lang="less" scoped>
  .login-form {
    margin-top: 32px;
    &-wrapper {
      width: 320px;
    }
    &-title {
      color: var(--color-text-1);
      font-weight: 500;
      font-size: 24px;
      line-height: 32px;
    }
    &-sub-title {
      color: var(--color-text-3);
      font-size: 16px;
      line-height: 24px;
    }
    &-captcha img {
      width: 111px;
      height: 36px;
      margin: 0 0 0 10px;
      cursor: pointer;
    }
    &-remember-me {
      display: flex;
      justify-content: space-between;
    }
    &-register-btn {
      color: var(--color-text-3) !important;
    }
  }
</style>
