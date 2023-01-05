import { UserState } from '@/store/modules/login/types';
import Unknown from '../assets/images/avatar/unknown.png';
import Male from '../assets/images/avatar/male.png';
import Female from '../assets/images/avatar/female.png';

export default function getAvatar(loginStore: UserState) {
  const userAvatar = loginStore.avatar;
  if (userAvatar) {
    const baseUrl = import.meta.env.VITE_API_BASE_URL;
    return `${baseUrl}/avatar/${userAvatar}`;
  }

  const userGender = loginStore.gender;
  if (userGender === 1) {
    return Male;
  }
  if (userGender === 2) {
    return Female;
  }
  return Unknown;
}
