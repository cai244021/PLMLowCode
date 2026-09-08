import {useState} from 'react';
import type {PlmActionDefinition, PlmFieldDefinition, PlmPageConfig} from './types';

interface Props {
  config: PlmPageConfig;
  fields: PlmFieldDefinition[];
  actions: PlmActionDefinition[];
  onChange: (config: PlmPageConfig) => void;
}

export default function PageResources({config, fields, actions, onChange}: Props) {
  const [objectType, setObjectType] = useState('');
  const [fieldSearch, setFieldSearch] = useState('');
  const [actionKind, setActionKind] = useState('');
  const [actionSearch, setActionSearch] = useState('');
  const types = [...new Set(fields.map(field => field.objectType).filter(type => type && type !== '*'))].sort();
  const matches = (values: string[], query: string) => values.join(' ').toLowerCase().includes(query.trim().toLowerCase());
  const candidateFields = fields.filter(field => !config.fieldCodes.includes(field.fieldCode)
    && (!objectType || field.objectType === objectType || field.objectType === '*')
    && matches([field.fieldCode, field.displayName, field.schemaName], fieldSearch));
  const candidateActions = actions.filter(action => action.enabled && !config.actionCodes.includes(action.actionCode)
    && (!actionKind || action.actionKind === actionKind)
    && matches([action.actionCode, action.actionName, action.jpoName, action.methodName], actionSearch));

  return <>
    <div className="binding-section">
      <div className="section-title"><div><h3>本页字段 · {config.fieldCodes.length}</h3><p>从公共字段库选入；按类型筛选时同时包含通用字段（*）。可依次选入多个类型，不改变全局定义。</p></div></div>
      <div className="resource-columns">
        <div>
          <h4>候选字段</h4>
          <div className="inline-grid">
            <label>对象类型筛选<select value={objectType} onChange={event => setObjectType(event.target.value)}><option value="">全部对象类型</option>{types.map(type => <option key={type}>{type}</option>)}</select></label>
            <label>搜索候选字段<input value={fieldSearch} onChange={event => setFieldSearch(event.target.value)} placeholder="名称、编码或Scheme属性" /></label>
          </div>
          <div className="resource-list">
            {candidateFields.map(field => <div className="resource-item" key={field.fieldCode}><div><strong>{field.displayName}</strong><small>{field.objectType} · {field.fieldCode} · {field.schemaName}</small></div><button type="button" aria-label={`选入字段 ${field.fieldCode}`} onClick={() => onChange({...config, fieldCodes: [...config.fieldCodes, field.fieldCode]})}>选入</button></div>)}
            {!candidateFields.length && <p className="empty-tip">没有匹配的未选字段；可调整筛选或在公共字段库中维护。</p>}
          </div>
        </div>
        <div><h4>已选入本页</h4><div className="resource-list">
          {config.fieldCodes.map(code => {
            const field = fields.find(item => item.fieldCode === code);
            const used = config.fieldBindings.some(item => item.fieldCode === code);
            return <div className="resource-item" key={code}><div><strong>{field?.displayName || '字段定义缺失'}</strong><small>{code} · {field?.objectType || '请检查公共字段库'}{used ? ' · 已绑定，请先删除下方绑定再移除' : ''}</small></div><button type="button" disabled={used} aria-label={`移除字段 ${code}`} onClick={() => onChange({...config, fieldCodes: config.fieldCodes.filter(item => item !== code)})}>移除</button></div>;
          })}
          {!config.fieldCodes.length && <p className="empty-tip">尚未选入字段。下方组件绑定将只显示本页字段。</p>}
        </div></div>
      </div>
    </div>
    <div className="binding-section">
      <div className="section-title"><div><h3>本页动作 · {config.actionCodes.length}</h3><p>从公共JPO动作库选入；移除仅影响本页清单，不会删除公共动作。筛选不代表PLM执行权限。</p></div></div>
      <div className="resource-columns">
        <div><h4>候选动作</h4><div className="inline-grid">
          <label>动作类型筛选<select value={actionKind} onChange={event => setActionKind(event.target.value)}><option value="">全部动作类型</option><option value="CREATE">创建</option><option value="UPDATE">更新</option><option value="QUERY">查询</option><option value="ACTION">其他操作</option><option value="NAVIGATION">导航</option></select></label>
          <label>搜索候选动作<input value={actionSearch} onChange={event => setActionSearch(event.target.value)} placeholder="名称、编码、JPO或方法" /></label>
        </div><div className="resource-list">
          {candidateActions.map(action => <div className="resource-item" key={action.actionCode}><div><strong>{action.actionName}</strong><small>{action.actionKind} · {action.actionCode} · {action.jpoName}:{action.methodName}</small></div><button type="button" aria-label={`选入动作 ${action.actionCode}`} onClick={() => onChange({...config, actionCodes: [...config.actionCodes, action.actionCode]})}>选入</button></div>)}
          {!candidateActions.length && <p className="empty-tip">没有匹配的未选启用动作；可调整筛选或在公共动作库中维护。</p>}
        </div></div>
        <div><h4>已选入本页</h4><div className="resource-list">
          {config.actionCodes.map(code => {
            const action = actions.find(item => item.actionCode === code);
            const used = config.dataBindings.some(item => item.actionCode === code) || config.actionBindings.some(item => item.actionCode === code) || config.tableBindings.some(item => item.queryActionCode === code);
            return <div className="resource-item" key={code}><div><strong>{action?.actionName || '动作定义缺失'}</strong><small>{code}{!action ? ' · 请检查公共动作库' : !action.enabled ? ' · 已禁用，不能新增绑定' : ''}{used ? ' · 已绑定，请先删除下方绑定再移除' : ''}</small></div><button type="button" disabled={used} aria-label={`移除动作 ${code}`} onClick={() => onChange({...config, actionCodes: config.actionCodes.filter(item => item !== code)})}>移除</button></div>;
          })}
          {!config.actionCodes.length && <p className="empty-tip">尚未选入动作。Table查询只能选择本页已启用的查询动作。</p>}
        </div></div>
      </div>
    </div>
  </>;
}
