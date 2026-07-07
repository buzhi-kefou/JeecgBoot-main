import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/erp/purchase/queryProductionOrderList',
}

export const getProductionOrderList = (params) => {
  return defHttp.post({ url: Api.list, data: params });
};
