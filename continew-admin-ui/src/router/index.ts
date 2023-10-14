import {
  createRouter,
  createWebHistory,
  RouteRecordNormalized,
} from 'vue-router';
import { useAppStore } from '@/store';
import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css';

import { DashboardRecentlyVisitedRecord } from '@/api/common/dashboard';
import { appRoutes, fixedRoutes, demoRoutes } from './routes';
import { REDIRECT_MAIN, NOT_FOUND_ROUTE } from './routes/base';
import createRouteGuard from './guard';

NProgress.configure({ showSpinner: false }); // NProgress Configuration

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: 'login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    {
      path: '/social/callback',
      name: 'SocialCallback',
      component: () => import('@/views/login/social/index.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    ...appRoutes,
    ...fixedRoutes,
    ...demoRoutes,
    REDIRECT_MAIN,
    NOT_FOUND_ROUTE,
  ],
  scrollBehavior() {
    return { top: 0 };
  },
});

createRouteGuard(router);

router.afterEach((to) => {
  const allMenuList = useAppStore().appAsyncMenusAll as RouteRecordNormalized[];
  const toMenu = allMenuList.find((m) => to.path === m.path) || undefined;
  if (toMenu === undefined) {
    return;
  }

  const recentlyVisitedList = window.localStorage.getItem('recently-visited');
  let copyRecentlyVisitedList: DashboardRecentlyVisitedRecord[];
  if (recentlyVisitedList === null) {
    copyRecentlyVisitedList = [];
  } else {
    copyRecentlyVisitedList = JSON.parse(recentlyVisitedList);
  }

  // 是否有重复点击的菜单
  copyRecentlyVisitedList.forEach(
    (item: DashboardRecentlyVisitedRecord, index: number) => {
      if (item.path === to.path) {
        copyRecentlyVisitedList.splice(index, 1);
      }
    }
  );

  // 最多存储 3 个
  const menuMeta = toMenu?.meta;
  const recentlyVisited: DashboardRecentlyVisitedRecord = {
    title: menuMeta?.locale,
    path: to.path,
    icon: menuMeta?.icon,
  };
  copyRecentlyVisitedList.reverse();
  copyRecentlyVisitedList.push(recentlyVisited);
  copyRecentlyVisitedList.reverse();
  if (copyRecentlyVisitedList.length >= 4) {
    copyRecentlyVisitedList = copyRecentlyVisitedList.splice(0, 3);
  }
  window.localStorage.setItem(
    'recently-visited',
    JSON.stringify(copyRecentlyVisitedList)
  );
});

export default router;
