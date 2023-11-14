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
import { listRoute } from '@/api/auth';
import { listOption } from '@/api/common';
import getFile from '@/utils/file';
import { AppState, Config } from './types';

const recursionMenu = (
  appMenu: RouteRecordNormalized[],
  list: Array<RouteRecordNormalized>,
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
  state: (): AppState => ({ ...defaultSettings, config: {} }),

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
        menuList,
      );
      return menuList;
    },
    getLogo(state: AppState): string | undefined {
      return state.config?.site_logo;
    },
    getFavicon(state: AppState): string | undefined {
      return state.config?.site_favicon;
    },
    getTitle(state: AppState): string | undefined {
      return state.config?.site_title;
    },
    getCopyright(state: AppState): string | undefined {
      return state.config?.site_copyright;
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

    /**
     * 初始化系统配置信息
     */
    init() {
      listOption({
        code: ['site_title', 'site_copyright', 'site_favicon', 'site_logo'],
      }).then((res) => {
        const resMap = new Map();
        res.data.forEach((item) => {
          resMap.set(item.label, item.value);
        });
        this.config = {
          site_title: resMap.get('site_title'),
          site_copyright: resMap.get('site_copyright'),
          site_logo: resMap.get('site_logo'),
          site_favicon: resMap.get('site_logo'),
        };
        document.title = resMap.get('site_title');
        document
          .querySelector('link[rel="shortcut icon"]')
          ?.setAttribute(
            'href',
            getFile(resMap.get('site_favicon')) ||
              'https://cnadmin.charles7c.top/favicon.ico',
          );
      });
    },

    /**
     * 保存系统配置
     *
     * @param config 系统配置
     */
    save(config: Config) {
      this.config = config;
      document.title = config.site_title || '';
      document
        .querySelector('link[rel="shortcut icon"]')
        ?.setAttribute(
          'href',
          getFile(config.site_favicon) ||
            'https://cnadmin.charles7c.top/favicon.ico',
        );
    },
  },
});

export default useAppStore;
