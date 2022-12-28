import { createPinia } from 'pinia';
import useAppStore from './modules/app';
import useLoginStore from './modules/login';
import useTabBarStore from './modules/tab-bar';

const pinia = createPinia();

export { useAppStore, useLoginStore, useTabBarStore };
export default pinia;
