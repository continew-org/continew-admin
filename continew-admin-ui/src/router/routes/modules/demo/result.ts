import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const RESULT: AppRouteRecordRaw = {
  name: 'Result',
  path: '/demo/result',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.result',
    icon: 'check-circle',
    requiresAuth: true,
    order: 903,
  },
  children: [
    {
      name: 'Success',
      path: 'success',
      component: () => import('@/views/demo/result/success/index.vue'),
      meta: {
        locale: 'menu.result.success',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      name: 'Error',
      path: 'error',
      component: () => import('@/views/demo/result/error/index.vue'),
      meta: {
        locale: 'menu.result.error',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default RESULT;
