import debug from './env';

export default ({ mock, setup }: { mock?: boolean; setup: () => void }) => {
  if (mock !== false && debug) setup();
};

export const successResponseWrap = (data: unknown) => {
  return {
    data,
    success: true,
    code: 200,
    msg: '操作成功',
  };
};

export const failResponseWrap = (data: unknown, msg: string, code = 500) => {
  return {
    data,
    success: false,
    code,
    msg,
  };
};
