import { BasicColumn, FormSchema } from '/@/components/Table';
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
  // textColumn('主键', 'fid', 180),
  textColumn('单据编号', 'billNo', 130),
  textColumn('单据类型', 'billType', 110),
  textColumn('生产组织', 'prdOrgId', 140),
  textColumn('单据日期', 'date', 120, 'center'),
  textColumn('计划组', 'workGroupId', 100),
  textColumn('计划员', 'plannerId', 100),
  textColumn('单据状态', 'documentStatus', 90, 'center', {
    customRender: ({ text }) => (text === 'C' ? '已审核' : text),
  }),
  textColumn('审批人', 'approverId', 100),
  textColumn('审批日期', 'approveDate', 160, 'center'),
  // textColumn('修改人', 'modifierId', 140),
  // textColumn('创建日期', 'createDate', 160, 'center'),
  // textColumn('创建人', 'creatorId', 140),
  // textColumn('修改日期', 'modifyDate', 160, 'center'),
  // textColumn('作废日期', 'cancelDate', 160, 'center'),
  // textColumn('作废人', 'canceler', 140),
  // textColumn('作废状态', 'cancelStatus', 100, 'center'),
  textColumn('备注', 'description', 180, 'left', { ellipsis: true }),
  textColumn('最后入库时间', 'ukptZhrkrq', 160, 'center'),
  textColumn('最早领料时间', 'ukptZzllrq', 160, 'center'),
  // textColumn('明细主键', 'lineEntryId', 180),
  // textColumn('头表ID', 'linePId', 180),
  // textColumn('物料ID', 'materialId', 160),
  textColumn('产品类型', 'productType', 110),
  textColumn('物料编码', 'materialNumber', 140),
  textColumn('物料名称', 'materialName', 220),
  textColumn('规格', 'specification', 200),
  textColumn('BOM版本', 'bomId', 160),
  // textColumn('业务状态', 'lineStatus', 100, 'center'),
  textColumn('业务状态名称', 'lineStatusLabel', 130, 'center'),
  textColumn('计划开工', 'planStartDate', 160, 'center'),
  textColumn('计划完工', 'planFinishDate', 160, 'center'),
  textColumn('需求单据', 'saleOrderNo', 150),
  textColumn('排产状态', 'scheduleStatus', 110, 'center'),
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'billNo',
    label: '单据编号',
    component: 'Input',
    colProps: { span: 6 },
    componentProps: {
      placeholder: '请输入单据编号',
      allowClear: true,
    },
  },
  {
    field: 'prdOrgId',
    label: '生产组织',
    component: 'Input',
    colProps: { span: 6 },
    componentProps: {
      placeholder: '请输入生产组织',
      allowClear: true,
    },
  },
  {
    field: 'materialId',
    label: '物料编码',
    component: 'Input',
    colProps: { span: 6 },
    componentProps: {
      placeholder: '请输入明细物料编码',
      allowClear: true,
    },
  },
  {
    field: 'planStartRange',
    label: '计划开工',
    component: 'RangePicker',
    colProps: { span: 6 },
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      allowClear: true,
    },
  },
  {
    field: 'planFinishRange',
    label: '计划完工',
    component: 'RangePicker',
    colProps: { span: 6 },
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      allowClear: true,
    },
  },
];
