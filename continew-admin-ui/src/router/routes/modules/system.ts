import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const System: AppRouteRecordRaw = {
  path: '/system',
  name: 'system',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.system',
    icon: 'icon-settings',
    requiresAuth: true,
    order: 2,
  },
  children: [
    {
      path: '/system/role',
      name: 'Role',
      component: () => import('@/views/system/role/index.vue'),
      meta: {
        locale: 'menu.system.role.list',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: '/system/dept',
      name: 'Dept',
      component: () => import('@/views/system/dept/index.vue'),
      meta: {
        locale: 'menu.system.dept.list',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default System;
