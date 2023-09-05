import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const Monitor: AppRouteRecordRaw = {
  name: 'Monitor',
  path: '/monitor',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.monitor',
    icon: 'computer',
    requiresAuth: true,
    order: 3,
  },
  children: [
    {
      name: 'OnlineUser',
      path: '/monitor/online',
      component: () => import('@/views/monitor/online/index.vue'),
      meta: {
        locale: 'menu.online.user.list',
        requiresAuth: true,
      },
    },
    {
      name: 'LoginLog',
      path: '/monitor/log/login',
      component: () => import('@/views/monitor/log/login/index.vue'),
      meta: {
        locale: 'menu.log.login.list',
        requiresAuth: true,
      },
    },
    {
      path: '/monitor/log/operation',
      name: 'OperationLog',
      component: () => import('@/views/monitor/log/operation/index.vue'),
      meta: {
        locale: 'menu.log.operation.list',
        requiresAuth: true,
      },
    },
    {
      name: 'SystemLog',
      path: '/monitor/log/system',
      component: () => import('@/views/monitor/log/system/index.vue'),
      meta: {
        locale: 'menu.log.system.list',
        requiresAuth: true,
      },
    },
  ],
};

export default Monitor;
