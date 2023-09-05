import { DEFAULT_LAYOUT } from '../../base';
import { AppRouteRecordRaw } from '../../types';

const VISUALIZATION: AppRouteRecordRaw = {
  name: 'Visualization',
  path: '/demo/visualization',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.visualization',
    requiresAuth: true,
    icon: 'bar-chart',
    order: 905,
  },
  children: [
    {
      name: 'DataAnalysis',
      path: 'data-analysis',
      component: () =>
        import('@/views/demo/visualization/data-analysis/index.vue'),
      meta: {
        locale: 'menu.visualization.dataAnalysis',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      name: 'MultiDimensionDataAnalysis',
      path: 'multi-dimension-data-analysis',
      component: () =>
        import(
          '@/views/demo/visualization/multi-dimension-data-analysis/index.vue'
        ),
      meta: {
        locale: 'menu.visualization.multiDimensionDataAnalysis',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      name: 'RealTimeMonitor',
      path: 'real-time-monitor',
      component: () =>
        import('@/views/demo/visualization/real-time-monitor/index.vue'),
      meta: {
        locale: 'menu.dashboard.realTimeMonitor',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default VISUALIZATION;
