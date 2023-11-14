import axios from 'axios';

const BASE_URL = '/dashboard';

export interface DashboardTotalRecord {
  pvCount: number;
  ipCount: number;
  todayPvCount: number;
  newPvFromYesterday: number;
}

export interface DashboardAccessTrendRecord {
  date: string;
  pvCount: number;
  ipCount: number;
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
  id: number;
  title: string;
  type: number;
}

export interface DashboardRecentlyVisitedRecord {
  title?: string;
  path: string;
  icon?: string;
}

export function getTotal() {
  return axios.get<DashboardTotalRecord>(`${BASE_URL}/total`);
}

export function listAccessTrend(days: number) {
  return axios.get<DashboardAccessTrendRecord[]>(
    `${BASE_URL}/access/trend/${days}`,
  );
}

export function listPopularModule() {
  return axios.get<DashboardPopularModuleRecord[]>(
    `${BASE_URL}/popular/module`,
  );
}

export function getGeoDistribution() {
  return axios.get<DashboardGeoDistributionRecord>(
    `${BASE_URL}/geo/distribution`,
  );
}

export function listAnnouncement() {
  return axios.get<DashboardAnnouncementRecord[]>(`${BASE_URL}/announcement`);
}
