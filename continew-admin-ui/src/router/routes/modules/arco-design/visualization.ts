import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const VISUALIZATION: AppRouteRecordRaw = {
  path: '/visualization',
  name: 'visualization',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.visualization',
    requiresAuth: true,
    icon: 'icon-bar-chart',
    order: 105,
  },
  children: [
    {
      path: 'data-analysis',
      name: 'DataAnalysis',
      component: () => import('@/views/arco-design/visualization/data-analysis/index.vue'),
      meta: {
        locale: 'menu.visualization.dataAnalysis',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: 'multi-dimension-data-analysis',
      name: 'MultiDimensionDataAnalysis',
      component: () =>
        import('@/views/arco-design/visualization/multi-dimension-data-analysis/index.vue'),
      meta: {
        locale: 'menu.visualization.multiDimensionDataAnalysis',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: 'monitor',
      name: 'Monitor',
      component: () => import('@/views/arco-design/visualization/monitor/index.vue'),
      meta: {
        locale: 'menu.dashboard.monitor',
        requiresAuth: true,
        roles: ['admin'],
      },
    },
  ],
};

export default VISUALIZATION;
