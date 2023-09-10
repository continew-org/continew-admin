import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const LIST: AppRouteRecordRaw = {
  name: 'List',
  path: '/demo/list',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.list',
    requiresAuth: true,
    icon: 'list',
    order: 900,
  },
  children: [
    {
      name: 'SearchTable',
      path: 'search-table', // The midline path complies with SEO specifications
      component: () => import('@/views/demo/list/search-table/index.vue'),
      meta: {
        locale: 'menu.list.searchTable',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      name: 'Card',
      path: 'card',
      component: () => import('@/views/demo/list/card/index.vue'),
      meta: {
        locale: 'menu.list.cardList',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default LIST;
