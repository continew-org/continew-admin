import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const Monitor: AppRouteRecordRaw = {
  path: '/monitor',
  name: 'monitor',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.monitor',
    icon: 'computer',
    requiresAuth: true,
    order: 2,
  },
  children: [
    {
      path: '/monitor/online',
      name: 'OnlineUser',
      component: () => import('@/views/monitor/online/index.vue'),
      meta: {
        locale: 'menu.online.user.list',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: '/monitor/log/login',
      name: 'LoginLog',
      component: () => import('@/views/monitor/log/login/index.vue'),
      meta: {
        locale: 'menu.log.login.list',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: '/monitor/log/operation',
      name: 'OperationLog',
      component: () => import('@/views/monitor/log/operation/index.vue'),
      meta: {
        locale: 'menu.log.operation.list',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: '/monitor/log/system',
      name: 'SystemLog',
      component: () => import('@/views/monitor/log/system/index.vue'),
      meta: {
        locale: 'menu.log.system.list',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default Monitor;
