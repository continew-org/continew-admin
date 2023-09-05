import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const System: AppRouteRecordRaw = {
  name: 'System',
  path: '/system',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.system',
    icon: 'settings',
    requiresAuth: true,
    order: 1,
  },
  children: [
    {
      name: 'User',
      path: '/system/user',
      component: () => import('@/views/system/user/index.vue'),
      meta: {
        locale: 'menu.system.user.list',
        requiresAuth: true,
      },
    },
    {
      name: 'Role',
      path: '/system/role',
      component: () => import('@/views/system/role/index.vue'),
      meta: {
        locale: 'menu.system.role.list',
        requiresAuth: true,
      },
    },
    {
      name: 'Menu',
      path: '/system/menu',
      component: () => import('@/views/system/menu/index.vue'),
      meta: {
        locale: 'menu.system.menu.list',
        requiresAuth: true,
      },
    },
    {
      name: 'Dept',
      path: '/system/dept',
      component: () => import('@/views/system/dept/index.vue'),
      meta: {
        locale: 'menu.system.dept.list',
        requiresAuth: true,
      },
    },
    {
      name: 'Announcement',
      path: '/system/announcement',
      component: () => import('@/views/system/announcement/index.vue'),
      meta: {
        locale: 'menu.system.announcement.list',
        requiresAuth: true,
      },
    },
  ],
};

export default System;
