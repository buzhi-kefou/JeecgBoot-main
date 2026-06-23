import { readFileSync } from 'fs';
import { resolve } from 'path';

const viewRoot = resolve(__dirname, '../src/views/erp/material-price');

describe('material-price page wiring', () => {
  test('uses table methods, not the register callback, to read search form values', () => {
    const source = readFileSync(resolve(viewRoot, 'index.vue'), 'utf8');

    expect(source).toContain('{ reload, getForm }');
    expect(source).toContain('const formData = getForm().getFieldsValue();');
    expect(source).not.toContain('registerTable.getForm()');
  });

  test('posts material query parameters in the request body', () => {
    const source = readFileSync(resolve(viewRoot, 'material-price.api.ts'), 'utf8');

    expect(source).toContain('defHttp.post({ url: Api.list, data: params })');
    expect(source).toContain('defHttp.post({ url: Api.getMaterialCodeList, data: params })');
  });
});
