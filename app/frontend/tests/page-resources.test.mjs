import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import test from 'node:test';
import ts from 'typescript';

const source = readFileSync(new URL('../src/types.ts', import.meta.url), 'utf8');
const {outputText} = ts.transpileModule(source, {compilerOptions: {module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2020}});
const {emptyPlmConfig, normalizePlmConfig} = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('新页清单为空且实例互不共享', () => {
  const first = emptyPlmConfig();
  first.fieldCodes.push('TITLE');
  assert.deepEqual(emptyPlmConfig().fieldCodes, []);
  assert.deepEqual(emptyPlmConfig().actionCodes, []);
  assert.deepEqual(emptyPlmConfig().dataBindings, []);
  assert.deepEqual(emptyPlmConfig().searchBindings, []);
});

test('旧页从字段、初始化查询、事件和Table绑定恢复清单，去重且不改输入', () => {
  const old = {fieldBindings: [{fieldCode: 'TITLE'}, {fieldCode: 'TITLE'}], dataBindings: [{actionCode: 'INIT_QUERY'}], actionBindings: [{actionCode: 'CREATE'}], tableBindings: [{queryActionCode: 'QUERY'}]};
  const before = JSON.stringify(old);
  const result = normalizePlmConfig(old);
  assert.deepEqual(result.fieldCodes, ['TITLE']);
  assert.deepEqual(result.actionCodes, ['INIT_QUERY', 'CREATE', 'QUERY']);
  assert.equal(JSON.stringify(old), before);
  assert.deepEqual(normalizePlmConfig(result), result);
});

test('保留未绑定清单项及缺失引用，兼容明确空清单但已有绑定', () => {
  const result = normalizePlmConfig({fieldCodes: ['EXTRA'], actionCodes: [], fieldBindings: [{fieldCode: 'MISSING'}], tableBindings: [{queryActionCode: 'OLD_QUERY'}]});
  assert.deepEqual(result.fieldCodes, ['EXTRA', 'MISSING']);
  assert.deepEqual(result.actionCodes, ['OLD_QUERY']);
  assert.deepEqual(normalizePlmConfig(JSON.parse(JSON.stringify(result))), result);
});

test('保留页面的3DSpace原生搜索绑定', () => {
  const searchBindings = [{componentId: 'u:search', formId: 'u:form', valueField: 'projectId', labelField: 'projectName', searchParams: 'field=TYPES=type_ProjectSpace&includeOIDprogram=JF_PublicMethodClass:getSystemAllProjectSpaces'}];
  assert.deepEqual(normalizePlmConfig({searchBindings}).searchBindings, searchBindings);
});

test('Table列绑定会自动纳入页面字段资源且补齐旧配置', () => {
  const result = normalizePlmConfig({tableBindings: [{
    componentId: 'u:list', queryActionCode: 'QUERY', itemsPath: 'data.items', totalPath: 'data.total',
    objectIdField: 'id', relIdField: 'id[connection]', columnBindings: [{columnName: 'attribute[Title]', fieldCode: 'DA_TITLE'}]
  }]});
  assert.deepEqual(result.fieldCodes, ['DA_TITLE']);
  assert.deepEqual(result.tableBindings[0].columnBindings, [{columnName: 'attribute[Title]', fieldCode: 'DA_TITLE'}]);
  assert.deepEqual(normalizePlmConfig({tableBindings: [{componentId: 'u:list', queryActionCode: 'QUERY'}]}).tableBindings[0].columnBindings, []);
});

test('Table拖拽动作自动纳入页面动作资源并保留允许类型', () => {
  const result = normalizePlmConfig({tableBindings: [{
    componentId: 'u:list', queryActionCode: 'QUERY_LIST',
    drop: {actionCode: 'QUERY_DA_TABLE_ROW', acceptedTypes: ['JFDA']}
  }]});
  assert.deepEqual(result.actionCodes, ['QUERY_LIST', 'QUERY_DA_TABLE_ROW']);
  assert.deepEqual(result.tableBindings[0].drop, {actionCode: 'QUERY_DA_TABLE_ROW', acceptedTypes: ['JFDA']});
});

test('DA新建弹框包含应用、确定、取消且应用提交后不关闭', () => {
  const schema = JSON.parse(readFileSync(new URL('../../examples/JF_DA_LIST_DEMO.json', import.meta.url), 'utf8'));
  const crud = schema.body[0];
  const dialog = crud.headerToolbar.find(item => item && item.id === 'u:da-new-button').dialog;
  assert.deepEqual(dialog.actions.map(action => action.label), ['应用', '确定', '取消']);
  assert.equal(dialog.actions[0].actionType, 'submit');
  assert.equal(dialog.actions[0].close, false);
  assert.equal(dialog.actions[1].actionType, 'confirm');
  assert.equal(dialog.actions[2].actionType, 'cancel');
  assert.equal(dialog.body.body[0].body[0].required, true);
  assert.equal(dialog.body.body[0].body[1].url.includes('searchParams='), true);
  assert.equal(dialog.body.reload, 'daList');
  assert.equal(dialog.body.body.find(item => item.name === 'changeType').options, undefined);
  assert.equal(dialog.body.body.find(item => item.name === 'projectPhase').options, undefined);
  assert.equal(dialog.body.body.find(item => item.name === 'beforeChange').required, true);
  assert.equal(dialog.body.body.find(item => item.name === 'afterChange').required, true);
  const affectedPlant = dialog.body.body.find(item => item.name === 'affectedPlant');
  assert.equal(affectedPlant.multiple, true);
  assert.equal(affectedPlant.joinValues, false);
  assert.equal(affectedPlant.options, undefined);
  assert.equal(crud.loadDataOnce, true);
  assert.equal(crud.loadDataOnceFetchOnFilter, false);
  assert.equal(crud.autoFillHeight, true);
  assert.equal(crud.autoJumpToTopOnPagerChange, false);
  assert.equal(crud.api.data.clientSide, true);
  assert.equal(crud.headerToolbar.includes('filter-toggler'), true);
  const filterButtons = crud.filter.body.find(item => item.type === 'button-toolbar').buttons;
  assert.deepEqual(filterButtons.map(button => button.label), ['执行筛选', '重置']);
  assert.equal(filterButtons[0].type, 'submit');
  assert.equal(crud.filter.actions.length, 0);
  assert.equal(crud.columns.filter(column => column.sortable).length, 12);
  assert.equal(crud.columns.find(column => column.name === 'attribute[JFChangeType]').sortable, true);
  assert.equal(crud.columns.find(column => column.name === 'attribute[JFProjectPhase]').sortable, true);
  assert.equal(crud.columns.find(column => column.name === 'attribute[JFAffectsFactory]').sortable, true);
  assert.equal(crud.filter.body.find(item => item.label === '变更类型').name, 'attribute[JFChangeType]');
  assert.equal(crud.filter.body.find(item => item.label === '项目阶段').name, 'attribute[JFProjectPhase]');
  assert.equal(crud.filter.body.find(item => item.name === 'attribute[JFChangeType]').options, undefined);
  assert.equal(crud.filter.body.find(item => item.name === 'attribute[JFProjectPhase]').options, undefined);
});

test('DA列表通过标准批量动作删除选中行并在成功后刷新', () => {
  const schema = JSON.parse(readFileSync(new URL('../../examples/JF_DA_LIST_DEMO.json', import.meta.url), 'utf8'));
  const crud = schema.body[0];
  const deleteButton = crud.bulkActions.find(item => item.id === 'u:da-delete-button');
  assert.equal(crud.primaryField, 'id');
  assert.equal(crud.headerToolbar.includes('bulkActions'), true);
  assert.equal(deleteButton.requireSelected, true);
  assert.equal(deleteButton.confirmText.includes('selectedItems.length'), true);
  assert.equal(deleteButton.reload, 'daList');
  assert.equal(deleteButton.messages.success, 'DA申请单删除成功');
});

test('DA信息动作以可拖动右侧面板展示五个详情页签', () => {
  const schema = JSON.parse(readFileSync(new URL('../../examples/JF_DA_LIST_DEMO.json', import.meta.url), 'utf8'));
  const crud = schema.body[0];
  const infoButton = crud.bulkActions.find(item => item.id === 'u:da-info-button');
  const detailService = infoButton.drawer.body;
  const tabs = detailService.body.find(item => item.id === 'u:da-detail-tabs').tabs;
  assert.equal(crud.bulkActions.indexOf(infoButton), crud.bulkActions.findIndex(item => item.id === 'u:da-delete-button') + 1);
  assert.equal(infoButton.actionType, 'drawer');
  assert.equal(infoButton.disabledOn.includes('selectedItems.length !== 1'), true);
  assert.equal(infoButton.drawer.position, 'right');
  assert.equal(infoButton.drawer.resizable, true);
  assert.equal(infoButton.drawer.overlay, false);
  const detailReload = infoButton.onEvent.click.actions[0];
  assert.equal(detailReload.actionType, 'reload');
  assert.equal(detailReload.componentId, 'u:da-detail-service');
  assert.equal(detailReload.data.objectId, '${selectedItems[0].id}');
  assert.equal(detailService.id, 'u:da-detail-service');
  assert.equal(detailService.api.data.objectId, '${objectId}');
  assert.equal(detailService.api.trackExpression, '${objectId}');
  assert.deepEqual(tabs.map(tab => tab.title), ['特性', '零件清单', '流程', '生命周期', '附件']);
  assert.equal(tabs[0].body.type, 'form');
  tabs.slice(1).forEach(tab => {
    assert.equal(tab.body.type, 'table');
    assert.equal(tab.body.autoFillHeight, true);
  });
});

test('DA创建JPO按PLM属性Range校验变更类型和项目阶段', () => {
  const source = readFileSync(new URL('../../../plm/spinner/schema_custom/Business/SourceFiles/JF_LowCode_mxJPO.java', import.meta.url), 'utf8');
  assert.equal(source.includes('mxAttr.getChoices(context, "JFChangeType")'), true);
  assert.equal(source.includes('mxAttr.getChoices(context, "JFProjectPhase")'), true);
  assert.equal(source.includes('Arrays.asList("CustomerRequirements", "DesignDeviations", "ProcessDeviations", "VAVE")'), false);
  assert.equal(source.includes('Arrays.asList("BatchProduction", "DV", "PV").contains(projectPhase)'), false);
});

test('DA拖拽加载JPO按objectId返回列表行并校验类型和所有者', () => {
  const source = readFileSync(new URL('../../../plm/spinner/schema_custom/Business/SourceFiles/JF_LowCode_mxJPO.java', import.meta.url), 'utf8');
  assert.equal(source.includes('public Map getDATableRowLowCode(Context context, String[] args)'), true);
  assert.equal(source.includes('"JFDA".equals(UIUtil.getValue(row, DomainConstants.SELECT_TYPE))'), true);
  assert.equal(source.includes('context.getUser().equals(UIUtil.getValue(row, DomainConstants.SELECT_OWNER))'), true);
  assert.equal(source.includes('"attribute[JFDAExtensionTime]"'), true);
});

test('3DSpace Runtime将长页面限制在当前视口并启用纵向滚动', () => {
  const css = readFileSync(new URL('../../../plm/3dspace/common/JFLowCode/runtime.css', import.meta.url), 'utf8');
  const jsp = readFileSync(new URL('../../../plm/3dspace/common/JF_LowCodeRuntime.jsp', import.meta.url), 'utf8');
  assert.equal(/html,\s*body\s*\{[^}]*height:\s*100%;[^}]*overflow:\s*hidden;/s.test(css), true);
  assert.equal(/#jf-lowcode-root\s*\{[^}]*height:\s*100%;[^}]*overflow-y:\s*auto;/s.test(css), true);
  assert.equal(jsp.includes('JFLowCode/runtime.css?v=20260912-1'), true);
});

test('PLM动作由当前Page发布快照解析而不是JSP业务白名单', () => {
  const actionJsp = readFileSync(new URL('../../../plm/3dspace/common/JF_LowCodeAction.jsp', import.meta.url), 'utf8');
  const runtimeJsp = readFileSync(new URL('../../../plm/3dspace/common/JF_LowCodeRuntime.jsp', import.meta.url), 'utf8');
  const pageJpo = readFileSync(new URL('../../../plm/spinner/schema_custom/Business/SourceFiles/JF_LowCodePage_mxJPO.java', import.meta.url), 'utf8');
  const publishService = readFileSync(new URL('../../backend/src/main/java/com/jfseat/lowcode/page/PagePublishService.java', import.meta.url), 'utf8');
  assert.equal(actionJsp.includes('"CREATE_DA".equals(actionCode)'), false);
  assert.equal(actionJsp.includes('"prepareActionInvocation"'), true);
  assert.equal(runtimeJsp.includes("JF_LowCodeAction.jsp?pageCode="), true);
  assert.equal(pageJpo.includes('public Map prepareActionInvocation'), true);
  assert.equal(pageJpo.includes('resolveMappedValue(inputMapping.get(keyValue), sourceParams)'), true);
  assert.equal(publishService.includes('resources.put("actions", actions)'), true);
  assert.equal(publishService.includes('validatePublishedActions(actionCodes, actions)'), true);
});
