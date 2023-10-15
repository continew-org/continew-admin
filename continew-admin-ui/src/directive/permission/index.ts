import { DirectiveBinding } from 'vue';
import { useUserStore } from '@/store';

function checkPermission(el: HTMLElement, binding: DirectiveBinding) {
  const { value } = binding;
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
    // 如果没有权限，移除元素
    if (!hasPermission && !hasRole && el.parentNode) {
      el.parentNode.removeChild(el);
    }
  } else {
    throw new Error(
      `need roles! Like v-permission="['admin','system:user:add']"`
    );
  }
}

export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
  updated(el: HTMLElement, binding: DirectiveBinding) {
    checkPermission(el, binding);
  },
};
