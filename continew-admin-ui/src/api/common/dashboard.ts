import axios from 'axios';
import type { TableData } from '@arco-design/web-vue/es/table/interface';

const BASE_URL = '/dashboard';

export interface DashboardTotalRecord {
  pvCount: number;
  ipCount: number;
  todayPvCount: number;
  newPvFromYesterday: number;
}

export interface DashboardPopularModuleRecord {
  module: string;
  pvCount: number;
  newPvFromYesterday: number;
}

export interface DashboardGeoDistributionRecord {
  locations: string[];
  locationIpStatistics: [];
}

export interface DashboardAnnouncementRecord {
  id: string;
  title: string;
  type: number;
}

export function getTotal() {
  return axios.get<DashboardTotalRecord>(`${BASE_URL}/total`);
}

export function listPopularModule() {
  return axios.get<DashboardPopularModuleRecord[]>(
    `${BASE_URL}/popular/module`
  );
}

export function getGeoDistribution() {
  return axios.get<DashboardGeoDistributionRecord>(
    `${BASE_URL}/geo/distribution`
  );
}

export function listAnnouncement() {
  return axios.get<DashboardAnnouncementRecord[]>(`${BASE_URL}/announcement`);
}

export interface ContentDataRecord {
  x: string;
  y: number;
}

export function queryContentData() {
  return axios.get<ContentDataRecord[]>('/api/content-data');
}