import { BasicColumn, FormSchema } from '/@/components/Table';
import { ref } from 'vue';
import { getMaterialBillMaterialOptions, getOrgList } from './material-bill-tree.api';

const borderStyle = '1px solid #000';

const textColumn = (
  title: string,
  dataIndex: string,
  width = 140,
  align: BasicColumn['align'] = 'left',
  extra: Partial<BasicColumn> = {}
): BasicColumn => ({
  title,
  dataIndex,
  width,
  align,
  customHeaderCell: () => ({
    style: {
      borderRight: borderStyle,
      borderBottom: borderStyle,
    },
  }),
  customCell: () => ({
    style: {
      borderRight: borderStyle,
      borderBottom: borderStyle,
    },
  }),
  ...extra,
});

export const tableColumns: BasicColumn[] = [
  textColumn('', '__expand__', 46, 'center'),
  textColumn('层级', 'levelNo', 80, 'center'),
  textColumn('父项物料编码', 'parentMaterialCode', 160),
  textColumn('子项物料编码', 'materialCodeChild', 160),
  textColumn('子项物料属性', 'itemProperty', 110, 'center'),
  textColumn('子项 BOM 版本', 'childBomVersion', 180, 'center'),
  textColumn('子项物料名称', 'materialNameChild', 180),
  textColumn('规格型号', 'materialModelChild', 180),
  textColumn('用量分子', 'numerator', 110, 'right'),
  textColumn('用量分母', 'denominator', 110, 'right'),
  // textColumn('子项 BOM', 'bomId', 130),
];

export const purchaseUsageColumns: BasicColumn[] = [
  textColumn('物料编码', 'materialCodeChild', 160),
  textColumn('物料名称', 'materialNameChild', 180),
  textColumn('规格型号', 'materialModelChild', 180),
  textColumn('物料属性', 'itemProperty', 110, 'center'),
  textColumn('累计分子', 'numerator', 120, 'right'),
  textColumn('累计分母', 'denominator', 120, 'right'),
];

export const materialOptions = ref<{ label: string; value: string }[]>([]);
export const orgOptions = ref<{ label: string; value: number }[]>([]);

export async function loadOrgOptions(keyword = '') {
  const result = await getOrgList({ keyword, pageNo: 1, pageSize: 50 });
  orgOptions.value = (result?.records || []).map((item) => ({
    label: item.orgCode ? `${item.orgCode} - ${item.orgName}` : item.orgName,
    value: Number(item.orgId),
  }));
}

export async function loadMaterialOptions(keyword = '', useOrgId?: string | number) {
  const result = await getMaterialBillMaterialOptions({ keyword, useOrgId });
  materialOptions.value = result.map((item) => ({
    label: `${item.materialCode} - ${item.materialName}${item.specification ? ` - ${item.specification}` : ''}`,
    value: item.materialCode,
  }));
}

export const searchFormSchema: FormSchema[] = [
  {
    field: 'useOrgId',
    label: '使用组织',
    component: 'Select',
    colProps: { span: 6 },
    componentProps: {
      placeholder: '请选择使用组织',
      showSearch: true,
      allowClear: true,
      options: orgOptions,
      filterOption: false,
      onSearch: loadOrgOptions,
    },
    rules: [{ required: true, message: '请选择使用组织' }],
  },
  {
    field: 'materialCode',
    label: '物料编码',
    component: 'Select',
    colProps: { span: 6 },
    componentProps: ({ formModel }) => ({
      placeholder: '请输入物料编码搜索',
      showSearch: true,
      allowClear: true,
      options: materialOptions,
      filterOption: false,
      onSearch: (keyword: string) => loadMaterialOptions(keyword, formModel.useOrgId),
    }),
    rules: [{ required: true, message: '请选择物料编码' }],
  },
];
