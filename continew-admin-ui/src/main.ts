import { createApp } from 'vue';
import ArcoVue from '@arco-design/web-vue';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
// eslint-disable-next-line import/no-unresolved
import 'virtual:svg-icons-register';
import globalComponents from '@/components';
import useDict from '@/utils/dict';
import router from './router';
import store from './store';
import i18n from './locale';
import directive from './directive';
import './mock';
import App from './App.vue';
import '@arco-design/web-vue/dist/arco.css';
import '@/assets/style/global.less';
import '@/utils/request';

const app = createApp(App);

// 全局方法挂载
app.config.globalProperties.useDict = useDict;

app.use(ArcoVue, {});
app.use(ArcoVueIcon);

app.use(router);
app.use(store);
app.use(i18n);
app.use(globalComponents);
app.use(directive);

app.mount('#app');
