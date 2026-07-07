<template>
  <BasicTable @register="registerTable" />
</template>

<script lang="ts">
  export default {
    name: 'ErpProductionOrderList',
  };
</script>

<script lang="ts" setup>
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getProductionOrderList } from './production-order.api';
  import { searchFormSchema, tableColumns } from './production-order.data';

  const { tableContext } = useListPage({
    designScope: 'erp-production-order-list',
    tableProps: {
      api: getProductionOrderList,
      rowKey: 'lineEntryId',
      columns: tableColumns,
      bordered: true,
      scroll: { x: 3200 },
      showActionColumn: false,
      formConfig: {
        labelWidth: 90,
        schemas: searchFormSchema,
        fieldMapToTime: [
          ['planStartRange', ['planStartBegin', 'planStartEnd'], 'YYYY-MM-DD'],
          ['planFinishRange', ['planFinishBegin', 'planFinishEnd'], 'YYYY-MM-DD'],
        ],
      },
    },
  });

  const [registerTable] = tableContext;
</script>
