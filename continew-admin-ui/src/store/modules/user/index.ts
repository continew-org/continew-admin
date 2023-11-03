import { defineStore } from 'pinia';
import {
  AccountLoginReq,
  EmailLoginReq,
  PhoneLoginReq,
  accountLogin as userAccountLogin,
  emailLogin as userEmailLogin,
  phoneLogin as userPhoneLogin,
  socialLogin as userSocialLogin,
  logout as userLogout,
  getUserInfo,
} from '@/api/auth';
import { getImageCaptcha as getCaptcha } from '@/api/common/captcha';
import { setToken, clearToken, clearTimer } from '@/utils/auth';
import { removeRouteListener } from '@/utils/route-listener';
import { UserState } from './types';
import useAppStore from '../app';

const useUserStore = defineStore('user', {
  state: (): UserState => ({
    id: '',
    username: '',
    nickname: '',
    gender: 0,
    phone: undefined,
    email: '',
    avatar: undefined,
    description: undefined,
    pwdResetTime: undefined,
    registrationDate: undefined,
    deptId: '',
    deptName: '',
    permissions: [],
    roles: [],
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
  },

  actions: {
    // 获取图片验证码
    getImgCaptcha() {
      return getCaptcha();
    },

    // 账号登录
    async accountLogin(req: AccountLoginReq) {
      try {
        const res = await userAccountLogin(req);
        setToken(res.data.token);
      } catch (err) {
        clearToken();
        throw err;
      }
    },

    // 邮箱登录
    async emailLogin(req: EmailLoginReq) {
      try {
        const res = await userEmailLogin(req);
        setToken(res.data.token);
      } catch (err) {
        clearToken();
        throw err;
      }
    },

    // 手机号登录
    async phoneLogin(req: PhoneLoginReq) {
      try {
        const res = await userPhoneLogin(req);
        setToken(res.data.token);
      } catch (err) {
        clearToken();
        throw err;
      }
    },

    // 三方账号身份登录
    async socialLogin(source: string, req: any) {
      try {
        const res = await userSocialLogin(source, req);
        setToken(res.data.token);
      } catch (err) {
        clearToken();
        throw err;
      }
    },

    // 用户退出
    async logout() {
      try {
        await userLogout();
      } finally {
        this.logoutCallBack();
      }
    },
    logoutCallBack() {
      const appStore = useAppStore();
      this.resetInfo();
      clearToken();
      clearTimer();
      removeRouteListener();
      appStore.clearServerMenu();
    },

    // 获取用户信息
    async getInfo() {
      const res = await getUserInfo();
      this.setInfo(res.data);
    },
    // 设置用户信息
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial);
    },
    // 重置用户信息
    resetInfo() {
      this.$reset();
    },
  },
});

export default useUserStore;
