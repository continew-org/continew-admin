import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const UserCenter: AppRouteRecordRaw = {
  path: '/login/user',
  name: 'user',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.user',
    icon: 'icon-user',
    requiresAuth: true,
  },
  children: [
    {
      path: 'center',
      name: 'UserCenter',
      component: () => import('@/views/system/user/center/index.vue'),
      meta: {
        locale: 'menu.user.center',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
  ],
};

export default UserCenter;
