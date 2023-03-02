import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const RESULT: AppRouteRecordRaw = {
  path: '/result',
  name: 'result',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.result',
    icon: 'icon-check-circle',
    requiresAuth: true,
    order: 103,
  },
  children: [
    {
      path: 'success',
      name: 'Success',
      component: () => import('@/views/arco-design/result/success/index.vue'),
      meta: {
        locale: 'menu.result.success',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'error',
      name: 'Error',
      component: () => import('@/views/arco-design/result/error/index.vue'),
      meta: {
        locale: 'menu.result.error',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default RESULT;
