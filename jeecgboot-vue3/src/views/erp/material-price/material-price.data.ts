import { BasicColumn, FormSchema } from '/@/components/Table';
import { ref } from 'vue';

/**
 * 生成月份列配置
 */
export const generateMonthColumns = (): BasicColumn[] => {
  const months: BasicColumn[] = [];
  for (let i = 1; i <= 12; i++) {
    months.push({
      title: `${i}月`,
      dataIndex: ['monthlyPrices', i],
      width: 100,
      align: 'center',
      customRender: ({ text }) => {
        return text && text > 0 ? `¥${text}` : '¥0';
      },
    });
  }
  return months;
};

/**
 * 生成年份选项
 */
export const generateYearOptions = (): { label: string; value: number }[] => {
  const options: { label: string; value: number }[] = [];
  const currentYear = new Date().getFullYear();
  for (let i = currentYear - 5; i <= currentYear + 2; i++) {
    options.push({
      label: `${i}年`,
      value: i,
    });
  }
  return options.sort((a, b) => b.value - a.value);
};

export const tableColumns: BasicColumn[] = [
  {
    title: '物料编码',
    dataIndex: 'materialCode',
    width: 150,
    align: 'left',
  },
  {
    title: '物料名称',
    dataIndex: 'materialName',
    width: 180,
    align: 'left',
  },
  {
    title: '规格',
    dataIndex: 'specification',
    width: 160,
    align: 'left',
  },
  {
    title: '使用组织',
    dataIndex: 'useOrgId',
    width: 160,
    align: 'left',
  },
  {
    title: '供应商编码',
    dataIndex: 'supplierId',
    width: 120,
    align: 'left',
  },
  {
    title: '供应商名称',
    dataIndex: 'supplierName',
    width: 150,
    align: 'left',
  },
  {
    title: '年平均价格',
    dataIndex: 'avgPrice',
    width: 120,
    align: 'center',
    customRender: ({ text }) => {
      return text && text > 0 ? `¥${text}` : '¥0';
    },
  },
  {
    title: '变化率',
    dataIndex: 'changeRate',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const rate = Number(text || 0);
      return `${rate > 0 ? '+' : ''}${rate}%`;
    },
  },
  {
    title: '记录数',
    dataIndex: 'recordCount',
    width: 100,
    align: 'center',
  },
  ...generateMonthColumns(),
];

// 物料选项
export const materialCodeOptions = ref<{ label: string; value: string }[]>([]);
export const orgOptions = ref<{ label: string; value: string }[]>([]);

// 加载物料选项
export const loadMaterialOptions = async (keyword: string = '', useOrgId?: string) => {
  try {
    const { getMaterialCodeList } = await import('./material-price.api');
    const result = await getMaterialCodeList({ keyword, useOrgId });
    materialCodeOptions.value = result.map((item) => ({
      label: `${item.materialCode} - ${item.materialName} - ${item.specification}`,
      value: item.materialCode,
    }));
  } catch (error) {
    console.error('加载物料列表失败:', error);
    materialCodeOptions.value = [];
  }
};

// 加载组织选项
export const loadOrgOptions = async (keyword: string = '') => {
  try {
    const { getOrgList } = await import('./material-price.api');
    const result = await getOrgList({ keyword, pageNo: 1, pageSize: 50 });
    const records = result?.records || [];
    orgOptions.value = records.map((item) => ({
      label: item.orgCode ? `${item.orgCode} - ${item.orgName}` : item.orgName,
      value: item.orgId,
    }));
  } catch (error) {
    console.error('加载组织列表失败:', error);
    orgOptions.value = [];
  }
};

export const searchFormSchema: FormSchema[] = [
  {
    field: 'materialCode',
    label: '物料编码',
    component: 'Select',
    colProps: { span: 10 },
    componentProps: ({ formModel }) => ({
      placeholder: '请输入物料编码搜索，不填查询全部物料',
      showSearch: true,
      allowClear: true,
      options: materialCodeOptions,
      filterOption: false,
      onSearch: (keyword: string) => loadMaterialOptions(keyword, formModel.useOrgId),
    }),
  },
  {
    field: 'useOrgId',
    label: '使用组织',
    component: 'Select',
    colProps: { span: 8 },
    componentProps: {
      placeholder: '请选择使用组织',
      showSearch: true,
      allowClear: true,
      options: orgOptions,
      filterOption: false,
      onSearch: loadOrgOptions,
    },
  },
  {
    field: 'year',
    label: '年份',
    component: 'Select',
    defaultValue: new Date().getFullYear(),
    colProps: { span: 4 },
    componentProps: {
      placeholder: '请选择年份',
      options: generateYearOptions(),
    },
    rules: [{ required: true, message: '请选择年份' }],
  },
];
