import Mock from 'mockjs';
import setupMock, { successResponseWrap } from '@/utils/setup-mock';
import { ServiceRecord } from '@/api/list';

const qualityInspectionList: ServiceRecord[] = [
  {
    id: 1,
    name: 'quality',
    title: '视频类-历史导入',
    description: '2021-10-12 00:00:00',
    data: [
      {
        label: '待质检数',
        value: '120',
      },
      {
        label: '积压时长',
        value: '60s',
      },
      {
        label: '待抽检数',
        value: '0',
      },
    ],
  },
  {
    id: 2,
    name: 'quality',
    title: '图文类-图片版权',
    description: '2021-12-11 18:30:00',
    data: [
      {
        label: '待质检数',
        value: '120',
      },
      {
        label: '积压时长',
        value: '60s',
      },
      {
        label: '待抽检数',
        value: '0',
      },
    ],
  },
  {
    id: 3,
    name: 'quality',
    title: '图文类-高清图片',
    description: '2021-10-15 08:10:00',
    data: [
      {
        label: '待质检数',
        value: '120',
      },
      {
        label: '积压时长',
        value: '60s',
      },
      {
        label: '待抽检数',
        value: '0',
      },
    ],
  },
];
const theServiceList: ServiceRecord[] = [
  {
    id: 1,
    icon: 'code',
    title: '漏斗分析',
    description:
      '用户行为分析之漏斗分析模型是企业实现精细化运营、进行用户行为分析的重要数据分析模型。',
    enable: true,
    actionType: 'button',
  },
  {
    id: 2,
    icon: 'edit',
    title: '用户分布',
    description:
      '快速诊断用户人群，地域细分情况，了解数据分布的集中度，以及主要的数据分布的区间段是什么。',
    enable: true,
    actionType: 'button',
    expires: true,
  },
  {
    id: 3,
    icon: 'user',
    title: '资源分发',
    description:
      '移动端动态化资源分发解决方案。提供稳定大流量服务支持、灵活定制的分发圈选规则，通过离线化预加载。',
    enable: false,
    actionType: 'button',
  },
  {
    id: 4,
    icon: 'user',
    title: '用户画像分析',
    description:
      '用户画像就是将典型用户信息标签化，根据用户特征、业务场景和用户行为等信息，构建一个标签化的用户模型。',
    enable: true,
    actionType: 'button',
  },
];
const rulesPresetList: ServiceRecord[] = [
  {
    id: 1,
    title: '内容屏蔽规则',
    description:
      '用户在执行特定的内容分发任务时，可使用内容屏蔽规则根据特定标签，过滤内容集合。',
    enable: true,
    actionType: 'switch',
  },
  {
    id: 2,
    title: '内容置顶规则',
    description:
      '该规则支持用户在执行特定内容分发任务时，对固定的几条内容置顶。',
    enable: true,
    actionType: 'switch',
  },
  {
    id: 3,
    title: '内容加权规则',
    description: '选定内容加权规则后可自定义从不同内容集合获取内容的概率。',
    enable: false,
    actionType: 'switch',
  },
  {
    id: 4,
    title: '内容分发规则',
    description: '内容分发时，对某些内容需要固定在C端展示的位置。',
    enable: true,
    actionType: 'switch',
  },
  {
    id: 5,
    title: '违禁内容识别',
    description: '精准识别赌博、刀枪、毒品、造假、贩假等违规物品和违规行为。',
    enable: false,
    actionType: 'switch',
  },
  {
    id: 6,
    title: '多语言文字符号识别',
    description:
      '精准识别英语、维语、藏语、蒙古语、朝鲜语等多种语言以及emoji表情形态的语义识别。',
    enable: false,
    actionType: 'switch',
  },
];

setupMock({
  setup() {
    // Quality Inspection
    Mock.mock(new RegExp('/api/list/quality-inspection'), () => {
      return successResponseWrap(
        qualityInspectionList.map((_, index) => ({
          ...qualityInspectionList[index % qualityInspectionList.length],
          id: Mock.Random.guid(),
        }))
      );
    });

    // the service
    Mock.mock(new RegExp('/api/list/the-service'), () => {
      return successResponseWrap(
        theServiceList.map((_, index) => ({
          ...theServiceList[index % theServiceList.length],
          id: Mock.Random.guid(),
        }))
      );
    });

    // rules preset
    Mock.mock(new RegExp('/api/list/rules-preset'), () => {
      return successResponseWrap(
        rulesPresetList.map((_, index) => ({
          ...rulesPresetList[index % rulesPresetList.length],
          id: Mock.Random.guid(),
        }))
      );
    });
  },
});
