import { useUserStore } from '@/store';

/**
 * 权限判断
 *
 * @param value 权限码列表
 * @return true 有权限，false 没有权限
 */
export default function checkPermission(value: Array<string>) {
  const userStore = useUserStore();
  const { permissions, roles } = userStore;
  const superAdmin = 'admin';
  const allPermission = '*';

  if (Array.isArray(value) && value.length > 0) {
    const permissionValues = value;
    // 校验权限码
    const hasPermission = permissions.some((permission: string) => {
      return (
        allPermission === permission || permissionValues.includes(permission)
      );
    });
    // 检验角色编码
    const hasRole = roles.some((role: string) => {
      return superAdmin === role || permissionValues.includes(role);
    });
    return hasPermission || hasRole;
  }
  throw new Error(
    `need roles! Like v-permission="['admin','system:user:add']"`
  );
}
