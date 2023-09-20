import axios from 'axios';
import qs from 'query-string';

const BASE_URL = '/api/system/option';

export interface BasicConfigRecord {
  site_title?: string;
  site_copyright?: string;
  site_logo: { url?: string };
  site_favicon: { url?: string };
}

export interface DataRecord {
  name: string;
  code: string;
  value: string;
  description?: string;
}

export interface ListParam {
  code?: Array<string>;
}

export function list(params: ListParam) {
  return axios.get<DataRecord[]>(`${BASE_URL}`, {
    params,
    paramsSerializer: (obj) => {
      return qs.stringify(obj);
    },
  });
}
