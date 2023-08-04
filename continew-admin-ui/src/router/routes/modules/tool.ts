import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const Tool: AppRouteRecordRaw = {
  path: '/tool',
  name: 'tool',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.tool',
    icon: 'tool',
    requiresAuth: true,
    order: 2,
  },
  children: [
    {
      path: '/tool/generator',
      name: 'Generator',
      component: () => import('@/views/tool/generator/index.vue'),
      meta: {
        locale: 'menu.tool.generator.list',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default Tool;
