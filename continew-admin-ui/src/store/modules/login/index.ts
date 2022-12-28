import { defineStore } from 'pinia';
import {
  getImageCaptcha as getCaptcha,
  login as userLogin,
  logout as userLogout,
  getUserInfo,
  LoginReq,
} from '@/api/auth/login';
import { setToken, clearToken } from '@/utils/auth';
import { removeRouteListener } from '@/utils/route-listener';
import { UserState } from './types';
import useAppStore from '../app';

const useLoginStore = defineStore('user', {
  state: (): UserState => ({
    nickname: undefined,
    avatar: undefined,
    email: undefined,
    phone: undefined,
    job: undefined,
    jobName: undefined,
    organization: undefined,
    organizationName: undefined,
    location: undefined,
    locationName: undefined,
    introduction: undefined,
    personalWebsite: undefined,
    registrationDate: undefined,
    accountId: undefined,
    certification: undefined,
    role: '',
  }),

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state };
    },
  },

  actions: {
    switchRoles() {
      return new Promise((resolve) => {
        this.role = this.role === 'user' ? 'admin' : 'user';
        resolve(this.role);
      });
    },
    // Set user's information
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial);
    },

    // Reset user's information
    resetInfo() {
      this.$reset();
    },

    // Get user's information
    async info() {
      const res = await getUserInfo();

      this.setInfo(res.data);
    },

    // 获取图片验证码
    getImgCaptcha() {
      return getCaptcha();
    },

    // 用户登录
    async login(req: LoginReq) {
      try {
        const res = await userLogin(req);
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
      removeRouteListener();
      appStore.clearServerMenu();
    },
  },
});

export default useLoginStore;
