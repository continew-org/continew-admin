import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const PROFILE: AppRouteRecordRaw = {
  name: 'Profile',
  path: '/demo/profile',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.profile',
    requiresAuth: true,
    icon: 'file',
    order: 902,
  },
  children: [
    {
      name: 'Basic',
      path: 'basic',
      component: () => import('@/views/demo/profile/basic/index.vue'),
      meta: {
        locale: 'menu.profile.basic',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default PROFILE;
