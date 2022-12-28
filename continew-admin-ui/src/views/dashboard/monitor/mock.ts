import Mock from 'mockjs';

import setupMock, {
  successResponseWrap,
  // failResponseWrap,
} from '@/utils/setup-mock';

setupMock({
  setup() {
    Mock.mock(new RegExp('/api/chat/list'), () => {
      // return failResponseWrap(null, '重新登陆', 50008);
      const data = Mock.mock({
        'data|4-6': [
          {
            'id|+1': 1,
            'username': '用户7352772',
            'content': '马上就开始了，好激动！',
            'time': '13:09:12',
            'isCollect|2': true,
          },
        ],
      });
      return successResponseWrap(data.data);
    });
  },
});
