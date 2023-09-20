import Mock from 'mockjs';
import setupMock, { successResponseWrap } from '@/utils/setup-mock';

setupMock({
  setup() {
    Mock.mock(new RegExp('/api/system/option'), () => {
      return successResponseWrap([
        {
          name: '系统标题',
          code: 'site_title',
          value: 'ContiNew Admin',
          description: '用于显示登录页面的系统标题。',
        },
        {
          name: '版权信息',
          code: 'site_copyright',
          value:
            'Copyright © 2022-2023 Charles7c ⋅ ContiNew Admin ⋅ 津ICP备2022005864号-2',
          description: '用于显示登录页面的底部版权信息。',
        },
        {
          name: '系统LOGO（16*16）',
          code: 'site_favicon',
          value: 'https://cnadmin.charles7c.top/favicon.ico',
          description: '用于显示浏览器地址栏的系统LOGO。',
        },
        {
          name: '系统LOGO（48*48）',
          code: 'site_logo',
          value: 'https://cnadmin.charles7c.top/logo.svg',
          description: '用于显示登录页面的系统LOGO。',
        },
      ]);
    });
  },
});
