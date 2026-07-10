<template>
  <PageWrapper :contentFullHeight="isDesktopViewport" :fixedHeight="isDesktopViewport">
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
        :width="'94vw'"
        :footer="null"
        destroy-on-close
        wrap-class-name="sipm-preview-modal"
        @after-close="handlePreviewClose"
      >
        <div class="sipm-preview-modal__toolbar">
          <div class="sipm-preview-modal__name">{{ selectedRecord?.name || selectedRecord?.objNo || '图纸预览' }}</div>
          <div class="sipm-preview-modal__actions">
            <a-button size="small" :disabled="!canAdjustPreview" @click="zoomOutPreview">缩小</a-button>
            <a-button size="small" :disabled="!canAdjustPreview" @click="zoomInPreview">放大</a-button>
            <a-button size="small" :disabled="!canAdjustPreview" @click="fitPreview">适配</a-button>
            <a-button size="small" :disabled="!canAdjustPreview" @click="actualSizePreview">100%</a-button>
            <span class="sipm-preview-modal__scale">{{ Math.round(previewScale * 100) }}%</span>
            <a-button v-if="previewBlobUrl" type="link" size="small" @click="openPreview">新窗口打开</a-button>
          </div>
        </div>
        <div
          ref="previewViewportRef"
          class="sipm-preview-modal__body"
          :class="{ 'is-dragging': previewDragging }"
          @wheel.prevent="handlePreviewWheel"
          @mousedown="handlePreviewDragStart"
        >
          <a-spin v-if="previewLoading" tip="图纸加载中..." />
          <div v-show="!previewLoading && !previewError && previewMode" class="sipm-preview-modal__stage" :style="previewStageStyle">
            <canvas ref="previewCanvasRef" class="sipm-preview-modal__canvas"></canvas>
          </div>
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
  import * as pdfjsLib from 'pdfjs-dist';
  import pdfWorkerSource from 'pdfjs-dist/build/pdf.worker.mjs?raw';
  import { computed, nextTick, onUnmounted, reactive, ref } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { useWindowSizeFn } from '/@/hooks/event/useWindowSizeFn';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getSipmDrawingImageBlob, querySipmDrawingList, SipmDrawingListResult, SipmDrawingRecord, SipmPartRecord } from './sipm-drawing.api';

  const pdfWorkerBlobUrl = typeof window === 'undefined' ? '' : URL.createObjectURL(new Blob([pdfWorkerSource], { type: 'text/javascript' }));
  if (pdfWorkerBlobUrl) {
    pdfjsLib.GlobalWorkerOptions.workerSrc = pdfWorkerBlobUrl;
  }

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
  const previewViewportRef = ref<HTMLDivElement | null>(null);
  const previewScale = ref(1);
  const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth);
  const previewOffset = reactive({ x: 0, y: 0 });
  const previewBaseSize = reactive({ width: 0, height: 0 });
  const previewDragging = ref(false);
  const previewDragStart = reactive({ x: 0, y: 0, offsetX: 0, offsetY: 0 });

  const total = computed(() => records.value.length);
  const isDesktopViewport = computed(() => viewportWidth.value > 1024);
  const previewTitle = computed(() => `图纸预览${selectedRecord.value?.objNo ? ` - ${selectedRecord.value.objNo}` : ''}`);
  const canAdjustPreview = computed(() => !previewLoading.value && !previewError.value && !!previewMode.value);
  const previewStageStyle = computed(() => ({
    width: `${previewBaseSize.width}px`,
    height: `${previewBaseSize.height}px`,
    transform: `translate(-50%, -50%) translate(${previewOffset.x}px, ${previewOffset.y}px) scale(${previewScale.value})`,
  }));
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
    y: isDesktopViewport.value ? 'calc(100vh - 420px)' : undefined,
  }));

  useWindowSizeFn(
    () => {
      viewportWidth.value = window.innerWidth;
    },
    120,
    { immediate: true }
  );

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
    resetPreviewTransform();
    await nextTick();
    try {
      await waitForPreviewCanvas();
      const response = await getSipmDrawingImageBlob(record.objId);
      const blob = response.data;
      if (!blob || blob.size === 0) {
        previewError.value = '未获取到图纸文件流';
        return;
      }
      if (await isPdfBlob(blob)) {
        resetPreviewBlob();
        const pdfBlob = toTypedPdfBlob(blob);
        previewBlobUrl.value = URL.createObjectURL(pdfBlob);
        previewMode.value = 'pdf';
        await renderPdfToCanvas(pdfBlob);
        return;
      }
      resetPreviewBlob();
      previewBlobUrl.value = URL.createObjectURL(blob);
      previewMode.value = 'image';
      await renderBlobToCanvas(previewBlobUrl.value);
    } catch (error) {
      console.error('[SipmDrawingPreview] render failed', error);
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
    resetPreviewTransform();
  }

  function resetPreviewBlob() {
    if (previewBlobUrl.value) {
      URL.revokeObjectURL(previewBlobUrl.value);
      previewBlobUrl.value = '';
    }
    previewMode.value = '';
  }

  function resetPreviewTransform() {
    previewScale.value = 1;
    previewOffset.x = 0;
    previewOffset.y = 0;
    previewBaseSize.width = 0;
    previewBaseSize.height = 0;
    previewDragging.value = false;
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

  async function waitForPreviewCanvas() {
    for (let i = 0; i < 5; i++) {
      await nextTick();
      if (previewCanvasRef.value && previewViewportRef.value) {
        return;
      }
      await new Promise((resolve) => window.setTimeout(resolve, 30));
    }
    throw new Error('preview canvas unavailable');
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
        setCanvasDisplaySize(canvas, image.naturalWidth, image.naturalHeight);
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

  async function renderPdfToCanvas(blob: Blob) {
    const canvas = previewCanvasRef.value;
    const context = canvas?.getContext('2d');
    if (!canvas || !context) {
      throw new Error('canvas unavailable');
    }

    const data = new Uint8Array(await blob.arrayBuffer());
    const pdf = await loadPdfDocument(data);
    try {
      const page = await pdf.getPage(1);
      const previewBody = previewViewportRef.value;
      const baseViewport = page.getViewport({ scale: 1 });
      const availableWidth = Math.max((previewBody?.clientWidth || baseViewport.width) - 24, 1);
      const availableHeight = Math.max((previewBody?.clientHeight || baseViewport.height) - 24, 1);
      const scale = Math.min(availableWidth / baseViewport.width, availableHeight / baseViewport.height);
      const pixelRatio = window.devicePixelRatio || 1;
      const viewport = page.getViewport({ scale: scale * pixelRatio });

      canvas.width = Math.floor(viewport.width);
      canvas.height = Math.floor(viewport.height);
      previewBaseSize.width = Math.floor(baseViewport.width * scale);
      previewBaseSize.height = Math.floor(baseViewport.height * scale);
      canvas.style.width = '100%';
      canvas.style.height = '100%';
      previewScale.value = 1;
      previewOffset.x = 0;
      previewOffset.y = 0;
      context.clearRect(0, 0, canvas.width, canvas.height);
      await page.render({ canvasContext: context, viewport }).promise;
    } finally {
      pdf.destroy();
    }
  }

  async function loadPdfDocument(data: Uint8Array) {
    return await pdfjsLib.getDocument({ data: data.slice() }).promise;
  }

  function setCanvasDisplaySize(canvas: HTMLCanvasElement, width: number, height: number) {
    canvas.width = width;
    canvas.height = height;
    const previewBody = previewViewportRef.value;
    const availableWidth = Math.max((previewBody?.clientWidth || width) - 24, 1);
    const availableHeight = Math.max((previewBody?.clientHeight || height) - 24, 1);
    const scale = Math.min(availableWidth / width, availableHeight / height, 1);
    previewBaseSize.width = Math.floor(width * scale);
    previewBaseSize.height = Math.floor(height * scale);
    canvas.style.width = '100%';
    canvas.style.height = '100%';
    previewScale.value = 1;
    previewOffset.x = 0;
    previewOffset.y = 0;
  }

  function zoomInPreview() {
    updatePreviewScale(previewScale.value * 1.2);
  }

  function zoomOutPreview() {
    updatePreviewScale(previewScale.value / 1.2);
  }

  function fitPreview() {
    previewScale.value = 1;
    previewOffset.x = 0;
    previewOffset.y = 0;
  }

  function actualSizePreview() {
    const canvas = previewCanvasRef.value;
    if (!canvas || !previewBaseSize.width || !previewBaseSize.height) {
      return;
    }
    previewScale.value = clampScale(Math.min(canvas.width / previewBaseSize.width, canvas.height / previewBaseSize.height));
    previewOffset.x = 0;
    previewOffset.y = 0;
  }

  function updatePreviewScale(scale: number) {
    previewScale.value = clampScale(scale);
  }

  function clampScale(scale: number) {
    return Math.min(Math.max(scale, 0.2), 8);
  }

  function handlePreviewWheel(event: WheelEvent) {
    if (!canAdjustPreview.value) {
      return;
    }
    const nextScale = previewScale.value * (event.deltaY > 0 ? 0.9 : 1.1);
    updatePreviewScale(nextScale);
  }

  function handlePreviewDragStart(event: MouseEvent) {
    if (!canAdjustPreview.value || event.button !== 0) {
      return;
    }
    previewDragging.value = true;
    previewDragStart.x = event.clientX;
    previewDragStart.y = event.clientY;
    previewDragStart.offsetX = previewOffset.x;
    previewDragStart.offsetY = previewOffset.y;
    window.addEventListener('mousemove', handlePreviewDragMove);
    window.addEventListener('mouseup', handlePreviewDragEnd);
  }

  function handlePreviewDragMove(event: MouseEvent) {
    if (!previewDragging.value) {
      return;
    }
    previewOffset.x = previewDragStart.offsetX + event.clientX - previewDragStart.x;
    previewOffset.y = previewDragStart.offsetY + event.clientY - previewDragStart.y;
  }

  function handlePreviewDragEnd() {
    previewDragging.value = false;
    window.removeEventListener('mousemove', handlePreviewDragMove);
    window.removeEventListener('mouseup', handlePreviewDragEnd);
  }

  onUnmounted(() => {
    handlePreviewDragEnd();
    resetPreviewBlob();
  });

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
      linear-gradient(90deg, rgba(35, 89, 119, 0.05) 1px, transparent 1px), linear-gradient(0deg, rgba(35, 89, 119, 0.05) 1px, transparent 1px),
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
    gap: 12px;
    min-height: 36px;
    padding: 0 2px 8px;
    color: #64748b;
  }

  :deep(.sipm-preview-modal .ant-modal) {
    top: 24px;
    max-width: calc(100vw - 32px);
    padding-bottom: 0;
  }

  :deep(.sipm-preview-modal .ant-modal-content) {
    height: calc(100vh - 48px);
    display: flex;
    flex-direction: column;
  }

  :deep(.sipm-preview-modal .ant-modal-body) {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-height: 0;
    padding: 10px 16px 16px;
  }

  .sipm-preview-modal__name {
    min-height: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .sipm-preview-modal__actions {
    display: flex;
    align-items: center;
    flex: none;
    gap: 6px;
    white-space: nowrap;

    :deep(.ant-btn) {
      min-width: 48px;
    }
  }

  .sipm-preview-modal__scale {
    min-width: 44px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
    font-size: 12px;
    color: #475569;
    text-align: center;
  }

  .sipm-preview-modal__body {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    flex: none;
    height: max(560px, calc(100vh - 150px));
    min-height: 560px;
    overflow: hidden;
    border: 1px solid #cbd8df;
    border-radius: 4px;
    cursor: grab;
    user-select: none;
    background:
      linear-gradient(45deg, #eef3f6 25%, transparent 25%), linear-gradient(-45deg, #eef3f6 25%, transparent 25%),
      linear-gradient(45deg, transparent 75%, #eef3f6 75%), linear-gradient(-45deg, transparent 75%, #eef3f6 75%), #f8fafc;
    background-position:
      0 0,
      0 8px,
      8px -8px,
      -8px 0;
    background-size: 16px 16px;
  }

  .sipm-preview-modal__body.is-dragging {
    cursor: grabbing;
  }

  .sipm-preview-modal__stage {
    position: absolute;
    top: 50%;
    left: 50%;
    box-shadow: 0 14px 34px rgba(15, 23, 42, 0.22);
    transform-origin: center center;
    will-change: transform;
  }

  .sipm-preview-modal__canvas {
    display: block;
    width: 100%;
    height: 100%;
    background: #fff;
  }

  :deep(.sipm-row-selected td) {
    background: #e7f4f8 !important;
  }

  @media (max-width: 1200px) {
    .sipm-page {
      padding: 12px;
    }

    .sipm-search {
      gap: 14px;
    }

    .sipm-search__main {
      flex: 1;
    }

    .sipm-search__input {
      width: min(360px, 42vw);
    }

    .sipm-search__meta {
      gap: 12px;
    }

    .sipm-content {
      min-height: 0;
    }
  }

  @media (max-width: 1024px) {
    .sipm-page {
      height: auto;
      min-height: 100%;
      overflow: visible;
    }

    .sipm-search {
      align-items: flex-start;
      min-height: auto;
    }

    .sipm-search__main {
      align-items: stretch;
      flex-direction: column;
      gap: 10px;
    }

    .sipm-search__input {
      width: 100%;
      margin-left: 0;
    }

    .sipm-search__meta {
      align-self: stretch;
      justify-content: flex-end;
      padding-top: 2px;
    }

    .sipm-part {
      padding: 12px 14px 0;
    }

    .sipm-part__plate {
      align-items: flex-start;
    }

    .sipm-part__name {
      max-width: 50%;
    }

    .sipm-content {
      flex: none;
      min-height: auto;
      overflow: visible;
    }

    .sipm-table {
      flex: none;
      overflow: visible;
    }

    .sipm-table :deep(.ant-table-wrapper),
    .sipm-table :deep(.ant-spin-nested-loading),
    .sipm-table :deep(.ant-spin-container) {
      min-width: 0;
    }

    .sipm-preview-modal__toolbar {
      align-items: flex-start;
    }

    .sipm-preview-modal__actions {
      flex-wrap: wrap;
      justify-content: flex-end;
      white-space: normal;
    }

    .sipm-preview-modal__body {
      height: max(480px, calc(100vh - 168px));
      min-height: 480px;
    }
  }

  @media (max-width: 720px) {
    .sipm-page {
      height: auto;
      min-height: 100%;
      padding: 8px;
      overflow: auto;
      background-size: 22px 22px;
    }

    .sipm-search {
      align-items: flex-start;
      flex-direction: column;
      gap: 10px;
      padding: 12px;
      margin-bottom: 8px;
      border-left-width: 3px;
    }

    .sipm-search__main {
      align-items: stretch;
      flex-direction: column;
      width: 100%;
      gap: 8px;
    }

    .sipm-search__title {
      margin-right: 0;
      font-size: 17px;
    }

    .sipm-search__subtitle {
      line-height: 1.35;
    }

    .sipm-search__input {
      width: 100%;
      margin-left: 0;
    }

    .sipm-search__meta {
      width: 100%;
      flex-wrap: wrap;
      gap: 8px 14px;
      justify-content: space-between;
    }

    .sipm-search__count {
      margin-right: -8px;
      font-size: 24px;
    }

    .sipm-search__current {
      width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .sipm-part {
      padding: 12px 12px 0;
      margin-bottom: 8px;
    }

    .sipm-part::before {
      width: 86px;
      opacity: 0.7;
    }

    .sipm-part__plate {
      align-items: flex-start;
      flex-direction: column;
      gap: 6px;
      padding-bottom: 10px;
      margin-bottom: 10px;
    }

    .sipm-part__code {
      max-width: 100%;
      overflow-wrap: anywhere;
      font-size: 19px;
    }

    .sipm-part__name {
      max-width: 100%;
      text-align: left;
      white-space: normal;
    }

    .sipm-part__form {
      :deep(.ant-form-item) {
        margin-bottom: 10px;
      }
    }

    .sipm-readonly-field {
      min-height: 30px;
      white-space: normal;
      overflow-wrap: anywhere;
    }

    .sipm-content__header {
      padding: 10px 12px;
    }

    .sipm-content__subtitle {
      line-height: 1.35;
    }

    .sipm-table :deep(.ant-table-pagination.ant-pagination) {
      margin: 10px 8px;
      row-gap: 8px;
    }

    :deep(.sipm-preview-modal .ant-modal) {
      top: 8px;
      max-width: calc(100vw - 16px);
      margin: 0 auto;
    }

    :deep(.sipm-preview-modal .ant-modal-content) {
      height: calc(100dvh - 16px);
    }

    :deep(.sipm-preview-modal .ant-modal-body) {
      padding: 8px 10px 10px;
    }

    .sipm-preview-modal__toolbar {
      align-items: stretch;
      flex-direction: column;
      gap: 8px;
    }

    .sipm-preview-modal__name {
      width: 100%;
    }

    .sipm-preview-modal__actions {
      justify-content: flex-start;
      gap: 6px;

      :deep(.ant-btn) {
        min-width: 44px;
        padding-inline: 8px;
      }
    }

    .sipm-preview-modal__body {
      height: calc(100dvh - 190px);
      min-height: 320px;
      touch-action: none;
    }

    .sipm-preview-modal__stage {
      max-width: calc(100vw - 40px);
      max-height: calc(100dvh - 220px);
    }
  }
</style>
