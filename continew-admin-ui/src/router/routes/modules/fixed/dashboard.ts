import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const DASHBOARD: AppRouteRecordRaw = {
  path: '/dashboard',
  name: 'Dashboard',
  component: DEFAULT_LAYOUT,
  redirect: '/dashboard/workplace',
  meta: {
    locale: 'menu.dashboard',
    requiresAuth: true,
    icon: 'dashboard',
    order: 0,
    hideChildrenInMenu: true,
  },
  children: [
    {
      name: 'Workplace',
      path: 'workplace',
      component: () => import('@/views/dashboard/workplace/index.vue'),
      meta: {
        locale: 'menu.dashboard.workplace',
        requiresAuth: true,
        roles: ['*'],
        activeMenu: 'Dashboard',
      },
    },
  ],
};

export default DASHBOARD;
