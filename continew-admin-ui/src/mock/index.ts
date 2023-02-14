import Mock from 'mockjs';

import './login';
import './message-box';

import '@/views/dashboard/workplace/mock';

import '@/views/arco-design/visualization/monitor/mock';

import '@/views/arco-design/list/card/mock';
import '@/views/arco-design/list/search-table/mock';

import '@/views/arco-design/form/step/mock';

import '@/views/arco-design/profile/basic/mock';

import '@/views/arco-design/visualization/data-analysis/mock';
import '@/views/arco-design/visualization/multi-dimension-data-analysis/mock';

import '@/views/system/user/center/mock';

Mock.setup({
  timeout: '600-1000',
});
