import Unknown from '../assets/images/avatar/unknown.png';
import Male from '../assets/images/avatar/male.png';
import Female from '../assets/images/avatar/female.png';

export default function getAvatar(gender: number | undefined) {
  if (gender === 1) {
    return Male;
  }
  if (gender === 2) {
    return Female;
  }
  return Unknown;
}
