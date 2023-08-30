import { fixedRoutes, demoRoutes } from '../routes';

const mixinRoutes = [...fixedRoutes, ...demoRoutes];

const staticMenus = mixinRoutes.map((el) => {
  const { name, path, meta, redirect, children } = el;
  return {
    name,
    path,
    meta,
    redirect,
    children,
  };
});

export default staticMenus;
