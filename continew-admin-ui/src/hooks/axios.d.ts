import axios, { Axios, AxiosResponse, AxiosRequestConfig } from 'axios';

declare module 'axios' {
  interface AxiosResponse<T = any> {
    success: boolean; // 是否成功
    code: number; // 状态码
    msg: string; // 状态信息
    timestamp: string; // 时间戳
    data: T; // 返回数据
  }
  export function create(config?: AxiosRequestConfig): AxiosInstance;
}
