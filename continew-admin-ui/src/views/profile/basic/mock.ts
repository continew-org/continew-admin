import Mock from 'mockjs';
import setupMock, { successResponseWrap } from '@/utils/setup-mock';

setupMock({
  setup() {
    Mock.mock(new RegExp('/api/profile/basic'), () => {
      return successResponseWrap({
        status: 2,
        video: {
          mode: '自定义',
          acquisition: {
            resolution: '720*1280',
            frameRate: 15,
          },
          encoding: {
            resolution: '720*1280',
            rate: {
              min: 300,
              max: 800,
              default: 1500,
            },
            frameRate: 15,
            profile: 'high',
          },
        },
        audio: {
          mode: '自定义',
          acquisition: {
            channels: 8,
          },
          encoding: {
            channels: 8,
            rate: 128,
            profile: 'ACC-LC',
          },
        },
      });
    });
    Mock.mock(new RegExp('/api/operation/log'), () => {
      return successResponseWrap([
        {
          key: '1',
          contentNumber: '视频类001003',
          updateContent: '视频参数变更',
          status: 0,
          updateTime: '2021-02-28 10:30:50',
        },
        {
          key: '2',
          contentNumber: '视频类058212',
          updateContent: '视频参数变更；音频参数变更',
          status: 1,
          updateTime: '2020-05-13 08:00:00',
        },
      ]);
    });
  },
});
