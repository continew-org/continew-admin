export interface UserState {
  userId: number;
  username: string;
  nickname: string;
  gender: number;
  phone?: string;
  email: string;
  avatar?: string;
  description?: string;
  pwdResetTime?: string;
  registrationDate?: string;
  deptId?: number;
  deptName?: string;
  permissions: Array<string>;
  roles: Array<string>;
}
