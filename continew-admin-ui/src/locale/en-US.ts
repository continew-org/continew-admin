import localeMessageBox from '@/components/message-box/locale/en-US';
import localeLogin from '@/views/login/locale/en-US';

import localeWorkplace from '@/views/dashboard/workplace/locale/en-US';

import localeMonitor from '@/views/dashboard/monitor/locale/en-US';

import localeDataAnalysis from '@/views/visualization/data-analysis/locale/en-US';
import localeMultiDAnalysis from '@/views/visualization/multi-dimension-data-analysis/locale/en-US';

import localeDept from '@/views/system/dept/locale/en-US';

import localeOnlineUser from '@/views/monitor/online/locale/en-US';
import localeLoginLog from '@/views/monitor/log/login/locale/en-US';
import localeOperationLog from '@/views/monitor/log/operation/locale/en-US';
import localeSystemLog from '@/views/monitor/log/system/locale/en-US';

import localeSearchTable from '@/views/list/search-table/locale/en-US';
import localeCardList from '@/views/list/card/locale/en-US';

import localeStepForm from '@/views/form/step/locale/en-US';
import localeGroupForm from '@/views/form/group/locale/en-US';

import localeBasicProfile from '@/views/profile/basic/locale/en-US';

import localeSuccess from '@/views/result/success/locale/en-US';
import localeError from '@/views/result/error/locale/en-US';

import locale403 from '@/views/exception/403/locale/en-US';
import locale404 from '@/views/exception/404/locale/en-US';
import locale500 from '@/views/exception/500/locale/en-US';

import localeUserCenter from '@/views/system/user/center/locale/en-US';

import localeSettings from './en-US/settings';

export default {
  'title': 'ContiNew Admin',
  'menu.dashboard': 'Dashboard',
  'menu.server.dashboard': 'Dashboard-Server',
  'menu.server.workplace': 'Workplace-Server',
  'menu.server.monitor': 'Monitor-Server',
  'menu.visualization': 'Data Visualization',
  'menu.system': 'System management',
  'menu.monitor': 'Monitor',
  'menu.list': 'List',
  'menu.form': 'Form',
  'menu.profile': 'Profile',
  'menu.result': 'Result',
  'menu.exception': 'Exception',
  'menu.user': 'User Center',
  'menu.arcoWebsite': 'Arco Design',
  'menu.faq': 'FAQ',
  'navbar.docs': 'Docs',
  'navbar.action.locale': 'Switch to English',
  ...localeSettings,
  ...localeMessageBox,
  ...localeLogin,
  ...localeWorkplace,
  ...localeMonitor,
  ...localeDataAnalysis,
  ...localeMultiDAnalysis,

  ...localeDept,

  ...localeOnlineUser,
  ...localeLoginLog,
  ...localeOperationLog,
  ...localeSystemLog,

  ...localeSearchTable,
  ...localeCardList,
  ...localeStepForm,
  ...localeGroupForm,
  ...localeBasicProfile,
  ...localeSuccess,
  ...localeError,
  ...locale403,
  ...locale404,
  ...locale500,
  ...localeUserCenter,
};
