import {useMemo} from 'react';
import type {SchemaObject} from 'amis';
import {collectPageComponents} from './schemaComponents';
import type {
  ActionBinding,
  FieldBinding,
  PlmActionDefinition,
  PlmFieldDefinition,
  PlmPageConfig,
  TableBinding
} from './types';

interface Props {
  schema: SchemaObject;
  config: PlmPageConfig;
  fields: PlmFieldDefinition[];
  actions: PlmActionDefinition[];
  onChange: (config: PlmPageConfig) => void;
  onSave: () => Promise<void>;
  onExport: () => void;
}

export default function PagePlmBinding({schema, config, fields, actions, onChange, onSave, onExport}: Props) {
  const components = useMemo(() => collectPageComponents(schema), [schema]);
  const enabledActions = actions.filter((action) => action.enabled);
  const queryActions = enabledActions.filter((action) => action.actionKind === 'QUERY');

  const updateContext = (key: keyof PlmPageConfig['context'], value: string) => {
    onChange({...config, context: {...config.context, [key]: value}});
  };

  const updateFieldBinding = (index: number, patch: Partial<FieldBinding>) => {
    onChange({...config, fieldBindings: config.fieldBindings.map((item, itemIndex) => itemIndex === index ? {...item, ...patch} : item)});
  };

  const updateActionBinding = (index: number, patch: Partial<ActionBinding>) => {
    onChange({...config, actionBindings: config.actionBindings.map((item, itemIndex) => itemIndex === index ? {...item, ...patch} : item)});
  };

  const updateTableBinding = (index: number, patch: Partial<TableBinding>) => {
    onChange({...config, tableBindings: config.tableBindings.map((item, itemIndex) => itemIndex === index ? {...item, ...patch} : item)});
  };

  const addFieldBinding = () => {
    const component = components.fields[0];
    const field = fields[0];
    if (!component || !field) return;
    onChange({...config, fieldBindings: [...config.fieldBindings, {componentId: component.id, fieldCode: field.fieldCode, valueKey: component.name || field.fieldCode.toLowerCase()}]});
  };

  const addActionBinding = () => {
    const component = components.actions[0];
    const action = enabledActions[0];
    if (!component || !action) return;
    onChange({...config, actionBindings: [...config.actionBindings, {componentId: component.id, event: component.type === 'form' ? 'submit' : 'click', actionCode: action.actionCode, successAction: 'NONE'}]});
  };

  const addTableBinding = () => {
    const component = components.tables[0];
    const action = queryActions[0];
    if (!component || !action) return;
    onChange({...config, tableBindings: [...config.tableBindings, {componentId: component.id, queryActionCode: action.actionCode, itemsPath: 'data.items', totalPath: 'data.total', objectIdField: 'objectId', relIdField: 'relId'}]});
  };

  return (
    <section className="binding-page">
      <div className="binding-heading">
        <div><h2>当前页面PLM绑定</h2><p>绑定信息独立于AMIS Schema保存，并与页面版本一起归档。</p></div>
        <div className="form-actions"><button type="button" onClick={onExport}>导出配置包</button><button type="button" className="primary" onClick={onSave}>保存页面及绑定</button></div>
      </div>

      <div className="summary-cards">
        <div><strong>{components.fields.length}</strong><span>可绑定字段组件</span></div>
        <div><strong>{components.actions.length}</strong><span>按钮/表单组件</span></div>
        <div><strong>{components.tables.length}</strong><span>Table组件</span></div>
        <div><strong>{config.fieldBindings.length + config.actionBindings.length + config.tableBindings.length}</strong><span>已配置绑定</span></div>
      </div>

      <div className="binding-section">
        <div className="section-title"><div><h3>PLM页面上下文</h3><p>发布后由JSP或Widget Runtime从URL与平台上下文中注入。</p></div></div>
        <div className="inline-grid">
          <label>对象ID参数<input value={config.context.objectIdParam} onChange={(e) => updateContext('objectIdParam', e.target.value)} /></label>
          <label>父对象参数<input value={config.context.parentOidParam} onChange={(e) => updateContext('parentOidParam', e.target.value)} /></label>
          <label>关系ID参数<input value={config.context.relIdParam} onChange={(e) => updateContext('relIdParam', e.target.value)} /></label>
        </div>
      </div>

      <div className="binding-section">
        <div className="section-title"><div><h3>表单字段绑定</h3><p>将AMIS输入控件绑定到PLM Scheme字段。</p></div><button type="button" onClick={addFieldBinding} disabled={!components.fields.length || !fields.length}>新增绑定</button></div>
        {!components.fields.length && <div className="empty-tip">当前页面没有输入控件，请先在页面设计中加入文本框、下拉框等组件。</div>}
        {config.fieldBindings.map((binding, index) => (
          <div className="binding-row field-row" key={`${binding.componentId}-${index}`}>
            <label>页面组件<select value={binding.componentId} onChange={(e) => updateFieldBinding(index, {componentId: e.target.value})}>{components.fields.map((component) => <option key={component.id} value={component.id}>{component.label}</option>)}</select></label>
            <label>PLM字段<select value={binding.fieldCode} onChange={(e) => updateFieldBinding(index, {fieldCode: e.target.value})}>{fields.map((field) => <option key={field.fieldCode} value={field.fieldCode}>{field.displayName} · {field.schemaName}</option>)}</select></label>
            <label>数据Key<input value={binding.valueKey} onChange={(e) => updateFieldBinding(index, {valueKey: e.target.value})} /></label>
            <button type="button" className="icon-danger" onClick={() => onChange({...config, fieldBindings: config.fieldBindings.filter((_, itemIndex) => itemIndex !== index)})}>删除</button>
          </div>
        ))}
      </div>

      <div className="binding-section">
        <div className="section-title"><div><h3>按钮与表单事件</h3><p>页面只保存actionCode，JPO名称与方法由动作库控制。</p></div><button type="button" onClick={addActionBinding} disabled={!components.actions.length || !enabledActions.length}>新增事件</button></div>
        {!components.actions.length && <div className="empty-tip">当前页面没有按钮或表单组件。</div>}
        {config.actionBindings.map((binding, index) => (
          <div className="binding-row action-row" key={`${binding.componentId}-${index}`}>
            <label>页面组件<select value={binding.componentId} onChange={(e) => updateActionBinding(index, {componentId: e.target.value})}>{components.actions.map((component) => <option key={component.id} value={component.id}>{component.label}</option>)}</select></label>
            <label>事件<select value={binding.event} onChange={(e) => updateActionBinding(index, {event: e.target.value as ActionBinding['event']})}><option value="click">点击</option><option value="submit">提交</option><option value="change">值变化</option></select></label>
            <label>PLM动作<select value={binding.actionCode} onChange={(e) => updateActionBinding(index, {actionCode: e.target.value})}>{enabledActions.map((action) => <option key={action.actionCode} value={action.actionCode}>{action.actionName} · {action.actionCode}</option>)}</select></label>
            <label>成功后<select value={binding.successAction} onChange={(e) => updateActionBinding(index, {successAction: e.target.value as ActionBinding['successAction']})}><option value="NONE">不处理</option><option value="REFRESH">刷新页面/Table</option><option value="CLOSE">关闭弹窗</option><option value="OPEN_DETAIL">打开返回对象详情</option></select></label>
            <button type="button" className="icon-danger" onClick={() => onChange({...config, actionBindings: config.actionBindings.filter((_, itemIndex) => itemIndex !== index)})}>删除</button>
          </div>
        ))}
      </div>

      <div className="binding-section">
        <div className="section-title"><div><h3>Table查询与对象ID</h3><p>配置查询JPO、数据路径以及行对象和关系ID字段。</p></div><button type="button" onClick={addTableBinding} disabled={!components.tables.length || !queryActions.length}>新增Table绑定</button></div>
        {!components.tables.length && <div className="empty-tip">当前页面没有CRUD、Table或Table 2.0组件。</div>}
        {config.tableBindings.map((binding, index) => (
          <div className="binding-row table-row" key={`${binding.componentId}-${index}`}>
            <label>Table组件<select value={binding.componentId} onChange={(e) => updateTableBinding(index, {componentId: e.target.value})}>{components.tables.map((component) => <option key={component.id} value={component.id}>{component.label}</option>)}</select></label>
            <label>查询动作<select value={binding.queryActionCode} onChange={(e) => updateTableBinding(index, {queryActionCode: e.target.value})}>{queryActions.map((action) => <option key={action.actionCode} value={action.actionCode}>{action.actionName} · {action.actionCode}</option>)}</select></label>
            <label>数据路径<input value={binding.itemsPath} onChange={(e) => updateTableBinding(index, {itemsPath: e.target.value})} /></label>
            <label>总数路径<input value={binding.totalPath} onChange={(e) => updateTableBinding(index, {totalPath: e.target.value})} /></label>
            <label>对象ID字段<input value={binding.objectIdField} onChange={(e) => updateTableBinding(index, {objectIdField: e.target.value})} /></label>
            <label>关系ID字段<input value={binding.relIdField} onChange={(e) => updateTableBinding(index, {relIdField: e.target.value})} /></label>
            <button type="button" className="icon-danger" onClick={() => onChange({...config, tableBindings: config.tableBindings.filter((_, itemIndex) => itemIndex !== index)})}>删除</button>
          </div>
        ))}
      </div>
    </section>
  );
}
