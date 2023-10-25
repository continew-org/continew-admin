<template>
  <div class="root">
    <div class="header">
      <img
        :src="getFile(appStore.getLogo) ?? './logo.svg'"
        alt="logo"
        height="33"
      />
      <div class="logo-text">{{ appStore.getTitle }}</div>
    </div>
    <div class="container">
      <div class="left-banner"></div>
      <div class="login-card">
        <div class="title">
          {{ $t('login.welcome') }} {{ appStore.getTitle }}
        </div>
        <EmailLogin v-if="isEmailLogin" />
        <a-tabs v-else class="account-tab" default-active-key="1">
          <a-tab-pane key="1" :title="$t('login.account')">
            <AccountLogin />
          </a-tab-pane>
          <a-tab-pane key="2" :title="$t('login.phone')">
            <PhoneLogin />
          </a-tab-pane>
        </a-tabs>
        <div class="oauth">
          <a-divider class="text" orientation="center">{{
            $t('login.other')
          }}</a-divider>
          <div class="idps">
            <div v-if="!isEmailLogin" class="mail app" @click="toggleLoginMode">
              <icon-email /> {{ $t('login.email.txt') }}
            </div>
            <div v-else class="account app" @click="toggleLoginMode">
              <icon-user /> {{ $t('login.account.txt') }}
            </div>
            <a-tooltip content="码云" mini>
              <a-link class="app" @click="handleSocialAuth('gitee')">
                <svg
                  class="icon"
                  fill="#C71D23"
                  role="img"
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
              <a-link class="app" @click="handleSocialAuth('github')">
                <svg
                  class="icon"
                  role="img"
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
        </div>
      </div>
    </div>
    <div v-if="appStore.device === 'desktop'" class="footer">
      <div class="beian">
        <div class="below text" v-html="appStore.getCopyright"></div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { useAppStore } from '@/store';
  import getFile from '@/utils/file';
  import useResponsive from '@/hooks/responsive';
  import { socialAuth } from '@/api/auth';
  import AccountLogin from './components/account-login.vue';
  import PhoneLogin from './components/phone-login.vue';
  import EmailLogin from './components/email-login.vue';

  const { t } = useI18n();
  const appStore = useAppStore();
  useResponsive(true);
  const isEmailLogin = ref(false);

  /**
   * 第三方登录授权
   *
   * @param source 来源
   */
  const handleSocialAuth = async (source: string) => {
    const { data } = await socialAuth(source);
    window.location.href = data;
  };

  const toggleLoginMode = () => {
    isEmailLogin.value = !isEmailLogin.value;
  };
</script>

<style lang="less" scoped>
  .root {
    background-image: url(https://z1.ax1x.com/2023/09/27/pPbnTOK.jpg);
    background-repeat: no-repeat;
    background-size: cover;
    min-height: 100vh;

    a {
      color: #3370ff;
      cursor: pointer !important;
      text-decoration: none;
    }

    a:hover {
      color: #6694ff;
    }

    .header {
      padding: 32px 40px 0;
      img {
        vertical-align: middle;
      }
      .logo-text {
        display: inline-block;
        margin-right: 4px;
        margin-left: 4px;
        color: var(--color-text-1);
        font-size: 24px;
        vertical-align: middle;
      }
    }

    .container {
      align-items: center;
      box-sizing: border-box;
      display: flex;
      height: calc(100vh - 100px);
      justify-content: center;
      margin: 0 auto;
      max-width: 1200px;
      min-height: 650px;
      .left-banner {
        flex: 1 1;
        height: 100%;
        max-height: 700px;
        position: relative;
        img {
          height: 100%;
          left: 0;
          max-height: 350px;
          max-width: 500px;
          object-fit: contain;
          position: absolute;
          top: 4.5%;
          width: 100%;
        }
      }
      .login-card {
        display: flex;
        background: #fff;
        border-radius: 20px;
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
        box-sizing: border-box;
        min-height: 500px;
        position: relative;
        width: 450px;
        flex-direction: column;
        margin-bottom: 53px;
        padding: 48px 43px 32px;
        .title {
          color: #020814;
          font-size: 24px;
          font-weight: 500;
          letter-spacing: 0.003em;
          line-height: 32px;
          padding: 0 5px;
        }
        .account-tab {
          margin-top: 36px;
          :deep(.arco-tabs-nav::before) {
            display: none;
          }
          :deep(.arco-tabs-tab-title) {
            font-size: 16px;
            font-weight: 500;
            line-height: 22px;
            display: inline-block;
            padding: 1px 0;
            position: relative;
          }
          :deep(.arco-tabs-tab-title:hover) {
            color: rgb(var(--primary-6));
          }
          :deep(.arco-tabs-tab-title:before) {
            display: none;
          }
          :deep(.arco-tabs-tab) {
            margin: 0 30px 0 6px;
          }
        }

        .oauth {
          margin-top: 20px;
          padding: 0 5px;
          :deep(.arco-divider-text) {
            color: #80838a;
            font-size: 12px;
            font-weight: 400;
            line-height: 20px;
          }
          :deep(.arco-divider) {
            margin-bottom: 25px;
          }
          .idps {
            align-items: center;
            display: flex;
            justify-content: center;
            width: 100%;
            .app {
              margin-right: 12px;
              align-items: center;
              border: 1px solid #eaedf1;
              border-radius: 32px;
              box-sizing: border-box;
              display: flex;
              height: 32px;
              justify-content: center;
              width: 32px;
              cursor: pointer;
              .icon {
                width: 21px;
                height: 20px;
              }
            }
            .app:hover {
              background: #f3f7ff;
              border: 1px solid #97bcff;
            }
            .mail {
              min-width: 81px;
              width: 81px;
            }
            .account {
              min-width: 147px;
              width: 147px;
            }
            .mail,
            .account {
              color: #41464f;
              font-size: 12px;
              font-weight: 400;
              line-height: 20px;
              padding: 6px 10px;
            }
            .mail svg,
            .account svg {
              font-size: 16px;
              margin-right: 10px;
            }
            .mail:hover,
            .account:hover {
              color: rgb(var(--primary-6));
            }
            .mail svg:hover,
            .account svg:hover {
              color: rgb(var(--primary-6));
            }
          }
        }
      }
    }

    .footer {
      align-items: center;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      .beian {
        .text {
          color: #41464f;
          font-size: 12px;
          font-weight: 400;
          letter-spacing: 0.2px;
          line-height: 20px;
          text-align: center;
        }
        .below {
          align-items: center;
          display: flex;
        }
      }
    }
  }
</style>
