import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const FORM: AppRouteRecordRaw = {
  path: '/form',
  name: 'form',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.form',
    icon: 'icon-bookmark',
    requiresAuth: true,
    order: 101,
  },
  children: [
    {
      path: 'step',
      name: 'Step',
      component: () => import('@/views/arco-design/form/step/index.vue'),
      meta: {
        locale: 'menu.form.step',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'group',
      name: 'Group',
      component: () => import('@/views/arco-design/form/group/index.vue'),
      meta: {
        locale: 'menu.form.group',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default FORM;
