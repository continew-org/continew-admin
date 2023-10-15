export interface UserState {
  id: string;
  username: string;
  nickname: string;
  gender: number;
  phone?: string;
  email: string;
  avatar?: string;
  description?: string;
  pwdResetTime?: string;
  registrationDate?: string;
  deptId?: string;
  deptName?: string;
  permissions: Array<string>;
  roles: Array<string>;
}
