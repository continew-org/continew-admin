const TIMER_KEY = 'timer';
const TOKEN_KEY = 'token';

const isLogin = () => {
  return !!localStorage.getItem(TOKEN_KEY);
};

const getToken = () => {
  return localStorage.getItem(TOKEN_KEY);
};

const setToken = (token: string) => {
  localStorage.setItem(TOKEN_KEY, token);
};

const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY);
};

const setTimer = (timer: number) => {
  localStorage.setItem(TIMER_KEY, String(timer));
};

const clearTimer = () => {
  clearInterval(Number(localStorage.getItem(TIMER_KEY)));
  localStorage.removeItem(TIMER_KEY);
};

export { isLogin, getToken, setToken, clearToken, setTimer, clearTimer };
