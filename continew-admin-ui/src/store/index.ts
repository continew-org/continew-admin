import { createPinia } from 'pinia';
import useAppStore from './modules/app';
import useUserStore from './modules/user';
import useDictStore from './modules/dict';
import useTabBarStore from './modules/tab-bar';

const pinia = createPinia();

export { useAppStore, useUserStore, useDictStore, useTabBarStore };
export default pinia;
