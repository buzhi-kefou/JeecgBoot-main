import { defHttp } from '/@/utils/http/axios';
import type { AxiosResponse } from 'axios';

enum Api {
  list = '/erp/sipm/list',
  image = '/erp/sipm/image',
}

export interface SipmDrawingRecord {
  errcode?: number;
  errmsg?: string | null;
  objId?: string;
  objNo?: string;
  fname?: string | null;
  suffix?: string | null;
  name?: string;
  fsize?: number | null;
  fsizeStr?: string;
  ctimestr?: string;
  mtimestr?: string;
  creator?: string;
  modifier?: string;
  ver?: string;
  extra?: Record<string, string>;
}

export interface SipmPartRecord {
  objId?: string;
  objNo?: string;
  name?: string;
  ctimestr?: string;
  mtimestr?: string;
  creator?: string;
  modifier?: string;
  ver?: string;
  extra?: Record<string, string>;
}

export interface SipmDesignRecord {
  errcode?: number;
  errmsg?: string | null;
  objId?: string;
  objNo?: string;
  fname?: string | null;
  suffix?: string | null;
  name?: string;
  fsize?: number | null;
  fsizeStr?: string;
  ctimestr?: string;
  mtimestr?: string;
  creator?: string;
  modifier?: string;
  ver?: string;
  extra?: Record<string, string>;
}

export interface SipmDrawingListResult {
  part?: SipmPartRecord | null;
  designs?: SipmDesignRecord[];
  drawings?: SipmDrawingRecord[];
  list?: SipmDrawingRecord[];
  errcode?: number;
  errmsg?: string | null;
  total?: number;
}

export const querySipmDrawingList = (params: { no: string; start?: number; size?: number }) => {
  return defHttp.get<SipmDrawingListResult>({ url: Api.list, params });
};

export const getSipmDrawingImageBlob = (id: string, type: 'DWGPE' | 'DESF2') => {
  return defHttp.get<AxiosResponse<Blob>>(
    { url: Api.image, params: { id, type }, responseType: 'blob' },
    { isTransformResponse: false, isReturnNativeResponse: true, successMessageMode: 'none', errorMessageMode: 'none' }
  );
};
