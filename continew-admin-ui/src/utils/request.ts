import axios from "axios";
import type { AxiosRequestConfig, AxiosResponse } from "axios";
import { Message } from "@arco-design/web-vue";
import { getToken } from "@/utils/auth";

// default config
if (import.meta.env.VITE_API_BASE_URL) {
  axios.defaults.baseURL = import.meta.env.VITE_API_BASE_URL;
  axios.defaults.timeout = 60000 // 1 分钟
}

// request interceptors
axios.interceptors.request.use((config: AxiosRequestConfig) => {
    const token = getToken();
    if (token) {
      if (!config.headers) {
        config.headers = {};
      }
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.log(error);
    return Promise.reject(error);
  }
);

export interface HttpResponse<T = unknown> {
  success: boolean; // 是否成功
  code: number; // 状态码
  msg: string; // 状态信息
  timestamp: string; // 时间戳
  data: T; // 返回数据
}

// response interceptors
axios.interceptors.response.use((response: AxiosResponse<HttpResponse>) => {
    const res = response.data;
    if (res.success) {
      return res;
    }

    Message.error({
      content: res.msg,
      duration: 3000
    });
    //
    // if (res.code === 401) {
    //   // 重定向路由到登录页面
    // }
    return Promise.reject(new Error(res.msg));
  },
  (error) => {
    console.error(`err: ${error}`);
    const res = error.response.data;
    Message.error({
      content: res.msg || "网络错误",
      duration: 3000
    });
    return Promise.reject(error);
  }
);