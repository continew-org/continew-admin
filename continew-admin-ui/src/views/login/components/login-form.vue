<template>
  <div class="login-form-wrapper">
    <div class="login-form-title">{{ $t('login.form.title') }}</div>
    <div class="login-form-sub-title">{{ $t('login.form.subTitle') }}</div>
    <div class="login-form-error-msg">{{ errorMessage }}</div>
    <a-form
      ref="loginFormRef"
      :model="loginForm"
      class="login-form"
      layout="vertical"
      @submit="handleLogin"
    >
      <a-form-item
        field="username"
        :rules="[{ required: true, message: $t('login.form.username.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input
          v-model="loginForm.username"
          :placeholder="$t('login.form.username.placeholder')"
          size="large"
          max-length="50"
        >
          <template #prefix>
            <icon-user />
          </template>
        </a-input>
      </a-form-item>
      <a-form-item
        field="password"
        :rules="[{ required: true, message: $t('login.form.password.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input-password
          v-model="loginForm.password"
          :placeholder="$t('login.form.password.placeholder')"
          size="large"
          allow-clear
          max-length="50"
        >
          <template #prefix>
            <icon-lock />
          </template>
        </a-input-password>
      </a-form-item>
      <a-form-item
        class="login-form-captcha"
        field="captcha"
        :rules="[{ required: true, message: $t('login.form.captcha.errMsg') }]"
        :validate-trigger="['change', 'blur']"
        hide-label
      >
        <a-input
          v-model="loginForm.captcha"
          :placeholder="$t('login.form.captcha.placeholder')"
          size="large"
          style="width: 63%"
          allow-clear
        >
          <template #prefix>
            <icon-check-circle />
          </template>
        </a-input>
        <img :src="captchaImgBase64" @click="getCaptcha" :alt="$t('login.form.captcha.placeholder')">
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
        <a-button type="primary" size="large" html-type="submit" long :loading="loading">
          {{ $t('login.form.login') }}
        </a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { ValidatedError } from '@arco-design/web-vue/es/form/interface';
  import { useI18n } from 'vue-i18n';
  // import debug from '@/utils/env';
  import { encryptByRsa } from "@/utils/encrypt";
  import { useStorage } from '@vueuse/core';
  import { useLoginStore } from '@/store';
  import useLoading from '@/hooks/loading';

  const router = useRouter();
  const { t } = useI18n();
  const errorMessage = ref('');
  const captchaImgBase64 = ref('');
  const { loading, setLoading } = useLoading();
  const loginStore = useLoginStore();

  const loginConfig = useStorage('login-config', {
    rememberMe: true,
    username: 'admin', // 演示默认值
    password: '123456', // 演示默认值
    // username: !debug ? '' : 'admin', // 演示默认值
    // password: !debug ? '' : '123456', // 演示默认值
  });

  const loginForm = reactive({
    username: loginConfig.value.username,
    password: loginConfig.value.password,
    captcha: '',
    uuid: '',
  });

  // 获取验证码
  const getCaptcha = async () => {
    const { data } = await loginStore.getImgCaptcha()
    loginForm.uuid = data.uuid
    captchaImgBase64.value = data.img
  }
  onMounted(() => {
    getCaptcha()
  })

  // 记住我
  const setRememberMe = (value: boolean) => {
    loginConfig.value.rememberMe = value;
  };

  // 登录处理
  const handleLogin = async ({
    errors,
    values,
  }: {
    errors: Record<string, ValidatedError> | undefined;
    values: Record<string, any>;
  }) => {
    if (loading.value) return;
    if (!errors) {
      setLoading(true);
      try {
        await loginStore.login({
          username: values.username,
          password: encryptByRsa(values.password) || '',
          captcha: values.captcha,
          uuid: values.uuid
        });
        const { redirect, ...othersQuery } = router.currentRoute.value.query;
        router.push({
          name: (redirect as string) || 'Workplace',
          query: {
            ...othersQuery,
          },
        });
        Message.success(t('login.form.login.success'));
        const { rememberMe } = loginConfig.value;
        const { username } = values;
        loginConfig.value.username = rememberMe ? username : '';
      } catch (err) {
        // errorMessage.value = (err as Error).message;
        await getCaptcha();
      } finally {
        setLoading(false);
      }
    }
  };
</script>

<style lang="less" scoped>
  .login-form {
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

    &-error-msg {
      height: 32px;
      color: rgb(var(--red-6));
      line-height: 32px;
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
