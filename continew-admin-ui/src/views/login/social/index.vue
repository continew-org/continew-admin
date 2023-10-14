<template>
  <a-spin :loading="loading" :tip="$t('login.ing')">
    <div></div>
  </a-spin>
</template>

<script setup lang="ts">
  import { getCurrentInstance, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useLoginStore } from '@/store';
  import { useI18n } from 'vue-i18n';

  const { proxy } = getCurrentInstance() as any;
  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();
  const loginStore = useLoginStore();
  const loading = ref(false);
  const source = route.query.source as string;

  /**
   * 社会化身份登录
   */
  const handleSocialLogin = () => {
    if (loading.value) return;
    loading.value = true;
    const { redirect, ...othersQuery } = router.currentRoute.value.query;
    loginStore
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
  handleSocialLogin();
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
