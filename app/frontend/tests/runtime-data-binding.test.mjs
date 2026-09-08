import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import test from 'node:test';
import vm from 'node:vm';

const source = readFileSync(new URL('../../../plm/3dspace/common/JFLowCode/runtime.js', import.meta.url), 'utf8');
const sandbox = {window: {}, URLSearchParams};
vm.runInNewContext(source, sandbox);
const {compilePackage, parseSearchTarget, applySearchResult} = sandbox.window.JFLowCodeRuntime;

test('页面初始化绑定为Service注入受控查询API且不修改原Schema', () => {
  const pagePackage = {
    schema: {
      type: 'page',
      body: [{id: 'u:init-service', type: 'service', api: '/old-api', body: []}]
    },
    plmConfig: {
      dataBindings: [{componentId: 'u:init-service', trigger: 'INIT', actionCode: 'QUERY_HEADER'}]
    }
  };
  const compiled = compilePackage(pagePackage);
  assert.equal(compiled.body[0].api.method, 'post');
  assert.equal(compiled.body[0].api.url, 'plm://QUERY_HEADER?componentId=u%3Ainit-service');
  assert.equal(pagePackage.schema.body[0].api, '/old-api');
});

test('初始化绑定不会注入到非Service组件', () => {
  const compiled = compilePackage({
    schema: {type: 'page', body: [{id: 'u:not-service', type: 'container'}]},
    plmConfig: {dataBindings: [{componentId: 'u:not-service', trigger: 'INIT', actionCode: 'QUERY_HEADER'}]}
  });
  assert.equal(compiled.body[0].api, undefined);
});

test('PLM Range字段绑定自动注入动态选项接口且移除静态选项', () => {
  const pagePackage = {
    schema: {type: 'page', body: [{id: 'u:plant', type: 'select', options: [{label: '旧值', value: 'OLD'}]}]},
    resources: {fields: [{fieldCode: 'AFFECTED_PLANT', rangeSource: 'PLM_RANGE', rangeConfig: {attributeName: 'JFAffectsFactory'}}]},
    plmConfig: {fieldBindings: [{componentId: 'u:plant', fieldCode: 'AFFECTED_PLANT', valueKey: 'affectedPlant'}]}
  };
  const compiled = compilePackage(pagePackage);
  assert.equal(compiled.body[0].name, 'affectedPlant');
  assert.equal(compiled.body[0].options, undefined);
  assert.equal(compiled.body[0].source.url, 'plm://QUERY_ATTRIBUTE_RANGE?componentId=u%3Aplant');
  assert.deepEqual({...compiled.body[0].source.data}, {fieldCode: 'AFFECTED_PLANT', attributeName: 'JFAffectsFactory'});
  assert.equal(pagePackage.schema.body[0].options[0].value, 'OLD');
});

test('PLM元数据自动替换表单和Table标题并用Range原值映射国际化显示值', () => {
  const pagePackage = {
    schema: {type: 'page', body: [
      {id: 'u:type', type: 'select', name: 'old'},
      {id: 'u:list', type: 'crud', columns: [{name: 'attribute[JFChangeType]', label: '旧标题'}]}
    ]},
    resources: {fields: [{fieldCode: 'CHANGE_TYPE', rangeSource: 'PLM_RANGE', rangeConfig: {attributeName: 'JFChangeType'}}]},
    plmConfig: {
      fieldBindings: [{componentId: 'u:type', fieldCode: 'CHANGE_TYPE', valueKey: 'changeType'}],
      tableBindings: [{componentId: 'u:list', queryActionCode: 'QUERY_LIST', columnBindings: [{columnName: 'attribute[JFChangeType]', fieldCode: 'CHANGE_TYPE'}]}]
    }
  };
  const metadata = {fields: {CHANGE_TYPE: {label: 'Change Type', options: [{value: 'VAVE', label: 'Value Analysis'}]}}};
  const compiled = compilePackage(pagePackage, metadata);
  assert.equal(compiled.body[0].label, 'Change Type');
  assert.deepEqual(compiled.body[0].options, [{value: 'VAVE', label: 'Value Analysis'}]);
  assert.equal(compiled.body[1].columns[0].name, 'attribute[JFChangeType]');
  assert.equal(compiled.body[1].columns[0].label, 'Change Type');
  assert.equal(compiled.body[1].columns[0].type, 'mapping');
  assert.deepEqual({...compiled.body[1].columns[0].map}, {VAVE: 'Value Analysis'});
});

test('PLM Policy状态元数据可直接映射MapList的current字段', () => {
  const pagePackage = {
    schema: {type: 'page', body: [{id: 'u:list', type: 'crud', columns: [{name: 'current', label: '状态'}]}]},
    resources: {fields: [{fieldCode: 'DA_CURRENT', rangeSource: 'PLM_STATE', rangeConfig: {policyName: 'JFDA'}}]},
    plmConfig: {
      tableBindings: [{componentId: 'u:list', queryActionCode: 'QUERY_LIST', columnBindings: [{columnName: 'current', fieldCode: 'DA_CURRENT'}]}]
    }
  };
  const metadata = {fields: {DA_CURRENT: {label: 'Maturity State', options: [{value: 'Review', label: 'In Review'}]}}};
  const compiled = compilePackage(pagePackage, metadata);
  assert.equal(compiled.body[0].columns[0].name, 'current');
  assert.equal(compiled.body[0].columns[0].label, 'Maturity State');
  assert.equal(compiled.body[0].columns[0].type, 'mapping');
  assert.deepEqual({...compiled.body[0].columns[0].map}, {Review: 'In Review'});
});

test('Table绑定注入查询地址时保留Schema中的静态API参数', () => {
  const pagePackage = {
    schema: {type: 'page', body: [{id: 'u:list', type: 'crud', api: {url: '/old', data: {clientSide: true}}}]},
    plmConfig: {tableBindings: [{componentId: 'u:list', queryActionCode: 'QUERY_LIST'}]}
  };
  const compiled = compilePackage(pagePackage);
  assert.equal(compiled.body[0].api.url, 'plm://QUERY_LIST?componentId=u%3Alist');
  assert.deepEqual({...compiled.body[0].api.data}, {clientSide: true});
  assert.equal(pagePackage.schema.body[0].api.url, '/old');
});

test('前端分页Table绑定自动请求全部数据并始终显示分页', () => {
  const pagePackage = {
    schema: {type: 'page', body: [{id: 'u:list', type: 'crud', loadDataOnce: true, perPage: 20}]},
    plmConfig: {tableBindings: [{componentId: 'u:list', queryActionCode: 'QUERY_LIST'}]}
  };
  const compiled = compilePackage(pagePackage);
  assert.deepEqual({...compiled.body[0].api.data}, {clientSide: true});
  assert.equal(compiled.body[0].alwaysShowPagination, true);
  assert.equal(pagePackage.schema.body[0].alwaysShowPagination, undefined);
});

test('DA批量删除按钮编译为受控动作并保留选择与刷新配置', () => {
  const pagePackage = {
    schema: {
      type: 'page',
      body: [{
        id: 'u:list', type: 'crud', name: 'daList', primaryField: 'id',
        bulkActions: [{
          id: 'u:delete', type: 'button', label: '删除', requireSelected: true,
          confirmText: '确认删除吗？', reload: 'daList'
        }]
      }]
    },
    plmConfig: {
      actionBindings: [{componentId: 'u:delete', event: 'click', actionCode: 'DELETE_DA', successAction: 'NONE'}]
    }
  };
  const compiled = compilePackage(pagePackage);
  const deleteButton = compiled.body[0].bulkActions[0];
  assert.equal(deleteButton.actionType, 'ajax');
  assert.equal(deleteButton.api.method, 'post');
  assert.equal(deleteButton.api.url, 'plm://DELETE_DA?componentId=u%3Adelete');
  assert.equal(deleteButton.requireSelected, true);
  assert.equal(deleteButton.reload, 'daList');
  assert.equal(pagePackage.schema.body[0].bulkActions[0].actionType, undefined);
});

test('PLM搜索地址只解析回填所需配置', () => {
  const target = parseSearchTarget('plm://search?searchType=PROJECT&formId=u%3Aform&valueField=projectId&labelField=projectName&searchParams=field%3DTYPES%3Dtype_ProjectSpace');
  assert.deepEqual({...target}, {
    searchType: 'PROJECT',
    componentId: '',
    formId: 'u:form',
    valueField: 'projectId',
    labelField: 'projectName',
    searchParams: 'field=TYPES=type_ProjectSpace'
  });
});

test('PLM搜索绑定将按钮编译为受控搜索协议且不改原Schema', () => {
  const pagePackage = {
    schema: {type: 'page', body: [{id: 'u:search', type: 'button', actionType: 'url', url: '/old'}]},
    plmConfig: {searchBindings: [{componentId: 'u:search', formId: 'u:form', valueField: 'projectId', labelField: 'projectName', searchParams: 'field=TYPES=type_ProjectSpace&includeOIDprogram=JF_PublicMethodClass:getSystemAllProjectSpaces'}]}
  };
  const compiled = compilePackage(pagePackage);
  assert.equal(compiled.body[0].url,
    'plm://search?componentId=u%3Asearch&searchType=&formId=u%3Aform&valueField=projectId&labelField=projectName&searchParams=field%3DTYPES%3Dtype_ProjectSpace%26includeOIDprogram%3DJF_PublicMethodClass%3AgetSystemAllProjectSpaces');
  assert.equal(pagePackage.schema.body[0].url, '/old');
});

test('PLM搜索结果同时回填对象ID和显示名称', () => {
  let values;
  const scoped = {
    getComponentById: (id) => id === 'u:form' ? {setValues: (next) => { values = next; }} : null
  };
  applySearchResult(scoped, {
    formId: 'u:form',
    valueField: 'projectId',
    labelField: 'projectName'
  }, {objectId: '1.2.3.4', displayName: '测试项目'});
  assert.deepEqual({...values}, {projectId: '1.2.3.4', projectName: '测试项目'});
});
