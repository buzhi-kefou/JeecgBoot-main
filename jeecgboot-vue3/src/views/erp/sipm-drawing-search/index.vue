<template>
  <PageWrapper contentFullHeight fixedHeight>
    <div class="sipm-page">
      <section class="sipm-search">
        <div class="sipm-search__main">
          <div class="sipm-search__heading">
            <div class="sipm-search__title">PLM图纸检索</div>
            <div class="sipm-search__subtitle">按物料编码定位部件与关联图纸</div>
          </div>
          <a-input-search
            v-model:value="searchNo"
            class="sipm-search__input"
            placeholder="请输入物料编码"
            enter-button="搜索"
            allow-clear
            :loading="loading"
            @search="handleSearch"
          />
        </div>
        <div class="sipm-search__meta">
          <span class="sipm-search__count">{{ total }}</span>
          <span class="sipm-search__label">张图纸</span>
          <span v-if="selectedRecord?.objNo" class="sipm-search__current">当前：{{ selectedRecord.objNo }}</span>
        </div>
      </section>

      <section class="sipm-part">
        <div class="sipm-part__plate">
          <div>
            <div class="sipm-part__eyebrow">Part record</div>
            <div class="sipm-part__code">{{ partForm.objNo || '等待查询' }}</div>
          </div>
          <div class="sipm-part__name">{{ partForm.name || '输入物料编码后显示物料信息' }}</div>
        </div>
        <a-form class="sipm-part__form" layout="vertical" :model="partForm">
          <a-row :gutter="12">
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="物料编码">
                <div class="sipm-readonly-field">{{ partForm.objNo || '-' }}</div>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="物料名称">
                <div class="sipm-readonly-field">{{ partForm.name || '-' }}</div>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="图号">
                <div class="sipm-readonly-field sipm-readonly-field--strong">{{ partForm.th || '-' }}</div>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="版本">
                <div class="sipm-readonly-field">{{ partForm.ver || '-' }}</div>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="创建时间">
                <div class="sipm-readonly-field">{{ partForm.ctimestr || '-' }}</div>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="修改时间">
                <div class="sipm-readonly-field">{{ partForm.mtimestr || '-' }}</div>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="创建人">
                <div class="sipm-readonly-field">{{ partForm.creator || '-' }}</div>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :sm="12" :md="8" :lg="6">
              <a-form-item label="修改人">
                <div class="sipm-readonly-field">{{ partForm.modifier || '-' }}</div>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </section>

      <section class="sipm-content">
        <div class="sipm-content__header">
          <div>
            <div class="sipm-content__title">关联图纸</div>
            <div class="sipm-content__subtitle">选择一行右侧预览，预览结果将在弹窗中打开</div>
          </div>
        </div>
        <div class="sipm-table">
          <a-table
            row-key="objId"
            size="small"
            bordered
            :columns="columns"
            :data-source="records"
            :loading="loading"
            :pagination="{ pageSize: 20, showSizeChanger: true, showTotal: (count) => `共 ${count} 条` }"
            :scroll="tableScroll"
            :row-class-name="getRowClassName"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'preview'">
                <a-button type="link" size="small" :disabled="!record.objId" @click="handlePreview(record)">预览</a-button>
              </template>
            </template>
          </a-table>
        </div>
      </section>

      <a-modal
        v-model:open="previewOpen"
        :title="previewTitle"
        :width="'88%'"
        :height="'88%'"
        :footer="null"
        destroy-on-close
        wrap-class-name="sipm-preview-modal"
        @after-close="handlePreviewClose"
      >
        <div class="sipm-preview-modal__toolbar">
          <div class="sipm-preview-modal__name">{{ selectedRecord?.name || selectedRecord?.objNo || '图纸预览' }}</div>
          <a-button v-if="previewBlobUrl" type="link" size="small" @click="openPreview">新窗口打开</a-button>
        </div>
        <div class="sipm-preview-modal__body">
          <a-spin v-if="previewLoading" tip="图纸加载中..." />
          <iframe
            v-if="!previewLoading && !previewError && previewMode === 'pdf'"
            class="sipm-preview-modal__frame"
            :src="previewBlobUrl"
          ></iframe>
          <canvas
            v-show="!previewLoading && !previewError && previewMode === 'image'"
            ref="previewCanvasRef"
            class="sipm-preview-modal__canvas"
          ></canvas>
          <a-empty v-if="!previewLoading && previewError" :description="previewError" />
        </div>
      </a-modal>
    </div>
  </PageWrapper>
</template>

<script lang="ts">
  export default {
    name: 'ErpSipmDrawingSearch',
  };
</script>

<script lang="ts" setup>
  import { computed, nextTick, ref } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getSipmDrawingImageBlob, querySipmDrawingList, SipmDrawingListResult, SipmDrawingRecord, SipmPartRecord } from './sipm-drawing.api';

  const { createMessage } = useMessage();

  const searchNo = ref(null);
  const loading = ref(false);
  const records = ref<SipmDrawingRecord[]>([]);
  const partInfo = ref<SipmPartRecord | null>(null);
  const selectedRecord = ref<SipmDrawingRecord | null>(null);
  const previewBlobUrl = ref('');
  const previewError = ref('');
  const previewLoading = ref(false);
  const previewMode = ref<'pdf' | 'image' | ''>('');
  const previewOpen = ref(false);
  const previewCanvasRef = ref<HTMLCanvasElement | null>(null);

  const total = computed(() => records.value.length);
  const previewTitle = computed(() => `图纸预览${selectedRecord.value?.objNo ? ` - ${selectedRecord.value.objNo}` : ''}`);
  const partForm = computed(() => ({
    objNo: partInfo.value?.objNo || '',
    name: partInfo.value?.name || '',
    th: partInfo.value?.extra?.TH || '',
    ver: partInfo.value?.ver || '',
    ctimestr: partInfo.value?.ctimestr || '',
    mtimestr: partInfo.value?.mtimestr || '',
    creator: partInfo.value?.creator || '',
    modifier: partInfo.value?.modifier || '',
  }));

  const columns = [
    { title: '图号', dataIndex: 'objNo', width: 170 },
    { title: '图纸名称', dataIndex: 'name', width: 140 },
    { title: '文件名', dataIndex: 'fname', width: 220, ellipsis: true },
    { title: '后缀', dataIndex: 'suffix', width: 80, align: 'center' },
    { title: '文件大小', dataIndex: 'fsizeStr', width: 100 },
    { title: '创建时间', dataIndex: 'ctimestr', width: 150 },
    { title: '修改时间', dataIndex: 'mtimestr', width: 150 },
    { title: '创建人', dataIndex: 'creator', width: 180, ellipsis: true },
    { title: '修改人', dataIndex: 'modifier', width: 180, ellipsis: true },
    { title: '预览', dataIndex: 'preview', width: 80, fixed: 'right', align: 'center' },
  ];

  const tableScroll = computed(() => ({
    x: columns.reduce((sum, column) => sum + getColumnWidth(column.width), 0),
    y: 'calc(100vh - 420px)',
  }));

  function getColumnWidth(width?: number | string) {
    if (typeof width === 'number') {
      return width;
    }
    if (typeof width === 'string') {
      const value = Number.parseInt(width, 10);
      return Number.isNaN(value) ? 120 : value;
    }
    return 120;
  }

  async function handleSearch(value?: string) {
    const no = (value || searchNo.value || '').trim();
    if (!no) {
      createMessage.warning('请输入物料编码');
      return;
    }
    loading.value = true;
    resetPreviewBlob();
    previewError.value = '';
    partInfo.value = null;
    selectedRecord.value = null;
    try {
      const result = await querySipmDrawingList({ no, start: 0, size: -1 });
      const listResult = normalizeListResult(result);
      partInfo.value = listResult.part || null;
      records.value = listResult.drawings || listResult.list || [];
      if (listResult.errcode && listResult.errcode !== 0) {
        createMessage.warning(listResult.errmsg || '查询失败');
      } else if (!records.value.length) {
        createMessage.info('未查询到图纸记录');
      }
    } finally {
      loading.value = false;
    }
  }

  function normalizeListResult(result: SipmDrawingListResult | SipmDrawingRecord[] | any): SipmDrawingListResult {
    if (Array.isArray(result)) {
      return { list: result, total: result.length, errcode: 0 };
    }
    if (Array.isArray(result?.drawings)) {
      return result;
    }
    if (Array.isArray(result?.list)) {
      return result;
    }
    if (Array.isArray(result?.result?.drawings)) {
      return result.result;
    }
    if (Array.isArray(result?.result?.list)) {
      return result.result;
    }
    return { list: [], total: 0, errcode: result?.errcode, errmsg: result?.errmsg };
  }

  async function handlePreview(record: SipmDrawingRecord) {
    if (!record.objId) {
      createMessage.warning('当前记录缺少对象ID');
      return;
    }
    selectedRecord.value = record;
    previewOpen.value = true;
    previewLoading.value = true;
    previewError.value = '';
    await nextTick();
    try {
      const response = await getSipmDrawingImageBlob(record.objId);
      const blob = response.data;
      if (!blob || blob.size === 0) {
        previewError.value = '未获取到图纸文件流';
        return;
      }
      if (await isPdfBlob(blob)) {
        resetPreviewBlob();
        previewBlobUrl.value = URL.createObjectURL(toTypedPdfBlob(blob));
        previewMode.value = 'pdf';
        return;
      }
      resetPreviewBlob();
      previewBlobUrl.value = URL.createObjectURL(blob);
      previewMode.value = 'image';
      await renderBlobToCanvas(previewBlobUrl.value);
    } catch (error) {
      previewError.value = '图纸加载失败，当前文件流不是浏览器可直接渲染的图片或PDF';
    } finally {
      previewLoading.value = false;
    }
  }

  function openPreview() {
    if (previewBlobUrl.value) {
      window.open(previewBlobUrl.value, '_blank');
    }
  }

  function handlePreviewClose() {
    resetPreviewBlob();
    previewError.value = '';
  }

  function resetPreviewBlob() {
    if (previewBlobUrl.value) {
      URL.revokeObjectURL(previewBlobUrl.value);
      previewBlobUrl.value = '';
    }
    previewMode.value = '';
  }

  async function isPdfBlob(blob: Blob) {
    if (blob.type?.toLowerCase().includes('pdf')) {
      return true;
    }
    const header = await blob.slice(0, 5).text();
    return header === '%PDF-';
  }

  function toTypedPdfBlob(blob: Blob) {
    return blob.type?.toLowerCase().includes('pdf') ? blob : new Blob([blob], { type: 'application/pdf' });
  }

  function renderBlobToCanvas(url: string) {
    return new Promise<void>((resolve, reject) => {
      const canvas = previewCanvasRef.value;
      const context = canvas?.getContext('2d');
      if (!canvas || !context) {
        reject(new Error('canvas unavailable'));
        return;
      }
      const image = new Image();
      image.onload = () => {
        canvas.width = image.naturalWidth;
        canvas.height = image.naturalHeight;
        context.clearRect(0, 0, canvas.width, canvas.height);
        context.drawImage(image, 0, 0);
        resolve();
      };
      image.onerror = () => {
        reject(new Error('image decode failed'));
      };
      image.src = url;
    });
  }

  function getRowClassName(record: SipmDrawingRecord) {
    return record.objId && selectedRecord.value?.objId === record.objId ? 'sipm-row-selected' : '';
  }
</script>

<style lang="less" scoped>
  .sipm-page {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    padding: 14px;
    overflow: hidden;
    color: #172033;
    background:
      linear-gradient(90deg, rgba(35, 89, 119, 0.05) 1px, transparent 1px),
      linear-gradient(0deg, rgba(35, 89, 119, 0.05) 1px, transparent 1px),
      #f4f7f8;
    background-size: 28px 28px;
  }

  .sipm-search {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 74px;
    padding: 14px 18px;
    margin-bottom: 12px;
    flex: none;
    background: #fbfcfd;
    border: 1px solid #cbd8df;
    border-left: 4px solid #1f6f8b;
    border-radius: 6px;
    box-shadow: 0 10px 24px rgba(26, 52, 69, 0.08);
  }

  .sipm-part {
    position: relative;
    flex: none;
    padding: 14px 16px 2px;
    margin-bottom: 12px;
    overflow: hidden;
    background: #fbfcfd;
    border: 1px solid #cbd8df;
    border-radius: 6px;
    box-shadow: 0 10px 24px rgba(26, 52, 69, 0.08);
  }

  .sipm-part::before {
    position: absolute;
    top: 0;
    right: 0;
    width: 168px;
    height: 100%;
    pointer-events: none;
    content: '';
    background:
      linear-gradient(135deg, transparent 0 44%, rgba(31, 111, 139, 0.12) 44% 46%, transparent 46%),
      linear-gradient(45deg, transparent 0 58%, rgba(199, 123, 39, 0.16) 58% 60%, transparent 60%);
  }

  .sipm-part__plate {
    position: relative;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 16px;
    padding-bottom: 12px;
    margin-bottom: 12px;
    border-bottom: 1px dashed #b7c8d1;
  }

  .sipm-part__eyebrow {
    margin-bottom: 2px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
    font-size: 11px;
    font-weight: 700;
    color: #647987;
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  .sipm-part__code {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
    font-size: 22px;
    font-weight: 800;
    line-height: 1.2;
    color: #102736;
  }

  .sipm-part__name {
    max-width: 42%;
    overflow: hidden;
    font-size: 15px;
    font-weight: 600;
    color: #405261;
    text-align: right;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sipm-part__form {
    :deep(.ant-form-item) {
      margin-bottom: 12px;
    }

    :deep(.ant-form-item-label > label) {
      height: 20px;
      font-size: 12px;
      color: #657a88;
    }
  }

  .sipm-readonly-field {
    display: flex;
    align-items: center;
    min-height: 32px;
    padding: 5px 9px;
    overflow: hidden;
    font-size: 13px;
    color: #172033;
    text-overflow: ellipsis;
    white-space: nowrap;
    background: #f5f8fa;
    border: 1px solid #d8e2e8;
    border-radius: 4px;
  }

  .sipm-readonly-field--strong {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
    font-weight: 700;
    color: #13566d;
    background: #edf7fa;
    border-color: #b9d9e4;
  }

  .sipm-search__main {
    display: flex;
    align-items: center;
    min-width: 0;
  }

  .sipm-search__title {
    flex: none;
    font-size: 18px;
    font-weight: 800;
    line-height: 1.25;
    color: #102736;
  }

  .sipm-search__subtitle {
    margin-top: 2px;
    font-size: 12px;
    color: #667b88;
  }

  .sipm-search__input {
    width: 380px;
    margin-left: 18px;

    :deep(.ant-input) {
      height: 36px;
      border-color: #b8cbd5;
    }

    :deep(.ant-input-search-button) {
      height: 36px;
      background: #1f6f8b;
      border-color: #1f6f8b;
      box-shadow: none;
    }
  }

  .sipm-search__meta {
    display: flex;
    align-items: baseline;
    gap: 16px;
    color: #647987;
    white-space: nowrap;
  }

  .sipm-search__count {
    margin-right: -12px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
    font-size: 28px;
    font-weight: 800;
    line-height: 1;
    color: #c77b27;
  }

  .sipm-search__label,
  .sipm-search__current {
    font-size: 12px;
  }

  .sipm-content {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    overflow: hidden;
    background: #fbfcfd;
    border: 1px solid #cbd8df;
    border-radius: 6px;
    box-shadow: 0 10px 24px rgba(26, 52, 69, 0.08);
  }

  .sipm-content__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex: none;
    padding: 12px 14px;
    border-bottom: 1px solid #d9e4ea;
  }

  .sipm-content__title {
    font-size: 15px;
    font-weight: 800;
    color: #102736;
  }

  .sipm-content__subtitle {
    margin-top: 2px;
    font-size: 12px;
    color: #6a7f8d;
  }

  .sipm-table {
    flex: 1;
    min-width: 0;
    min-height: 0;
    overflow: hidden;
    padding: 0;
    background: #fbfcfd;
  }

  .sipm-table :deep(.ant-table) {
    color: #172033;
    background: #fbfcfd;
  }

  .sipm-table :deep(.ant-table-thead > tr > th) {
    font-size: 12px;
    font-weight: 700;
    color: #465c6a;
    background: #edf3f6;
    border-color: #d7e3e9;
  }

  .sipm-table :deep(.ant-table-tbody > tr > td) {
    border-color: #e4edf1;
  }

  .sipm-table :deep(.ant-table-tbody > tr:hover > td) {
    background: #f0f8fb;
  }

  .sipm-table :deep(.ant-table-cell-fix-right) {
    box-shadow: -8px 0 16px rgba(23, 32, 51, 0.06);
  }

  .sipm-table :deep(.ant-btn-link) {
    color: #1f6f8b;
    font-weight: 700;
  }

  .sipm-ellipsis {
    display: inline-block;
    max-width: 210px;
    overflow: hidden;
    text-overflow: ellipsis;
    vertical-align: bottom;
    white-space: nowrap;
  }

  .sipm-preview-modal__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 36px;
    padding: 0 2px 8px;
    color: #64748b;
  }

  .sipm-preview-modal__name {
    min-height: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sipm-preview-modal__body {
    display: flex;
    align-items: center;
    justify-content: center;
    height: calc(82vh - 120px);
    min-height: 520px;
    overflow: hidden;
    border: 1px solid #cbd8df;
    border-radius: 4px;
  }

  .sipm-preview-modal__canvas {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }

  .sipm-preview-modal__frame {
    width: 100%;
    height: 100%;
    border: 0;
  }

  :deep(.sipm-row-selected td) {
    background: #e7f4f8 !important;
  }

  @media (max-width: 1200px) {
    .sipm-content {
      min-height: 0;
    }
  }

  @media (max-width: 720px) {
    .sipm-search {
      align-items: flex-start;
      flex-direction: column;
      gap: 10px;
    }

    .sipm-search__main {
      align-items: stretch;
      flex-direction: column;
      width: 100%;
      gap: 8px;
    }

    .sipm-search__title {
      margin-right: 0;
    }

    .sipm-search__input {
      width: 100%;
      margin-left: 0;
    }

    .sipm-search__meta {
      width: 100%;
      justify-content: space-between;
    }

    .sipm-part__plate {
      align-items: flex-start;
      flex-direction: column;
      gap: 6px;
    }

    .sipm-part__name {
      max-width: 100%;
      text-align: left;
    }
  }
</style>
