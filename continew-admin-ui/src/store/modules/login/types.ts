export type RoleType = '' | '*' | 'admin' | 'user';
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

  job?: string;
  jobName?: string;
  location?: string;
  locationName?: string;
  introduction?: string;
  personalWebsite?: string;
  role: RoleType;
}
