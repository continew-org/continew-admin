import Mock from 'mockjs';
import setupMock, { successResponseWrap } from '@/utils/setup-mock';

setupMock({
  setup() {
    Mock.mock(new RegExp('/api/user/save-info'), () => {
      return successResponseWrap('ok');
    });
    Mock.mock(new RegExp('/api/user/certification'), () => {
      return successResponseWrap({
        enterpriseInfo: {
          accountType: '企业账号',
          status: 0,
          time: '2022-12-27 20:00:00',
          legalPerson: '张**',
          certificateType: '中国身份证',
          authenticationNumber: '110************123',
          enterpriseName: '低调有实力的企业',
          enterpriseCertificateType: '企业营业执照',
          organizationCode: '7*******9',
        },
        record: [
          {
            certificationType: 1,
            certificationContent: '企业实名认证，法人姓名：张**',
            status: 0,
            time: '2022-12-27 20:00:00',
          },
          {
            certificationType: 1,
            certificationContent: '企业实名认证，法人姓名：张**',
            status: 1,
            time: '2022-12-27 20:00:00',
          },
        ],
      });
    });
    Mock.mock(new RegExp('/api/user/upload'), () => {
      return successResponseWrap('ok');
    });
  },
});
