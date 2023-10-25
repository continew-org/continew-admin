<template>
  <a-list-item-meta>
    <template #avatar>
      <a-typography-paragraph>
        {{ $t('userCenter.securitySettings.social.label') }}
      </a-typography-paragraph>
    </template>
    <template #description>
      <div class="tip">
        {{ $t('userCenter.securitySettings.social.tip') }}
      </div>
      <div class="content">
        <a-typography-paragraph>
          <span v-if="socialBinds.length > 0">
            {{ socialBinds.map((item) => item.description).join('、') }}
          </span>
          <span v-else class="tip">
            {{ $t('userCenter.securitySettings.social.content') }}
          </span>
        </a-typography-paragraph>
      </div>
      <div class="operation">
        <a-tooltip content="码云" mini>
          <a-link @click="handleBind('GITEE', '码云')">
            <svg
              v-if="giteeSocial"
              class="icon"
              style="fill: #c71d23"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M11.984 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12 12 12 0 0 0 12-12A12 12 0 0 0 12 0a12 12 0 0 0-.016 0zm6.09 5.333c.328 0 .593.266.592.593v1.482a.594.594 0 0 1-.593.592H9.777c-.982 0-1.778.796-1.778 1.778v5.63c0 .327.266.592.593.592h5.63c.982 0 1.778-.796 1.778-1.778v-.296a.593.593 0 0 0-.592-.593h-4.15a.592.592 0 0 1-.592-.592v-1.482a.593.593 0 0 1 .593-.592h6.815c.327 0 .593.265.593.592v3.408a4 4 0 0 1-4 4H5.926a.593.593 0 0 1-.593-.593V9.778a4.444 4.444 0 0 1 4.445-4.444h8.296Z"
              />
            </svg>
            <svg
              v-else
              class="icon GITEE"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M11.984 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12 12 12 0 0 0 12-12A12 12 0 0 0 12 0a12 12 0 0 0-.016 0zm6.09 5.333c.328 0 .593.266.592.593v1.482a.594.594 0 0 1-.593.592H9.777c-.982 0-1.778.796-1.778 1.778v5.63c0 .327.266.592.593.592h5.63c.982 0 1.778-.796 1.778-1.778v-.296a.593.593 0 0 0-.592-.593h-4.15a.592.592 0 0 1-.592-.592v-1.482a.593.593 0 0 1 .593-.592h6.815c.327 0 .593.265.593.592v3.408a4 4 0 0 1-4 4H5.926a.593.593 0 0 1-.593-.593V9.778a4.444 4.444 0 0 1 4.445-4.444h8.296Z"
              />
            </svg>
          </a-link>
        </a-tooltip>
        <a-tooltip content="GitHub" mini>
          <a-link @click="handleBind('GITHUB', 'GitHub')">
            <svg
              v-if="githubSocial"
              class="icon"
              style="fill: #181717"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"
              />
            </svg>
            <svg
              v-else
              class="icon GITHUB"
              viewBox="0 0 24 24"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"
              />
            </svg>
          </a-link>
        </a-tooltip>
      </div>
    </template>
  </a-list-item-meta>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import {
    UserSocialBindRecord,
    listSocial,
    unbindSocial,
  } from '@/api/system/user-center';
  import { socialAuth } from '@/api/auth';

  const { proxy } = getCurrentInstance() as any;
  const { t } = useI18n();
  const socialBinds = ref<UserSocialBindRecord[]>([]);
  const giteeSocial = ref<UserSocialBindRecord>();
  const githubSocial = ref<UserSocialBindRecord>();

  /**
   * 查询绑定的第三方账号
   */
  const list = () => {
    listSocial().then((res) => {
      socialBinds.value = res.data;
      giteeSocial.value = socialBinds.value.find(
        (item) => item.source === 'GITEE'
      );
      githubSocial.value = socialBinds.value.find(
        (item) => item.source === 'GITHUB'
      );
    });
  };
  list();

  /**
   * 绑定或解绑
   *
   * @param source 来源
   * @param sourceDescription 来源描述
   */
  const handleBind = (source: string, sourceDescription: string) => {
    const isBind = socialBinds.value.some((item) => item.source === source);
    if (isBind) {
      proxy.$modal.warning({
        title: `确认解除和${sourceDescription}平台的三方账号绑定吗?`,
        titleAlign: 'start',
        content: '解除绑定后，将无法使用该第三方账户登录到此账号',
        hideCancel: false,
        onOk: () => {
          unbindSocial(source).then((res) => {
            list();
            proxy.$message.success(res.msg);
          });
        },
      });
      return;
    }
    proxy.$modal.info({
      title: '提示',
      titleAlign: 'start',
      content: `确认和${sourceDescription}平台的三方账号绑定吗?`,
      hideCancel: false,
      onOk: () => {
        socialAuth(source).then((res) => {
          window.location.href = res.data;
        });
      },
    });
  };
</script>

<style scoped lang="less">
  :deep(.arco-link) {
    padding: 1px 2px;
  }

  .icon {
    width: 21px;
    height: 20px;
    fill: rgb(170, 170, 170);
  }

  .icon:hover.GITEE {
    fill: #c71d23;
  }

  .icon:hover.GITHUB {
    fill: #181717;
  }
</style>
