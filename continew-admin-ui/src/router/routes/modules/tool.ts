import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const Tool: AppRouteRecordRaw = {
  name: 'Tool',
  path: '/tool',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.tool',
    icon: 'tool',
    requiresAuth: true,
    order: 2,
  },
  children: [
    {
      name: 'Generator',
      path: '/tool/generator',
      component: () => import('@/views/tool/generator/index.vue'),
      meta: {
        locale: 'menu.tool.generator.list',
        requiresAuth: true,
      },
    },
  ],
};

export default Tool;
