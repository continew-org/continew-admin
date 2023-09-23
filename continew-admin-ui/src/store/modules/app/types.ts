import type { RouteRecordNormalized } from 'vue-router';

export interface Config {
  site_title?: string;
  site_copyright?: string;
  site_logo?: string;
  site_favicon?: string;
}
export interface AppState {
  theme: string;
  colorWeak: boolean;
  navbar: boolean;
  menu: boolean;
  topMenu: boolean;
  menuAccordion: boolean;
  hideMenu: boolean;
  menuCollapse: boolean;
  footer: boolean;
  themeColor: string;
  menuWidth: number;
  globalSettings: boolean;
  device: string;
  tabBar: boolean;
  menuFromServer: boolean;
  serverMenu: RouteRecordNormalized[];
  [key: string]: unknown;
  config: Config;
}
