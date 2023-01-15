import { defineStore } from 'pinia';
import {
  login as userLogin,
  logout as userLogout,
  getUserInfo,
  LoginReq,
} from '@/api/auth/login';
import { getImageCaptcha as getCaptcha } from '@/api/common/captcha';
import { setToken, clearToken } from '@/utils/auth';
import { removeRouteListener } from '@/utils/route-listener';
import { UserState } from './types';
import useAppStore from '../app';

const useLoginStore = defineStore('user', {
  state: (): UserState => ({
    userId: '',
    username: '',
    nickname: '',
    gender: 0,
    phone: undefined,
    email: '',
    avatar: undefined,
    notes: undefined,
    pwdResetTime: undefined,
    registrationDate: undefined,

    job: 'backend',
    jobName: '后端艺术家',
    organization: 'Backend',
    organizationName: '后端',
    location: 'beijing',
    locationName: '北京',
    introduction: '低调星人',
    personalWebsite: 'https://blog.charles7c.top',
    role: '',
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

    // 切换角色
    switchRoles() {
      return new Promise((resolve) => {
        this.role = this.role === 'user' ? 'admin' : 'user';
        resolve(this.role);
      });
    },
  },
});

export default useLoginStore;
