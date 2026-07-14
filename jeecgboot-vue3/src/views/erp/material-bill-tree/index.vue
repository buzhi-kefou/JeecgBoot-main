<template>
  <div class="material-bill-tree-page">
    <section class="data-section bom-tree-section" :class="{ 'is-purchase-tab': activeTab === 'purchase' }">
      <BasicTable class="material-tree-table" @register="registerTable">
        <template #tableTitle>
          <div class="material-bill-table-title">
            <a-tabs v-model:activeKey="activeTab" class="material-bill-tabs">
              <a-tab-pane key="purchase" tab="外购件用量">
                <section class="purchase-usage-section">
                  <header class="section-heading">
                    <div>
                      <div class="section-eyebrow">外购件汇总</div>
                      <div class="section-title">外购件累计用量</div>
                    </div>
                    <div class="section-meta">
                      <a-badge :count="purchaseUsages.length" :number-style="{ backgroundColor: '#2468c9' }" />
                      <span>{{ purchaseUsages.length ? '累计口径：沿父项逐级相乘' : '查询后展示最底层外购件累计用量' }}</span>
                    </div>
                  </header>
                  <BasicTable
                    class="purchase-usage-table"
                    :columns="purchaseUsageColumns"
                    :data-source="purchaseUsages"
                    :pagination="false"
                    :show-action-column="false"
                    :show-index-column="false"
                    :show-table-setting="false"
                    :use-search-form="false"
                    bordered
                    row-key="id"
                    size="small"
                  />
                </section>
              </a-tab-pane>
              <a-tab-pane key="tree" tab="物料清单层级明细">
                <div class="tree-title-bar">
                  <div>
                    <div class="section-eyebrow">BOM 结构</div>
                    <div class="section-title">物料清单层级明细</div>
                  </div>
                  <div class="tree-title-actions">
                    <span class="section-meta">选择物料后展开节点查看下级组成</span>
                    <a-button @click="expandAll">展开全部</a-button>
                    <a-button @click="collapseAll">折叠全部</a-button>
                  </div>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
        </template>
      </BasicTable>
    </section>
  </div>
</template>

<script lang="ts">
  export default {
    name: 'ErpMaterialBillTree',
  };
</script>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getMaterialBillChildLines } from './material-bill-tree.api';
  import {
    loadOrgOptions,
    purchaseUsageColumns,
    searchFormSchema,
    tableColumns,
  } from './material-bill-tree.data';

  interface MaterialBillTreeRow {
    bomId?: string | number;
    billId?: string | number;
    childBomVersion?: string;
    children?: MaterialBillTreeRow[];
    itemProperty?: string;
    levelNo?: string | number;
    materialCodeChild?: string;
    parentBillId?: string | number;
    parentMaterialCode?: string;
    [key: string]: unknown;
  }

  interface MaterialBillPurchaseUsage {
    denominator: number;
    id: string | number;
    itemProperty: string;
    materialCodeChild: string;
    materialModelChild: string;
    materialNameChild: string;
    numerator: number;
    usageAmount: number;
  }

  interface MaterialBillChildLineResult {
    childLines: MaterialBillTreeRow[];
    purchaseUsages: MaterialBillPurchaseUsage[];
  }

  const purchaseUsages = ref<MaterialBillPurchaseUsage[]>([]);
  const activeTab = ref('purchase');

  async function queryMaterialBillChildLines(params: { materialCode: string; useOrgId: number }) {
    const result = (await getMaterialBillChildLines(params)) as MaterialBillChildLineResult;
    purchaseUsages.value = result?.purchaseUsages || [];
    return result?.childLines || [];
  }

  /**
   * 将全层级明细整理为树。优先用 BOM 主键关联；部分历史数据的 bomId 为 0 时，
   * 退化为“父项物料编码 + 上一层级”关联。
   */
  function toMaterialBillTree(rows: MaterialBillTreeRow[]) {
    if (!rows?.length) {
      return rows;
    }
    if (rows.some((row) => row.children?.length)) {
      return rows;
    }

    const nodes = rows.map(({ children: _children, ...row }) => ({ ...row }) as MaterialBillTreeRow);
    const nodesByParentBillId = new Map<string, MaterialBillTreeRow[]>();
    const nodesByLevelAndMaterialCode = new Map<string, MaterialBillTreeRow[]>();
    const childNodes = new Set<MaterialBillTreeRow>();

    nodes.forEach((node) => {
      if (node.parentBillId !== undefined && node.parentBillId !== null) {
        const parentBillId = String(node.parentBillId);
        nodesByParentBillId.set(parentBillId, [...(nodesByParentBillId.get(parentBillId) || []), node]);
      }
      if (node.levelNo !== undefined && node.materialCodeChild) {
        const key = `${node.levelNo}:${node.materialCodeChild}`;
        nodesByLevelAndMaterialCode.set(key, [...(nodesByLevelAndMaterialCode.get(key) || []), node]);
      }
    });

    nodes.forEach((node) => {
      const bomId = String(node.bomId ?? '');
      const childrenByBom = bomId && bomId !== '0' ? nodesByParentBillId.get(bomId) || [] : [];
      if (childrenByBom.length) {
        node.children = childrenByBom;
        childrenByBom.forEach((child) => childNodes.add(child));
      }
    });

    nodes.forEach((node) => {
      if (childNodes.has(node) || !node.parentMaterialCode || node.levelNo === undefined) {
        return;
      }
      const parentLevel = Number(node.levelNo) - 1;
      if (!Number.isFinite(parentLevel)) {
        return;
      }
      const parents = nodesByLevelAndMaterialCode.get(`${parentLevel}:${node.parentMaterialCode}`) || [];
      parents.forEach((parent) => {
        parent.children = [...(parent.children || []), node];
        childNodes.add(node);
      });
    });

    return nodes.filter((node) => !childNodes.has(node));
  }

  function getLevelRowClass(record: MaterialBillTreeRow) {
    const levelNo = Math.max(1, Math.min(5, Number(record.levelNo) || 1));
    return `material-bill-level-${levelNo}`;
  }

  function formatLevelNo(value: unknown) {
    const levelNo = Number(value);
    if (!Number.isFinite(levelNo) || levelNo <= 1) {
      return value ?? '';
    }
    return `${'\u3000'.repeat(Math.floor(levelNo) - 1)}${value}`;
  }

  const materialBillTreeColumns = tableColumns.map((column) =>
    column.dataIndex === 'levelNo'
      ? {
          ...column,
          align: 'left',
          customRender: ({ text }) => formatLevelNo(text),
        }
      : column
  );

  const { tableContext } = useListPage({
    designScope: 'erp-material-bill-tree',
    tableProps: {
      api: queryMaterialBillChildLines,
      immediate: false,
      rowKey: 'id',
      isTreeTable: true,
      defaultExpandAllRows: false,
      expandColumnWidth: 46,
      rowClassName: getLevelRowClass,
      columns: materialBillTreeColumns,
      bordered: true,
      scroll: { x: 1300 },
      pagination: false,
      showActionColumn: false,
      afterFetch: toMaterialBillTree,
      formConfig: {
        labelWidth: 90,
        schemas: searchFormSchema,
        fieldMapToTime: [],
      },
    },
  });

  const [registerTable, { expandAll, collapseAll }] = tableContext;

  onMounted(async () => {
    await loadOrgOptions('');
  });
</script>

<style lang="less" scoped>
  .material-bill-tree-page {
    background: #f4f7fb;
  }

  .data-section {
    overflow: hidden;
    background: #fff;
    border: 1px solid #e6edf6;
    border-radius: 4px;
    box-shadow: 0 1px 2px rgb(34 65 104 / 4%);
  }

  .purchase-usage-section {
    overflow: hidden;
    margin-bottom: 14px;
    border: 1px solid #e6edf6;
    border-top: 3px solid #2468c9;
    border-radius: 4px;
  }

  .bom-tree-section {
    border-top: 3px solid #5d8fd4;
  }

  :deep(.bom-tree-section .jeecg-basic-table-form-container) {
    padding: 12px 12px 8px;
    background: #fff;
    border-bottom: 1px solid #e9eff6;
  }

  :deep(.bom-tree-section .ant-table-title) {
    padding: 0;
  }

  .material-bill-table-title {
    width: 100%;
    padding: 12px;
    background: #f8fbff;
  }

  .material-bill-tabs {
    width: 100%;
  }

  :deep(.material-bill-tabs > .ant-tabs-nav) {
    margin: 0 0 12px;
    padding: 0 6px;
  }

  :deep(.material-bill-tabs > .ant-tabs-content-holder) {
    background: #fff;
  }

  :deep(.bom-tree-section.is-purchase-tab .material-tree-table .ant-table-container) {
    display: none;
  }

  :deep(.bom-tree-section.is-purchase-tab .purchase-usage-table .ant-table-container) {
    display: block;
  }

  .section-heading {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    min-height: 64px;
    padding: 12px 18px;
    background: linear-gradient(90deg, #f8fbff 0%, #fff 72%);
    border-bottom: 1px solid #e9eff6;
  }

  .tree-title-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    min-height: 58px;
    padding: 12px 6px 4px;
  }

  .tree-title-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .section-eyebrow {
    margin-bottom: 2px;
    color: #6d7e94;
    font-size: 12px;
    letter-spacing: 0.08em;
  }

  .section-title {
    color: #1d2d44;
    font-size: 16px;
    font-weight: 600;
    line-height: 1.35;
  }

  .section-meta {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #6d7e94;
    font-size: 12px;
    white-space: nowrap;
  }

  :deep(.purchase-usage-table .ant-table-wrapper) {
    padding: 8px 12px 12px;
  }

  :deep(.ant-table-tbody > tr.material-bill-level-1 > td) {
    background-color: #f4f8ff;
  }

  :deep(.ant-table-tbody > tr.material-bill-level-2 > td) {
    background-color: #f7fbf8;
  }

  :deep(.ant-table-tbody > tr.material-bill-level-3 > td) {
    background-color: #fcfaf4;
  }

  :deep(.ant-table-tbody > tr.material-bill-level-4 > td) {
    background-color: #fbf8fc;
  }

  :deep(.ant-table-tbody > tr.material-bill-level-5 > td) {
    background-color: #fafafa;
  }

  @media screen and (max-width: 768px) {
    .section-heading,
    .tree-title-bar {
      align-items: flex-start;
      flex-direction: column;
      gap: 6px;
    }

    .section-meta,
    .tree-title-actions {
      align-items: flex-start;
      flex-wrap: wrap;
      white-space: normal;
    }
  }
</style>
