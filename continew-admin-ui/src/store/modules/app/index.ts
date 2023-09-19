import { defineStore } from 'pinia';
import { h } from 'vue';
import { Message } from '@arco-design/web-vue';
import {
  IconCheckCircleFill,
  IconCloseCircleFill,
} from '@arco-design/web-vue/es/icon';
import type { MessageReturn } from '@arco-design/web-vue/es/message/interface';
import type { RouteRecordNormalized } from 'vue-router';
import defaultSettings from '@/config/settings.json';
import { listRoute } from '@/api/auth/login';
import { AppState } from './types';

const recursionMenu = (
  appMenu: RouteRecordNormalized[],
  list: Array<RouteRecordNormalized>
) => {
  appMenu.forEach((item) => {
    const childrenAppMenu = item.children as RouteRecordNormalized[];
    if (childrenAppMenu != null && childrenAppMenu.length > 0) {
      recursionMenu(childrenAppMenu, list);
    } else {
      list.push(item);
    }
  });
};
const useAppStore = defineStore('app', {
  state: (): AppState => ({ ...defaultSettings }),

  getters: {
    appCurrentSetting(state: AppState): AppState {
      return { ...state };
    },
    appDevice(state: AppState) {
      return state.device;
    },
    appAsyncMenus(state: AppState): RouteRecordNormalized[] {
      return state.serverMenu as unknown as RouteRecordNormalized[];
    },
    appAsyncMenusAll(state: AppState): RouteRecordNormalized[] {
      const menuList: RouteRecordNormalized[] = [];
      recursionMenu(
        state.serverMenu as unknown as RouteRecordNormalized[],
        menuList
      );
      return menuList;
    },
  },

  actions: {
    // Update app settings
    updateSettings(partial: Partial<AppState>) {
      // @ts-ignore-next-line
      this.$patch(partial);
    },

    // Change theme color
    toggleTheme(dark: boolean) {
      if (dark) {
        this.theme = 'dark';
        document.body.setAttribute('arco-theme', 'dark');
      } else {
        this.theme = 'light';
        document.body.removeAttribute('arco-theme');
      }
    },
    toggleDevice(device: string) {
      this.device = device;
    },
    toggleMenu(value: boolean) {
      this.hideMenu = value;
    },
    async fetchServerMenuConfig() {
      let messageInstance: MessageReturn | null = null;
      try {
        messageInstance = Message.loading({
          id: 'menuNotice', // Keep the instance id the same
          content: '菜单加载中',
        });
        const { data } = await listRoute();
        this.serverMenu = data;
        messageInstance = Message.success({
          id: 'menuNotice',
          content: '菜单加载成功',
          duration: 1000,
          icon: () => h(IconCheckCircleFill),
        });
      } catch (error) {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        messageInstance = Message.error({
          id: 'menuNotice',
          content: '菜单加载失败',
          icon: () => h(IconCloseCircleFill),
        });
      }
    },
    clearServerMenu() {
      this.serverMenu = [];
    },
  },
});

export default useAppStore;
