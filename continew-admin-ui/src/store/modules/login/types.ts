export type RoleType = '' | '*' | 'admin' | 'user';
export interface UserState {
  nickname?: string;
  avatar?: string;
  email?: string;
  phone?: string;
  job?: string;
  organization?: string;
  location?: string;
  introduction?: string;
  personalWebsite?: string;
  jobName?: string;
  organizationName?: string;
  locationName?: string;
  registrationDate?: string;
  accountId?: string;
  certification?: number;
  role: RoleType;
}
