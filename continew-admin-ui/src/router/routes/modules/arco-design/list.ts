import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const LIST: AppRouteRecordRaw = {
  path: '/list',
  name: 'list',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.list',
    requiresAuth: true,
    icon: 'list',
    order: 100,
  },
  children: [
    {
      path: 'search-table', // The midline path complies with SEO specifications
      name: 'SearchTable',
      component: () => import('@/views/arco-design/list/search-table/index.vue'),
      meta: {
        locale: 'menu.list.searchTable',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'card',
      name: 'Card',
      component: () => import('@/views/arco-design/list/card/index.vue'),
      meta: {
        locale: 'menu.list.cardList',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default LIST;
