<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
<!--      <template #tableTitle>-->
<!--        <a-button type="primary" preIcon="ant-design:search-outlined" @click="handleSearch" style="margin-right: 5px">-->
<!--          查询-->
<!--        </a-button>-->
<!--      </template>-->
    </BasicTable>
  </div>
</template>

<script lang="ts">
  export default {
    name: 'ErpMaterialPrice',
  };
</script>

<script lang="ts" setup>
  import { onMounted } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { getMaterialPriceList } from './material-price.api';
  import { tableColumns, searchFormSchema, loadMaterialOptions, loadOrgOptions } from './material-price.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';

  const { createMessage } = useMessage();

  // 列表页面公共参数、方法
  const { tableContext } = useListPage({
    designScope: 'erp-material-price',
    tableProps: {
      // title: '物料供应商价格',
      api: getMaterialPriceList,
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

  const [registerTable, { reload, getForm }, { rowSelection }] = tableContext;

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
    reload();
  };

  // 初始化时加载默认物料
  onMounted(async () => {
    // 可以在这里加载一些默认的物料选项
    await loadMaterialOptions('');
    await loadOrgOptions('');
  });

</script>
