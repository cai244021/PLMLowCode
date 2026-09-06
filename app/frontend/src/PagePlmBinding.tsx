import {useMemo} from 'react';
import type {SchemaObject} from 'amis';
import {collectPageComponents, collectTableColumns} from './schemaComponents';
import PageResources from './PageResources';
import type {
  ActionBinding,
  DataBinding,
  FieldBinding,
  PlmActionDefinition,
  PlmFieldDefinition,
  PlmPageConfig,
  SearchBinding,
  TableBinding
} from './types';

interface Props {
  pageCode: string;
  pageName: string;
  schema: SchemaObject;
  config: PlmPageConfig;
  fields: PlmFieldDefinition[];
  actions: PlmActionDefinition[];
  onChange: (config: PlmPageConfig) => void;
  onSave: () => Promise<void>;
  onExport: () => void;
}

export default function PagePlmBinding({pageCode, pageName, schema, config, fields, actions, onChange, onSave, onExport}: Props) {
  const components = useMemo(() => collectPageComponents(schema), [schema]);
  const pageFields = fields.filter(field => config.fieldCodes.includes(field.fieldCode));
  const enabledActions = actions.filter((action) => action.enabled && config.actionCodes.includes(action.actionCode));
  const queryActions = enabledActions.filter((action) => action.actionKind === 'QUERY');

  const updateContext = (key: keyof PlmPageConfig['context'], value: string) => {
    onChange({...config, context: {...config.context, [key]: value}});
  };

  const updateFieldBinding = (index: number, patch: Partial<FieldBinding>) => {
    onChange({...config, fieldBindings: config.fieldBindings.map((item, itemIndex) => itemIndex === index ? {...item, ...patch} : item)});
  };

  const updateDataBinding = (index: number, patch: Partial<DataBinding>) => {
    onChange({...config, dataBindings: config.dataBindings.map((item, itemIndex) => itemIndex === index ? {...item, ...patch} : item)});
  };

  const updateActionBinding = (index: number, patch: Partial<ActionBinding>) => {
    onChange({...config, actionBindings: config.actionBindings.map((item, itemIndex) => itemIndex === index ? {...item, ...patch} : item)});
  };

  const updateTableBinding = (index: number, patch: Partial<TableBinding>) => {
    onChange({...config, tableBindings: config.tableBindings.map((item, itemIndex) => itemIndex === index ? {...item, ...patch} : item)});
  };

  const updateSearchBinding = (index: number, patch: Partial<SearchBinding>) => {
    onChange({...config, searchBindings: config.searchBindings.map((item, itemIndex) => itemIndex === index ? {...item, ...patch} : item)});
  };

  const addFieldBinding = () => {
    const component = components.fields[0];
    const field = pageFields[0];
    if (!component || !field) return;
    onChange({...config, fieldBindings: [...config.fieldBindings, {componentId: component.id, fieldCode: field.fieldCode, valueKey: component.name || field.fieldCode.toLowerCase()}]});
  };

  const addDataBinding = () => {
    const component = components.data[0];
    const action = queryActions[0];
    if (!component || !action) return;
    onChange({...config, dataBindings: [...config.dataBindings, {componentId: component.id, trigger: 'INIT', actionCode: action.actionCode}]});
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
    onChange({...config, tableBindings: [...config.tableBindings, {componentId: component.id, queryActionCode: action.actionCode, itemsPath: 'data.items', totalPath: 'data.total', objectIdField: 'id', relIdField: 'id[connection]', columnBindings: []}]});
  };

  const addSearchBinding = () => {
    const button = components.actions.find((component) => component.type === 'button');
    const form = components.actions.find((component) => component.type === 'form');
    if (!button || !form) return;
    onChange({...config, searchBindings: [...config.searchBindings, {
      componentId: button.id,
      formId: form.id,
      valueField: 'objectId',
      labelField: 'displayName',
      searchParams: 'field=TYPES=type_ProjectSpace&table=AEFGeneralSearchResults&includeOIDprogram=JF_PublicMethodClass:getSystemAllProjectSpaces&showInitialResults=true&selection=single'
    }]});
  };

  return (
    <section className="binding-page">
      <div className="binding-heading">
        <div><h2>当前页面PLM绑定</h2><p>{pageName} · {pageCode}</p><p>本页资源清单及绑定与页面版本一起保存，不影响其他页面。</p></div>
        <div className="form-actions"><button type="button" onClick={onExport}>导出配置包</button><button type="button" className="primary" onClick={onSave}>保存页面及绑定</button></div>
      </div>

      <PageResources config={config} fields={fields} actions={actions} onChange={onChange} />

      <div className="summary-cards">
        <div><strong>{components.fields.length}</strong><span>可绑定字段组件</span></div>
        <div><strong>{components.data.length}</strong><span>初始化查询组件</span></div>
        <div><strong>{components.actions.length}</strong><span>按钮/表单组件</span></div>
        <div><strong>{components.tables.length}</strong><span>Table组件</span></div>
        <div><strong>{config.fieldBindings.length + config.dataBindings.length + config.actionBindings.length + config.tableBindings.length + config.searchBindings.length}</strong><span>已配置绑定</span></div>
      </div>

      <div className="binding-section">
        <div className="section-title"><div><h3>3DSpace原生搜索</h3><p>将AMIS按钮绑定到emxFullSearch.jsp；多个参数使用 &amp; 连接。field、table、form、includeOIDprogram、excludeOIDprogram、showInitialResults等业务参数会透传，submitURL和requestId由Runtime统一接管。</p></div><button type="button" onClick={addSearchBinding} disabled={!components.actions.some((component) => component.type === 'button') || !components.actions.some((component) => component.type === 'form')}>新增搜索绑定</button></div>
        {config.searchBindings.map((binding, index) => (
          <div className="binding-row search-row" key={`${binding.componentId}-${index}`}>
            <label>搜索按钮<select value={binding.componentId} onChange={(e) => updateSearchBinding(index, {componentId: e.target.value})}>{components.actions.filter((component) => component.type === 'button').map((component) => <option key={component.id} value={component.id}>{component.label}</option>)}</select></label>
            <label>回填表单<select value={binding.formId} onChange={(e) => updateSearchBinding(index, {formId: e.target.value})}>{components.actions.filter((component) => component.type === 'form').map((component) => <option key={component.id} value={component.id}>{component.label}</option>)}</select></label>
            <label>对象ID字段<input value={binding.valueField} onChange={(e) => updateSearchBinding(index, {valueField: e.target.value})} placeholder="projectId" /></label>
            <label>显示名称字段<input value={binding.labelField} onChange={(e) => updateSearchBinding(index, {labelField: e.target.value})} placeholder="projectName" /></label>
            <label className="search-params">emxFullSearch参数<textarea rows={4} value={binding.searchParams} onChange={(e) => updateSearchBinding(index, {searchParams: e.target.value})} placeholder="field=TYPES=type_ProjectSpace&table=AEFGeneralSearchResults&includeOIDprogram=JF_PublicMethodClass:getSystemAllProjectSpaces&showInitialResults=true&selection=single" /></label>
            <button type="button" className="icon-danger" onClick={() => onChange({...config, searchBindings: config.searchBindings.filter((_, itemIndex) => itemIndex !== index)})}>删除</button>
          </div>
        ))}
      </div>

      <div className="binding-section">
        <div className="section-title"><div><h3>页面初始化查询</h3><p>页面打开时调用一次查询动作，并将返回的data写入对应AMIS Service的数据域。</p></div><button type="button" onClick={addDataBinding} disabled={!components.data.length || !queryActions.length}>新增初始化查询</button></div>
        {!components.data.length && <div className="empty-tip">当前页面没有带稳定ID的Service组件，请先在页面设计中加入服务Service。</div>}
        {config.dataBindings.map((binding, index) => (
          <div className="binding-row data-row" key={`${binding.componentId}-${index}`}>
            <label>Service组件<select value={binding.componentId} onChange={(e) => updateDataBinding(index, {componentId: e.target.value})}>{components.data.map((component) => <option key={component.id} value={component.id}>{component.label}</option>)}</select></label>
            <label>触发时机<select value={binding.trigger} onChange={(e) => updateDataBinding(index, {trigger: e.target.value as DataBinding['trigger']})}><option value="INIT">页面初始化</option></select></label>
            <label>查询动作<select value={binding.actionCode} onChange={(e) => updateDataBinding(index, {actionCode: e.target.value})}>{!queryActions.some(action => action.actionCode === binding.actionCode) && <option value={binding.actionCode} disabled>{binding.actionCode} · 非可用查询动作</option>}{queryActions.map((action) => <option key={action.actionCode} value={action.actionCode}>{action.actionName} · {action.actionCode}</option>)}</select></label>
            <button type="button" className="icon-danger" onClick={() => onChange({...config, dataBindings: config.dataBindings.filter((_, itemIndex) => itemIndex !== index)})}>删除</button>
          </div>
        ))}
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
        <div className="section-title"><div><h3>表单字段绑定</h3><p>将AMIS输入控件绑定到本页已选入的PLM字段；请先在上方选入字段。</p></div><button type="button" onClick={addFieldBinding} disabled={!components.fields.length || !pageFields.length}>新增绑定</button></div>
        {!components.fields.length && <div className="empty-tip">当前页面没有输入控件，请先在页面设计中加入文本框、下拉框等组件。</div>}
        {config.fieldBindings.map((binding, index) => (
          <div className="binding-row field-row" key={`${binding.componentId}-${index}`}>
            <label>页面组件<select value={binding.componentId} onChange={(e) => updateFieldBinding(index, {componentId: e.target.value})}>{components.fields.map((component) => <option key={component.id} value={component.id}>{component.label}</option>)}</select></label>
            <label>PLM字段<select value={binding.fieldCode} onChange={(e) => updateFieldBinding(index, {fieldCode: e.target.value})}>{!pageFields.some(field => field.fieldCode === binding.fieldCode) && <option value={binding.fieldCode} disabled>{binding.fieldCode} · 定义缺失</option>}{pageFields.map((field) => <option key={field.fieldCode} value={field.fieldCode}>{field.displayName} · {field.schemaName}</option>)}</select></label>
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
            <label>PLM动作<select value={binding.actionCode} onChange={(e) => updateActionBinding(index, {actionCode: e.target.value})}>{!enabledActions.some(action => action.actionCode === binding.actionCode) && <option value={binding.actionCode} disabled>{binding.actionCode} · 已禁用或缺失</option>}{enabledActions.map((action) => <option key={action.actionCode} value={action.actionCode}>{action.actionName} · {action.actionCode}</option>)}</select></label>
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
            <label>查询动作<select value={binding.queryActionCode} onChange={(e) => updateTableBinding(index, {queryActionCode: e.target.value})}>{!queryActions.some(action => action.actionCode === binding.queryActionCode) && <option value={binding.queryActionCode} disabled>{binding.queryActionCode} · 非可用查询动作</option>}{queryActions.map((action) => <option key={action.actionCode} value={action.actionCode}>{action.actionName} · {action.actionCode}</option>)}</select></label>
            <label>数据路径<input value={binding.itemsPath} onChange={(e) => updateTableBinding(index, {itemsPath: e.target.value})} /></label>
            <label>总数路径<input value={binding.totalPath} onChange={(e) => updateTableBinding(index, {totalPath: e.target.value})} /></label>
            <label>对象ID字段<input value={binding.objectIdField} onChange={(e) => updateTableBinding(index, {objectIdField: e.target.value})} /></label>
            <label>关系ID字段<input value={binding.relIdField} onChange={(e) => updateTableBinding(index, {relIdField: e.target.value})} /></label>
            <div className="span-all">
              <div className="section-title"><div><strong>列字段与国际化</strong><p>列名直接填写JPO MapList原始key，例如 attribute[JFChangeType]；标题和Range显示值由PLM字段定义自动解析。</p></div><button type="button" disabled={!collectTableColumns(schema, binding.componentId).length || !pageFields.length} onClick={() => {
                const column = collectTableColumns(schema, binding.componentId)[0];
                const field = pageFields[0];
                if (column && field) updateTableBinding(index, {columnBindings: [...(binding.columnBindings || []), {columnName: column.name, fieldCode: field.fieldCode}]});
              }}>新增列绑定</button></div>
              {(binding.columnBindings || []).map((columnBinding, columnIndex) => (
                <div className="binding-row field-row" key={`${columnBinding.columnName}-${columnIndex}`}>
                  <label>Table列<select value={columnBinding.columnName} onChange={(e) => updateTableBinding(index, {columnBindings: binding.columnBindings.map((item, itemIndex) => itemIndex === columnIndex ? {...item, columnName: e.target.value} : item)})}>{collectTableColumns(schema, binding.componentId).map(column => <option key={column.name} value={column.name}>{column.label}</option>)}</select></label>
                  <label>PLM字段<select value={columnBinding.fieldCode} onChange={(e) => updateTableBinding(index, {columnBindings: binding.columnBindings.map((item, itemIndex) => itemIndex === columnIndex ? {...item, fieldCode: e.target.value} : item)})}>{pageFields.map(field => <option key={field.fieldCode} value={field.fieldCode}>{field.displayName} · {field.schemaName}</option>)}</select></label>
                  <button type="button" className="icon-danger" onClick={() => updateTableBinding(index, {columnBindings: binding.columnBindings.filter((_, itemIndex) => itemIndex !== columnIndex)})}>删除列绑定</button>
                </div>
              ))}
            </div>
            <button type="button" className="icon-danger" onClick={() => onChange({...config, tableBindings: config.tableBindings.filter((_, itemIndex) => itemIndex !== index)})}>删除</button>
          </div>
        ))}
      </div>
    </section>
  );
}
