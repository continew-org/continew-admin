import axios from 'axios';
import type { TableData } from '@arco-design/web-vue/es/table/interface';

const BASE_URL = '/dashboard';

export interface AnnouncementDashboardRecord {
  id: string;
  title: string;
  type: number;
}

export function listAnnouncement() {
  return axios.get<AnnouncementDashboardRecord[]>(`${BASE_URL}/announcement`);
}

export interface ContentDataRecord {
  x: string;
  y: number;
}

export function queryContentData() {
  return axios.get<ContentDataRecord[]>('/api/content-data');
}

export interface PopularRecord {
  key: number;
  clickNumber: string;
  title: string;
  increases: number;
}

export function queryPopularList(params: { type: string }) {
  return axios.get<TableData[]>('/api/popular/list', { params });
}
