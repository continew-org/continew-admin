import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const EXCEPTION: AppRouteRecordRaw = {
  name: 'Exception',
  path: '/demo/exception',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.exception',
    requiresAuth: true,
    icon: 'exclamation-2',
    order: 904,
  },
  children: [
    {
      name: '403',
      path: '403',
      component: () => import('@/views/demo/exception/403/index.vue'),
      meta: {
        locale: 'menu.exception.403',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      name: '404',
      path: '404',
      component: () => import('@/views/demo/exception/404/index.vue'),
      meta: {
        locale: 'menu.exception.404',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      name: '500',
      path: '500',
      component: () => import('@/views/demo/exception/500/index.vue'),
      meta: {
        locale: 'menu.exception.500',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default EXCEPTION;
