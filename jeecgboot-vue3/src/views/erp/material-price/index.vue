<template>
  <div>
    <BasicTable :key="tableKey" @register="registerTable" :rowSelection="rowSelection" @fetch-success="resetTrendChart">
      <template #tableTitle>
        <a-button type="primary" @click="handleOpenTrendChart">
          <Icon icon="ant-design:line-chart-outlined" />
          价格趋势
        </a-button>
      </template>
    </BasicTable>
    <a-modal v-model:open="trendChartVisible" title="价格趋势" width="92%" :footer="null" destroyOnClose wrapClassName="material-price-trend-modal">
      <LineMulti :chartData="trendChartData" :height="trendChartHeight" :option="trendChartOption" />
    </a-modal>
  </div>
</template>

<script lang="ts">
  export default {
    name: 'ErpMaterialPrice',
  };
</script>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { getMaterialPriceList } from './material-price.api';
  import { tableColumns, searchFormSchema, loadMaterialOptions, loadOrgOptions } from './material-price.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { Icon } from '/@/components/Icon';
  import LineMulti from '/@/components/chart/LineMulti.vue';

  const { createMessage } = useMessage();
  const tableKey = 'erp-material-price-table';
  const trendChartVisible = ref(false);
  const selectedTrendRows = ref<any[]>([]);
  const trendChartHeight = computed(() => {
    const legendRows = Math.ceil(selectedTrendRows.value.length / 3);
    const extraLegendHeight = Math.min(Math.max(legendRows - 1, 0), 4) * 24;
    return `${520 + extraLegendHeight}px`;
  });

  const trendChartOption = computed(() => {
    // 计算数据的最大最小值
    const values = selectedTrendRows.value
      .flatMap((row) => {
        const monthlyPrices = row?.monthlyPrices || {};
        return Object.values(monthlyPrices).map(Number);
      })
      .filter((v) => v > 0); // 过滤掉0值

    let min = 0;
    let max = 100;

    if (values.length > 0) {
      min = Math.min(...values);
      max = Math.max(...values);
      // 添加一些边距，避免数据点紧贴边界

      const padding = (max - min) * 0.1;
      if (padding > 0) {
        min = Math.max(0, Math.floor(min - padding));
        max = Math.ceil(max + padding);
      } else {
        // 当所有值相同时，添加默认边距
        max = max > 0 ? max * 1.2 : 100;
        min = 0;
      }
    }

    return {
      legend: {
        type: 'plain',
        top: 10,
        left: 16,
        right: 16,
        itemWidth: 14,
        itemHeight: 8,
        itemGap: 14,
        textStyle: {
          fontSize: 12,
          color: '#4b5563',
        },
      },
      grid: {
        top: Math.min(72 + Math.ceil(selectedTrendRows.value.length / 3) * 24, 168),
        left: 64,
        right: 36,
        bottom: 56,
        containLabel: true,
      },
      tooltip: {
        trigger: 'axis',
        confine: true,
        axisPointer: {
          type: 'line',
        },
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        axisTick: {
          alignWithLabel: true,
        },
      },
      yAxis: {
        type: 'value',
        name: '价格',
        min,
        max,
      },
    };
  });

  // 列表页面公共参数、方法
  const { tableContext } = useListPage({
    designScope: 'erp-material-price',
    tableProps: {
      // title: '物料供应商价格',
      api: getMaterialPriceList,
      rowKey: (record) => `${record.materialId || record.materialCode || ''}-${record.supplierId || ''}-${record.useOrgId || ''}`,
      columns: tableColumns,
      bordered: true,
      formConfig: {
        labelWidth: 120,
        schemas: searchFormSchema,
        fieldMapToTime: [],
      },
      // 查询后自动刷新表格
      onRefresh: (reset) => {
        if (!reset) {
          handleSearch();
        }
      },
    },
    exportConfig: {
      name: '物料供应商价格列表',
      url: '/erp/purchase/exportMaterialPrice',
    },
    importConfig: {
      url: '/erp/purchase/importMaterialPrice',
    },
  });

  const [registerTable, { reload, getForm, getSelectRows, clearSelectedRowKeys }, { rowSelection }] = tableContext;

  const trendChartData = computed(() => {
    return selectedTrendRows.value.flatMap((row) => {
      const monthlyPrices = row?.monthlyPrices || {};
      const type = buildTrendSeriesName(row);
      return Array.from({ length: 12 }, (_, index) => {
        const month = index + 1;
        return {
          name: `${month}月`,
          type,
          value: Number(monthlyPrices[month] ?? 0),
        };
      });
    });
  });

  /**
   * 操作列定义
   * @param record
   */
  const handleSearch = async () => {
    // 检查表单数据
    const formData = getForm().getFieldsValue();
    if (!formData.year) {
      createMessage.warning('请选择年份');
      return;
    }
    resetTrendChart();
    reload();
  };

  const resetTrendChart = () => {
    selectedTrendRows.value = [];
    trendChartVisible.value = false;
    clearSelectedRowKeys();
  };

  const handleOpenTrendChart = () => {
    const rows = getSelectRows();
    if (!rows || rows.length === 0) {
      createMessage.warning('请选择需要查看趋势的明细行');
      return;
    }
    selectedTrendRows.value = rows;
    trendChartVisible.value = true;
  };

  const buildTrendSeriesName = (row: any) => {
    const material = row?.materialCode || row?.materialName || '未知物料';
    const supplier = row?.supplierName || row?.supplierId || '无供应商';
    return `${material} / ${supplier}`;
  };

  // 初始化时加载默认物料
  onMounted(async () => {
    await loadMaterialOptions('');
    await loadOrgOptions('');
  });
</script>

<style lang="less">
  .material-price-trend-modal {
    .ant-modal {
      max-width: 1280px;
    }

    .ant-modal-body {
      max-height: calc(100vh - 160px);
      padding: 16px 20px 20px;
      overflow-y: auto;
    }
  }
</style>
