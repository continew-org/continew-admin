<template>
  <a-spin :loading="loading" :tip="isLogin() ? $t('bind.ing') : $t('login.ing')">
    <div></div>
  </a-spin>
</template>

<script setup lang="ts">
  import { getCurrentInstance, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useUserStore } from '@/store';
  import { useI18n } from 'vue-i18n';
  import { isLogin } from '@/utils/auth';
  import { bindSocial } from '@/api/system/user-center';

  const { proxy } = getCurrentInstance() as any;
  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const userStore = useUserStore();
  const loading = ref(false);
  const source = route.query.source as string;

  /**
   * 社会化身份登录
   */
  const handleSocialLogin = () => {
    if (loading.value) return;
    loading.value = true;
    const { redirect, ...othersQuery } = router.currentRoute.value.query;
    userStore
      .socialLogin(source, othersQuery)
      .then(() => {
        router.push({
          name: (redirect as string) || 'Workplace',
        });
        proxy.$notification.success(t('login.success'));
      })
      .catch(() => {
        router.push({
          name: 'login',
          query: {
            ...othersQuery,
          },
        });
      })
      .finally(() => {
        loading.value = false;
      });
  };

  /**
   * 绑定第三方账号
   */
  const handleBindSocial = () => {
    if (loading.value) return;
    loading.value = true;
    const { redirect, ...othersQuery } = router.currentRoute.value.query;
    bindSocial(source, othersQuery)
      .then((res) => {
        router.push({
          name: 'UserCenter',
          query: {
            tab: 'security-setting',
          },
        });
        proxy.$message.success(res.msg);
      })
      .catch(() => {
        router.push({
          name: 'UserCenter',
          query: {
            tab: 'security-setting',
          },
        });
      })
      .finally(() => {
        loading.value = false;
      });
  };

  if (isLogin()) {
    handleBindSocial();
  } else {
    handleSocialLogin();
  }
</script>

<script lang="ts">
  export default {
    name: 'SocialCallback',
  };
</script>

<style scoped lang="less">
  div {
    width: 150px;
    height: 150px;
    position: absolute;
    left: 50%;
    top: 45%;
    margin-left: -50px;
    margin-top: -50px;
  }
</style>
