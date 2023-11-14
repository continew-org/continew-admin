import type { RouteRecordNormalized } from 'vue-router';

const appModules = import.meta.glob('./modules/*.ts', { eager: true });
const fixedModules = import.meta.glob('./modules/fixed/*.ts', { eager: true });
const demoModules = import.meta.glob('./modules/demo/*.ts', { eager: true });

function formatModules(_modules: any, result: RouteRecordNormalized[]) {
  Object.keys(_modules).forEach((key) => {
    const defaultModule = _modules[key].default;
    if (!defaultModule) return;
    const moduleList = Array.isArray(defaultModule)
      ? [...defaultModule]
      : [defaultModule];
    result.push(...moduleList);
  });
  return result;
}

export const appRoutes: RouteRecordNormalized[] = formatModules(appModules, []);
export const fixedRoutes: RouteRecordNormalized[] = formatModules(
  fixedModules,
  [],
);
export const demoRoutes: RouteRecordNormalized[] = formatModules(
  demoModules,
  [],
);
