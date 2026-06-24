import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/erp/purchase/queryMaterialSupplierPrice',
  getMaterialCodeList = '/erp/purchase/getMaterialCodeList',
  getOrgList = '/erp/purchase/getOrgList',
}

/**
 * 查询物料供应商价格列表
 * @param params 物料编码、年份和分页参数
 */
export const getMaterialPriceList = (params: { materialCode?: string; useOrgId?: string; year: number; pageNo?: number; pageSize?: number }) => {
  return defHttp.post({ url: Api.list, data: params });
};

/**
 * 获取物料编码列表
 * @param params
 */
export const getMaterialCodeList = (params: { keyword: string; useOrgId?: string }) => {
  return defHttp.post({ url: Api.getMaterialCodeList, data: params });
};

/**
 * 获取组织下拉列表
 * @param params
 */
export const getOrgList = (params: { keyword?: string; pageNo?: number; pageSize?: number }) => {
  return defHttp.post({ url: Api.getOrgList, data: params });
};
