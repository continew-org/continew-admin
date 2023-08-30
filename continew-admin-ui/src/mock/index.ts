import Mock from 'mockjs';

import './login';
import './message-box';

import '@/views/dashboard/workplace/mock';

import '@/views/demo/visualization/monitor/mock';

import '@/views/demo/list/card/mock';
import '@/views/demo/list/search-table/mock';

import '@/views/demo/form/step/mock';

import '@/views/demo/profile/basic/mock';

import '@/views/demo/visualization/data-analysis/mock';
import '@/views/demo/visualization/multi-dimension-data-analysis/mock';

import '@/views/system/user/center/mock';

Mock.setup({
  timeout: '600-1000',
});
