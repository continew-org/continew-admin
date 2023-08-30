import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const EXCEPTION: AppRouteRecordRaw = {
  path: '/exception',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.exception',
    requiresAuth: true,
    icon: 'exclamation-circle',
    order: 904,
  },
  children: [
    {
      name: '403',
      path: '403',
      component: () => import('@/views/arco-design/exception/403/index.vue'),
      meta: {
        locale: 'menu.exception.403',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      name: '404',
      path: '404',
      component: () => import('@/views/arco-design/exception/404/index.vue'),
      meta: {
        locale: 'menu.exception.404',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      name: '500',
      path: '500',
      component: () => import('@/views/arco-design/exception/500/index.vue'),
      meta: {
        locale: 'menu.exception.500',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default EXCEPTION;
