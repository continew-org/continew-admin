import { createPinia } from 'pinia';
import useAppStore from './modules/app';
import useLoginStore from './modules/login';
import useDictStore from './modules/dict';
import useTabBarStore from './modules/tab-bar';

const pinia = createPinia();

export { useAppStore, useLoginStore, useDictStore, useTabBarStore };
export default pinia;
