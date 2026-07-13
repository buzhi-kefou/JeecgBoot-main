import { defHttp } from '/@/utils/http/axios';

enum Api {
  childLines = '/erp/material-bill/child-lines',
  materialOptions = '/erp/material-bill/material-options',
  getOrgList = '/erp/purchase/getOrgList',
}

export const getMaterialBillChildLines = (params: { materialCode: string; useOrgId: number }) => {
  return defHttp.post({ url: Api.childLines, data: params });
};

export const getMaterialBillMaterialOptions = (params: { keyword: string; useOrgId?: string | number }) => {
  return defHttp.post({ url: Api.materialOptions, data: params });
};

export const getOrgList = (params: { keyword?: string; pageNo?: number; pageSize?: number }) => {
  return defHttp.post({ url: Api.getOrgList, data: params });
};
