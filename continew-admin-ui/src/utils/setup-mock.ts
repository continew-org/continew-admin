// import debug from './env';

export default ({ mock, setup }: { mock?: boolean; setup: () => void }) => {
  // 仅在开发环境启用 mock
  // if (mock !== false && debug) setup();

  // 在生产环境也启用 mock
  if (mock !== false) setup();
};

export const successResponseWrap = (data: unknown) => {
  return {
    data,
    success: true,
    code: 200,
    msg: '操作成功',
    timestamp: new Date().toLocaleDateString().replace(/\//g, '-'),
  };
};

export const failResponseWrap = (data: unknown, msg: string, code = 500) => {
  return {
    data,
    success: false,
    code,
    msg,
    timestamp: new Date().toLocaleDateString().replace(/\//g, '-'),
  };
};
