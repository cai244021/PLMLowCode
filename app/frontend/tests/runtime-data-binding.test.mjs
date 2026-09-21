import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import test from 'node:test';
import vm from 'node:vm';

const source = readFileSync(new URL('../../../plm/3dspace/common/JFLowCode/runtime.js', import.meta.url), 'utf8');
const spaceSource = readFileSync(new URL('../../../plm/3dspace/common/JF_LowCodeRuntime.jsp', import.meta.url), 'utf8');

test('Space只加载发布页，404、服务器错误和网络失败均不回退静态JSON', async () => {
  assert.equal(spaceSource.includes('loadStaticPackage'), false);
  assert.equal(spaceSource.includes('JFLowCode/pages/'), false);
  const loader = spaceSource.match(/    function loadPagePackage\(\) \{[\s\S]*?\n    \}/)[0];
  for (const status of [404, 403, 500, 200, 'network']) {
    let calls = 0;
    const page = {schema: {type: 'page'}, plmConfig: {}};
    const scope = {
      pageCode: 'JF_DA_LIST_DEMO',
      JFLowCodeRuntime: {parseJson: JSON.parse},
      fetch: async url => {
        calls++;
        assert.match(url, /^JF_LowCodePage\.jsp\?pageCode=JF_DA_LIST_DEMO/);
        if (status === 'network') throw new Error('network unavailable');
        return {status, ok: status === 200, text: async () => JSON.stringify({status: 0, msg: '', data: page})};
      }
    };
    vm.runInNewContext(loader, scope);
    if (status === 200) assert.deepEqual(await scope.loadPagePackage(), page);
    else await assert.rejects(scope.loadPagePackage(), status === 'network' ? /network unavailable/ : new RegExp('HTTP ' + status));
    assert.equal(calls, 1);
  }
});
let capturedEnv;
test('Space拒绝业务错误、旧响应和损坏页面包', async () => {
  const loader = spaceSource.match(/    function loadPagePackage\(\) \{[\s\S]*?\n    \}/)[0];
  for (const body of [{status: 1, msg: '无权读取', data: {}}, {schema: {}, plmConfig: {}}, {status: 0, data: {}}]) {
    const scope = {pageCode: 'TEST', JFLowCodeRuntime: {parseJson: JSON.parse},
      fetch: async () => ({ok: true, text: async () => JSON.stringify(body)})};
    vm.runInNewContext(loader, scope);
    await assert.rejects(scope.loadPagePackage(), body.status === 1 ? /无权读取/ : /格式不正确/);
  }
});
let capturedData;
const modalContainer = {};
const bodyClasses = new Set();
const documentBody = {classList: {add: value => bodyClasses.add(value)}};
const sandbox = {
  window: {
    setTimeout,
    document: {
      body: documentBody,
      querySelector: selector => selector === '#root' || selector === '.amis-scope' ? modalContainer : null
    },
    amisRequire: () => ({embed: (_container, _schema, data, env) => {
      capturedEnv = env;
      capturedData = data;
      return {getComponentById: () => ({setValues: () => assert.fail('取消搜索不应回填表单')})};
    }})
  },
  URLSearchParams
};
vm.runInNewContext(source, sandbox);

test('Runtime统一接管并去重当前页面的重复错误提示', () => {
  const messages = [];
  const noticeSandbox = {
    window: {
      amisRequire: moduleName => moduleName === 'amis-ui'
        ? {toast: {error: message => messages.push(message)}} : null
    },
    URLSearchParams
  };
  vm.runInNewContext(source, noticeSandbox);
  noticeSandbox.window.JFLowCodeRuntime.notify('error', 'Trigger校验失败');
  noticeSandbox.window.JFLowCodeRuntime.notify('danger', 'Trigger校验失败');
  assert.deepEqual(messages, ['Trigger校验失败']);
});

test('Runtime在渲染及请求前拒绝不兼容Adapter', () => {
  assert.equal(sandbox.window.JFLowCodeRuntime.protocolVersion, 1);
  for (const version of [undefined, 0, 2, '1', 1.5]) {
    let called = false;
    assert.throws(() => sandbox.window.JFLowCodeRuntime.embed({
      pagePackage: {schema: {}, plmConfig: {}},
      adapter: {protocolVersion: version, executeAction: () => {called = true;}}
    }), /Adapter协议不兼容/);
    assert.equal(called, false);
  }
});
const {
  compilePackage, createFetcher, parseSearchTarget, applySearchResult,
  parseDroppedItems, validateDroppedItems, mergeDroppedRows, resolveMapping,
  executeEffects, executeEventBinding
} = sandbox.window.JFLowCodeRuntime;

test('通用映射支持类型保留、嵌套对象、数组和字符串插值', () => {
  const sourceData = {form: {title: 'DA标题'}, selected: [{id: '1'}, {id: '2'}], count: 2};
  const mapped = resolveMapping({
    title: '${form.title}',
    firstId: '${selected[0].id}',
    selected: '${selected}',
    message: '共${count}条-${missing}',
    nested: [{id: '${selected.1.id}'}]
  }, sourceData);
  assert.equal(mapped.title, 'DA标题');
  assert.equal(mapped.firstId, '1');
  assert.equal(Array.isArray(mapped.selected), true);
  assert.equal(mapped.selected.length, 2);
  assert.equal(mapped.message, '共2条-');
  assert.equal(mapped.nested[0].id, '2');
  assert.equal(resolveMapping('${__proto__.polluted}', sourceData), undefined);
});

test('V2事件编译为带bindingId的受控PLM动作', () => {
  const compiled = compilePackage({
    schema: {type: 'page', body: [{id: 'u:create', type: 'button'}]},
    plmConfig: {eventBindings: [{
      id: 'create-da',
      source: {componentId: 'u:create', event: 'click'},
      action: {actionCode: 'CREATE_DA', inputMapping: {}},
      success: [],
      failure: []
    }]}
  });
  assert.equal(compiled.body[0].actionType, 'ajax');
  assert.equal(compiled.body[0].api.url, 'plm://CREATE_DA?componentId=u%3Acreate&bindingId=create-da');
});

test('Runtime为现有和未来的所有Dialog统一启用拖拽且不修改原Schema', () => {
  const pagePackage = {
    schema: {type: 'page', body: [{id: 'u:new', type: 'button', dialog: {id: 'u:dialog', type: 'dialog', title: '新建'}}]},
    plmConfig: {}
  };
  const compiled = compilePackage(pagePackage);
  assert.equal(compiled.body[0].dialog.draggable, true);
  assert.equal(pagePackage.schema.body[0].dialog.draggable, undefined);
});

test('V2表格选择变化映射为AMIS原生selectedChange事件', () => {
  const compiled = compilePackage({
    schema: {type: 'page', body: [{id: 'u:list', type: 'crud'}]},
    plmConfig: {eventBindings: [{
      id: 'selection-change',
      source: {componentId: 'u:list', event: 'selectionChange'},
      action: {actionCode: 'LOAD_SELECTION', inputMapping: {}},
      success: [],
      failure: []
    }]}
  });
  assert.equal(compiled.body[0].onEvent.selectionChange, undefined);
  assert.equal(compiled.body[0].onEvent.selectedChange.actions[0].args.api.url,
    'plm://LOAD_SELECTION?componentId=u%3Alist&bindingId=selection-change');
});

test('通用Effect按顺序更新指定组件、通知并执行链式动作', async () => {
  const calls = [];
  const tableData = {items: [{id: 'old'}]};
  const table = {
    getData: () => tableData,
    setData: data => {
      tableData.items = data.items;
      calls.push(`rows:${data.items.map(item => item.id).join(',')}`);
    },
    reload: () => calls.push('reload')
  };
  const runtime = {
    scoped: {getComponentById: id => id === 'u:list' ? table : null},
    adapter: {
      context: {objectId: 'ctx'},
      executeAction: (code, data) => {
        calls.push(`action:${code}:${data.objectId}`);
        return {status: 0, msg: '', data: {}};
      },
      notify: (level, message) => calls.push(`notify:${level}:${message}`)
    }
  };
  await executeEffects([
    {type: 'APPEND_ROWS', target: 'u:list', position: 'first', deduplicateBy: 'id', mapping: '${response.data.rows}'},
    {type: 'RELOAD', target: 'u:list'},
    {type: 'NOTIFY', level: 'success', message: '已加载${response.data.rows[0].id}'},
    {type: 'CHAIN_ACTION', action: {actionCode: 'AUDIT', inputMapping: {objectId: '${response.data.rows[0].id}'}}}
  ], {response: {data: {rows: [{id: 'new'}, {id: 'old'}]}}}, runtime);
  assert.deepEqual(calls, ['rows:new,old', 'reload', 'notify:success:已加载new', 'action:AUDIT:new']);
});

test('弹窗抽屉和关闭Effect使用AMIS嵌入实例动作', async () => {
  const calls = [];
  const runtime = {
    scoped: {
      getComponentById: () => null,
      doAction: (action, data) => calls.push({action, data}),
      closeById: id => calls.push({closeById: id})
    },
    adapter: {}
  };
  await executeEffects([
    {type: 'OPEN_DIALOG', mapping: {id: 'u:dialog', title: '新建DA', body: '内容'}},
    {type: 'OPEN_DRAWER', mapping: {id: 'u:drawer', title: 'DA详情', body: '内容'}},
    {type: 'CLOSE', target: 'u:dialog'}
  ], {response: {data: {}}}, runtime);
  assert.equal(JSON.stringify(calls), JSON.stringify([
    {action: {actionType: 'dialog', dialog: {id: 'u:dialog', title: '新建DA', body: '内容', draggable: true}}, data: {response: {data: {}}}},
    {action: {actionType: 'drawer', drawer: {id: 'u:drawer', title: 'DA详情', body: '内容'}}, data: {response: {data: {}}}},
    {closeById: 'u:dialog'}
  ]));
});

test('V2事件应用输入映射并按标准状态选择成功或失败Effect', async () => {
  const calls = [];
  const runtime = {
    scoped: null,
    adapter: {
      context: {objectId: 'ctx'},
      executeAction: (code, data) => {
        calls.push({code, data});
        return {status: data.title ? 0 : 1, msg: data.title ? 'ok' : '缺少标题', data: {objectId: '1'}};
      },
      notify: (level, message) => calls.push({level, message})
    }
  };
  const binding = {
    source: {componentId: 'u:form', event: 'submit'},
    action: {actionCode: 'CREATE_DA', inputMapping: {title: '${form.title}', contextId: '${plmContext.objectId}'}},
    success: [{type: 'NOTIFY', level: 'success', message: '${response.msg}'}],
    failure: [{type: 'NOTIFY', level: 'error', message: '${response.msg}'}]
  };
  await executeEventBinding(binding, {form: {title: '新DA'}}, runtime);
  await executeEventBinding(binding, {form: {title: ''}}, runtime);
  assert.deepEqual(JSON.parse(JSON.stringify(calls)), [
    {code: 'CREATE_DA', data: {title: '新DA', contextId: 'ctx'}},
    {level: 'success', message: 'ok'},
    {code: 'CREATE_DA', data: {title: '', contextId: 'ctx'}},
    {level: 'error', message: '缺少标题'}
  ]);
});

test('V2空输入映射透传页面事件数据以兼容V1升级', async () => {
  let received;
  await executeEventBinding({
    action: {actionCode: 'DELETE_DA', inputMapping: {}},
    success: [],
    failure: []
  }, {selectedItems: [{id: '1'}]}, {
    scoped: null,
    adapter: {
      context: {},
      executeAction: (_code, data) => {
        received = data;
        return {status: 0, msg: '', data: {}};
      }
    }
  });
  assert.deepEqual(JSON.parse(JSON.stringify(received)), {selectedItems: [{id: '1'}]});
});

test('P0查询创建删除场景均通过同一V2事件执行链', async () => {
  const requests = [];
  const effects = [];
  const runtime = {
    scoped: {getComponentById: id => ({
      reload: () => effects.push(`reload:${id}`),
      setData: () => effects.push(`data:${id}`)
    })},
    adapter: {
      context: {},
      executeAction: (code, data) => {
        requests.push({code, data});
        return {status: 0, msg: `${code}成功`, data: {objectId: 'new-id', items: []}};
      },
      close: () => effects.push('close'),
      notify: (level, message) => effects.push(`${level}:${message}`)
    }
  };
  await executeEventBinding({action: {actionCode: 'QUERY_CURRENT_USER_DA_LIST', inputMapping: {page: '${page}'}}, success: [{type: 'SET_DATA', target: 'u:list', mapping: '${response.data}'}], failure: []}, {page: 1}, runtime);
  await executeEventBinding({action: {actionCode: 'CREATE_DA', inputMapping: {title: '${title}'}}, success: [{type: 'RELOAD', target: 'u:list'}, {type: 'CLOSE'}], failure: []}, {title: '新DA'}, runtime);
  await executeEventBinding({action: {actionCode: 'DELETE_DA', inputMapping: {ids: '${selectedItems}'}}, success: [{type: 'RELOAD', target: 'u:list'}, {type: 'NOTIFY', level: 'success', message: '${response.msg}'}], failure: []}, {selectedItems: ['1', '2']}, runtime);
  assert.deepEqual(JSON.parse(JSON.stringify(requests)), [
    {code: 'QUERY_CURRENT_USER_DA_LIST', data: {page: 1}},
    {code: 'CREATE_DA', data: {title: '新DA'}},
    {code: 'DELETE_DA', data: {ids: ['1', '2']}}
  ]);
  assert.deepEqual(effects, ['data:u:list', 'reload:u:list', 'close', 'reload:u:list', 'success:DELETE_DA成功']);
});

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

test('页面初始化绑定保留通用报表查询的静态参数', () => {
  const pagePackage = {
    schema: {
      type: 'page',
      body: [{id: 'u:report-service', type: 'service', api: {data: {reportCode: 'APPROVAL_TASK', scope: 'all'}}}]
    },
    plmConfig: {
      dataBindings: [{componentId: 'u:report-service', trigger: 'INIT', actionCode: 'QUERY_PLM_REPORT_SUMMARY'}]
    }
  };

  const compiled = compilePackage(pagePackage);
  assert.equal(compiled.body[0].api.url, 'plm://QUERY_PLM_REPORT_SUMMARY?componentId=u%3Areport-service');
  assert.deepEqual({...compiled.body[0].api.data}, {reportCode: 'APPROVAL_TASK', scope: 'all'});
});

test('页面初始化绑定保留Service动态请求跟踪条件', () => {
  const pagePackage = {
    schema: {
      type: 'page',
      body: [{
        id: 'u:detail-service',
        type: 'service',
        api: {data: {objectId: '${objectId}'}, trackExpression: '${objectId}'}
      }]
    },
    plmConfig: {
      dataBindings: [{componentId: 'u:detail-service', trigger: 'INIT', actionCode: 'QUERY_DETAIL'}]
    }
  };

  const compiled = compilePackage(pagePackage);
  assert.equal(compiled.body[0].api.url, 'plm://QUERY_DETAIL?componentId=u%3Adetail-service');
  assert.equal(compiled.body[0].api.trackExpression, '${objectId}');
  assert.deepEqual({...compiled.body[0].api.data}, {objectId: '${objectId}'});
});

test('请求出口从原始JSON恢复AMIS遗漏的报表静态参数', async () => {
  let actionRequest;
  const pagePackage = {
    schema: {
      type: 'page',
      body: [{id: 'u:report-service', type: 'service', api: {data: {reportCode: 'APPROVAL_TASK', scope: 'all'}}}]
    }
  };
  const fetcher = createFetcher(pagePackage, {
    executeAction: (actionCode, data) => {
      actionRequest = {actionCode, data};
      return {status: 0, data: {}};
    }
  });

  await fetcher({
    url: 'plm://QUERY_PLM_REPORT_SUMMARY?componentId=u%3Areport-service',
    method: 'post',
    data: {reportCode: 'CHANGE_EXECUTION', scope: 'overdue'}
  });

  assert.equal(actionRequest.actionCode, 'QUERY_PLM_REPORT_SUMMARY');
  assert.deepEqual({...actionRequest.data}, {reportCode: 'APPROVAL_TASK', scope: 'overdue'});
});

test('请求出口不会把尚未计算的动态模板发送到JPO', async () => {
  let actionRequest;
  const pagePackage = {
    schema: {
      type: 'page',
      body: [{
        id: 'u:report-list',
        type: 'crud',
        api: {data: {reportCode: 'CHANGE_EXECUTION', scope: '${scope}', page: '${page}'}}
      }]
    }
  };
  const fetcher = createFetcher(pagePackage, {
    executeAction: (actionCode, data) => {
      actionRequest = {actionCode, data};
      return {status: 0, data: {}};
    }
  });

  await fetcher({
    url: 'plm://QUERY_PLM_REPORT_DETAILS?componentId=u%3Areport-list',
    method: 'post',
    data: {scope: 'overdue', page: 1}
  });

  assert.deepEqual({...actionRequest.data}, {reportCode: 'CHANGE_EXECUTION', scope: 'overdue', page: 1});
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
  assert.equal(compiled.body[0].options, undefined);
  assert.equal(compiled.body[0].source, '${__plmFieldOptions.CHANGE_TYPE}');
  assert.equal(compiled.body[1].columns[0].name, 'attribute[JFChangeType]');
  assert.equal(compiled.body[1].columns[0].label, 'Change Type');
  assert.equal(compiled.body[1].columns[0].type, 'mapping');
  assert.deepEqual({...compiled.body[1].columns[0].map}, {VAVE: 'Value Analysis'});
});

test('PLM Range选项注入AMIS初始数据域供表单动态加载', async () => {
  const pagePackage = {
    schema: {type: 'page', body: [{id: 'u:type', type: 'select'}]},
    resources: {fields: [{fieldCode: 'CHANGE_TYPE', rangeSource: 'PLM_RANGE', rangeConfig: {attributeName: 'JFChangeType'}}]},
    plmConfig: {fieldBindings: [{componentId: 'u:type', fieldCode: 'CHANGE_TYPE', valueKey: 'changeType'}]}
  };
  const fieldOptions = [{value: 'VAVE', label: 'Value Analysis'}];

  await sandbox.window.JFLowCodeRuntime.embed({
    container: '#root',
    pagePackage,
    adapter: {
      context: {},
      executeAction: () => ({status: 0, data: {fields: {CHANGE_TYPE: {label: 'Change Type', options: fieldOptions}}}}),
      protocolVersion: 1
    }
  });

  assert.deepEqual(capturedData.data.__plmFieldOptions.CHANGE_TYPE, fieldOptions);
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

test('Table拖拽绑定生成独立投放区域并解析3DSearch对象', () => {
  const pagePackage = {
    schema: {type: 'page', body: [{id: 'u:da-list', type: 'crud'}]},
    plmConfig: {tableBindings: [{
      componentId: 'u:da-list', queryActionCode: 'QUERY_LIST',
      drop: {actionCode: 'QUERY_DA_TABLE_ROW', acceptedTypes: ['JFDA']}
    }]}
  };
  const compiled = compilePackage(pagePackage);
  assert.match(compiled.body[0].className, /jf-lowcode-drop-u-da-list/);
  assert.equal(pagePackage.schema.body[0].className, undefined);
  const items = parseDroppedItems(JSON.stringify({data: {items: [{objectId: '1.2.3', objectType: 'JFDA'}]}}));
  assert.deepEqual(JSON.parse(JSON.stringify(items)), [{objectId: '1.2.3', objectType: 'JFDA', objectTaxonomies: []}]);
  assert.doesNotThrow(() => validateDroppedItems(items, ['type_JFDA']));
  assert.throws(() => validateDroppedItems(items, ['VPMReference']), /Table/);
});

test('Table拖入数据按objectId去重更新并移动到第一行', async () => {
  let nextData;
  const table = {
    getData: () => ({items: [{id: '0', name: '原第一行'}, {id: '1', name: '旧名称'}], total: 2}),
    setData: (data) => { nextData = data; }
  };
  await mergeDroppedRows({getComponentById: () => table}, {componentId: 'u:list', objectIdField: 'id'}, [
    {id: '1', name: '新名称'}, {id: '2', name: '新增对象'}
  ]);
  assert.equal(JSON.stringify(nextData.items), JSON.stringify([
    {id: '1', name: '新名称'}, {id: '2', name: '新增对象'}, {id: '0', name: '原第一行'}
  ]));
  assert.equal(nextData.total, 3);
});

test('Table投放后按每个objectId调用配置动作并写入当前Table', async () => {
  const target = {};
  let dropHandler;
  let actionRequest;
  let tableData = {items: [], total: 0};
  let targetQueryCount = 0;
  const originalQuerySelector = sandbox.window.document.querySelector;
  const originalAmisRequire = sandbox.window.amisRequire;
  try {
    sandbox.window.document.querySelector = selector => {
      if (selector !== '.jf-lowcode-drop-u-da-list') return modalContainer;
      targetQueryCount += 1;
      return targetQueryCount > 1 ? target : null;
    };
    sandbox.window.amisRequire = () => ({embed: () => ({
      getComponentById: () => ({
        getData: () => tableData,
        setData: data => { tableData = data; }
      })
    })});
    await sandbox.window.JFLowCodeRuntime.embed({
      container: '#root',
      pagePackage: {
        schema: {type: 'page', body: [{id: 'u:da-list', type: 'crud'}]},
        plmConfig: {tableBindings: [{
          componentId: 'u:da-list', queryActionCode: 'QUERY_LIST', objectIdField: 'id',
          drop: {actionCode: 'QUERY_DA_TABLE_ROW', acceptedTypes: ['JFDA']}
        }]}
      },
      adapter: {
        executeAction: (actionCode, data) => {
          actionRequest = {actionCode, data};
          return {status: 0, data: {id: data.objectId, name: 'DA-0001'}};
        },
        protocolVersion: 1,
        bindTableDrop: (_target, handler) => { dropHandler = handler; }
      }
    });
    assert.ok(targetQueryCount > 1);
    dropHandler(JSON.stringify({data: {items: [{objectId: '1.2.3', objectType: 'JFDA'}]}}));
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(JSON.stringify(actionRequest), JSON.stringify({actionCode: 'QUERY_DA_TABLE_ROW', data: {objectId: '1.2.3'}}));
    assert.equal(JSON.stringify(tableData.items), JSON.stringify([{id: '1.2.3', name: 'DA-0001'}]));
  } finally {
    sandbox.window.document.querySelector = originalQuerySelector;
    sandbox.window.amisRequire = originalAmisRequire;
  }
});

test('V2拖拽事件按每个objectId执行统一动作和成功Effect', async () => {
  const target = {};
  let dropHandler;
  const actionRequests = [];
  const notifications = [];
  const dropErrors = [];
  const originalQuerySelector = sandbox.window.document.querySelector;
  const originalAmisRequire = sandbox.window.amisRequire;
  try {
    sandbox.window.document.querySelector = selector => selector === '.jf-lowcode-drop-u-da-list' ? target : modalContainer;
    sandbox.window.amisRequire = () => ({embed: () => ({getComponentById: () => null})});
    await sandbox.window.JFLowCodeRuntime.embed({
      container: '#root',
      pagePackage: {
        schema: {type: 'page', body: [{id: 'u:da-list', type: 'crud'}]},
        plmConfig: {eventBindings: [{
          id: 'drop-da',
          source: {componentId: 'u:da-list', event: 'drop', acceptedTypes: ['JFDA']},
          action: {actionCode: 'QUERY_DA_TABLE_ROW', inputMapping: {objectId: '${objectId}'}},
          success: [{type: 'NOTIFY', level: 'success', message: '已加载${response.data.name}'}],
          failure: []
        }]}
      },
      adapter: {
        executeAction: (actionCode, data) => {
          actionRequests.push({actionCode, data});
          return {status: 0, msg: '', data: {id: data.objectId, name: data.objectId}};
        },
        protocolVersion: 1,
        notify: (level, message) => notifications.push({level, message}),
        notifyError: error => dropErrors.push(error.message),
        bindTableDrop: (_target, handler) => { dropHandler = handler; }
      }
    });
    dropHandler(JSON.stringify({data: {items: [
      {objectId: '1.2.3', objectType: 'JFDA'},
      {objectId: '1.2.4', objectType: 'JFDA'}
    ]}}));
    await new Promise(resolve => setImmediate(resolve));
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(JSON.stringify(actionRequests), JSON.stringify([
      {actionCode: 'QUERY_DA_TABLE_ROW', data: {objectId: '1.2.3'}},
      {actionCode: 'QUERY_DA_TABLE_ROW', data: {objectId: '1.2.4'}}
    ]));
    assert.equal(JSON.stringify(notifications), JSON.stringify([
      {level: 'success', message: '已加载1.2.3'},
      {level: 'success', message: '已加载1.2.4'}
    ]));
    dropHandler(JSON.stringify({data: {items: [{objectId: '2.3.4', objectType: 'VPMReference'}]}}));
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(actionRequests.length, 2);
    assert.equal(dropErrors.length, 1);
  } finally {
    sandbox.window.document.querySelector = originalQuerySelector;
    sandbox.window.amisRequire = originalAmisRequire;
  }
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

test('Widget手动关闭搜索后保持表单原值', async () => {
  const notifications = [];
  const pagePackage = {
    schema: {type: 'page', body: []},
    plmConfig: {searchBindings: [{
      componentId: 'u:search', formId: 'u:form', valueField: 'projectId',
      labelField: 'projectName', searchParams: 'field=TYPES=type_ProjectSpace'
    }]}
  };
  await sandbox.window.JFLowCodeRuntime.embed({
    container: '#root',
    pagePackage,
    adapter: {
      executeAction: () => ({status: 0, data: {fields: {}}}),
      protocolVersion: 1,
      openSearch: () => Promise.resolve({objectId: '', cancelled: true}),
      notify: (level, message) => notifications.push({level, message})
    }
  });
  assert.equal(capturedEnv.getModalContainer(), documentBody);
  assert.ok(bodyClasses.has('amis-scope'));
  capturedEnv.notify('error', '删除失败');
  assert.deepEqual(notifications, [{level: 'error', message: '删除失败'}]);
  capturedEnv.jumpTo('plm://search?componentId=u%3Asearch');
  await new Promise(resolve => setImmediate(resolve));
});

test('PLM报表明细与动态过滤选项共享汇总Service数据域', () => {
  [
    'JF_APPROVAL_TASK_REPORT.json',
    'JF_PROJECT_TASK_STATUS_REPORT.json',
    'JF_CHANGE_EXECUTION_REPORT.json'
  ].forEach(fileName => {
    const pagePackage = JSON.parse(readFileSync(
      new URL(`../../examples/reports/${fileName}`, import.meta.url),
      'utf8'
    ));
    const compiled = compilePackage(pagePackage);
    const service = compiled.body[0];
    const cards = service.body.find(component => component.type === 'cards');
    const detail = service.body.find(component => component.type === 'crud');
    const dynamicFilters = detail.filter.body.filter(component => component.source);

    assert.equal(pagePackage.schema.body.length, 1);
    assert.equal(service.type, 'service');
    assert.equal(cards.card.useCardLabel, false);
    assert.ok(detail);
    assert.ok(dynamicFilters.length > 0);
    assert.equal(detail.api.data.scope, '${scope}');
    assert.equal(detail.api.data.page, '${page}');
    assert.match(detail.api.url, /^plm:\/\/QUERY_PLM_REPORT_DETAILS\?/);
    dynamicFilters.forEach(component => assert.match(component.source, /^\$\{filterOptions\./));
  });
});
