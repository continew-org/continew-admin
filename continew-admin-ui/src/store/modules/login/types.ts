export type RoleType = '' | '*' | 'admin' | 'user';
export interface UserState {
  userId: string;
  username: string;
  nickname: string;
  gender: number;
  phone?: string;
  email: string;
  avatar?: string;
  description?: string;
  pwdResetTime?: string;
  registrationDate?: string;

  job?: string;
  jobName?: string;
  organization?: string;
  organizationName?: string;
  location?: string;
  locationName?: string;
  introduction?: string;
  personalWebsite?: string;
  role: RoleType;
}
