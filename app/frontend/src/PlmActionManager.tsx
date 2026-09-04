import {useState} from 'react';
import axios from 'axios';
import type {PlmActionDefinition} from './types';

interface Props {
  actions: PlmActionDefinition[];
  onReload: () => Promise<void>;
  notify: (message: string) => void;
}

const newAction = (): PlmActionDefinition => ({
  actionCode: '',
  actionName: '',
  actionKind: 'ACTION',
  jpoName: '',
  methodName: '',
  httpMethod: 'POST',
  inputMapping: {},
  outputMapping: {},
  enabled: true
});

export default function PlmActionManager({actions, onReload, notify}: Props) {
  const [draft, setDraft] = useState<PlmActionDefinition>(newAction());
  const [inputText, setInputText] = useState('{}');
  const [outputText, setOutputText] = useState('{}');
  const [editingCode, setEditingCode] = useState('');

  const selectAction = (action: PlmActionDefinition) => {
    setDraft({...action});
    setInputText(JSON.stringify(action.inputMapping || {}, null, 2));
    setOutputText(JSON.stringify(action.outputMapping || {}, null, 2));
    setEditingCode(action.actionCode);
  };

  const reset = () => {
    setDraft(newAction());
    setInputText('{}');
    setOutputText('{}');
    setEditingCode('');
  };

  const update = <K extends keyof PlmActionDefinition>(key: K, value: PlmActionDefinition[K]) => {
    setDraft((current) => ({...current, [key]: value}));
  };

  const save = async () => {
    const actionCode = draft.actionCode.trim().toUpperCase();
    if (!/^[A-Z0-9_]{1,100}$/.test(actionCode)) {
      notify('动作编码只能使用大写字母、数字和下划线');
      return;
    }
    if (!draft.actionName.trim()) {
      notify('动作名称不能为空');
      return;
    }
    if (draft.actionKind !== 'NAVIGATION' && (!draft.jpoName.trim() || !draft.methodName.trim())) {
      notify('非跳转动作必须配置JPO名称和执行方法');
      return;
    }
    try {
      const inputMapping = JSON.parse(inputText || '{}');
      const outputMapping = JSON.parse(outputText || '{}');
      await axios.put(`/api/plm-actions/${actionCode}`, {...draft, inputMapping, outputMapping});
      notify(`动作 ${actionCode} 已保存`);
      await onReload();
      setEditingCode(actionCode);
      setDraft((current) => ({...current, actionCode}));
    } catch (error: any) {
      notify(error instanceof SyntaxError
        ? '输入或输出映射不是有效JSON'
        : error.response?.data?.message || '动作保存失败');
    }
  };

  const remove = async () => {
    if (!editingCode || !window.confirm(`确定删除动作 ${editingCode}？`)) {
      return;
    }
    await axios.delete(`/api/plm-actions/${editingCode}`);
    notify(`动作 ${editingCode} 已删除`);
    reset();
    await onReload();
  };

  return (
    <section className="management-page">
      <aside className="catalog-list">
        <div className="panel-title">
          <div><strong>JPO动作库</strong><small>{actions.length} 个动作</small></div>
          <button type="button" onClick={reset}>新增动作</button>
        </div>
        {actions.map((action) => (
          <button
            type="button"
            key={action.actionCode}
            className={`catalog-item ${editingCode === action.actionCode ? 'selected' : ''}`}
            onClick={() => selectAction(action)}
          >
            <strong>{action.actionName}</strong>
            <span>{action.actionCode}</span>
            <small>{action.actionKind} · {action.jpoName || '页面跳转'}.{action.methodName || ''}</small>
          </button>
        ))}
      </aside>
      <div className="config-form">
        <div className="form-heading">
          <div><h2>{editingCode ? '编辑JPO动作' : '新增JPO动作'}</h2><p>运行端通过actionCode查找受控的JPO和方法。</p></div>
          <div className="form-actions">
            {editingCode && <button type="button" className="danger" onClick={remove}>删除</button>}
            <button type="button" className="primary" onClick={save}>保存动作</button>
          </div>
        </div>
        <div className="form-grid">
          <label>动作编码<input value={draft.actionCode} disabled={Boolean(editingCode)} onChange={(e) => update('actionCode', e.target.value.toUpperCase())} placeholder="例如 CREATE_PART" /></label>
          <label>动作名称<input value={draft.actionName} onChange={(e) => update('actionName', e.target.value)} placeholder="例如 创建零件" /></label>
          <label>动作类型<select value={draft.actionKind} onChange={(e) => update('actionKind', e.target.value as PlmActionDefinition['actionKind'])}><option value="CREATE">创建</option><option value="UPDATE">更新</option><option value="QUERY">查询</option><option value="ACTION">业务操作</option><option value="NAVIGATION">页面跳转</option></select></label>
          <label>请求方式<select value={draft.httpMethod} onChange={(e) => update('httpMethod', e.target.value as PlmActionDefinition['httpMethod'])}><option value="POST">POST</option><option value="GET">GET</option></select></label>
          <label>JPO名称<input value={draft.jpoName || ''} onChange={(e) => update('jpoName', e.target.value)} placeholder="例如 JF_CompetitiveBOM" /></label>
          <label>执行方法<input value={draft.methodName || ''} onChange={(e) => update('methodName', e.target.value)} placeholder="例如 createCompetitiveBOM" /></label>
          <label>输入参数映射JSON<textarea rows={10} value={inputText} onChange={(e) => setInputText(e.target.value)} /></label>
          <label>输出结果映射JSON<textarea rows={10} value={outputText} onChange={(e) => setOutputText(e.target.value)} /></label>
          <div className="check-row span-2"><label><input type="checkbox" checked={draft.enabled} onChange={(e) => update('enabled', e.target.checked)} />启用该动作</label></div>
        </div>
      </div>
    </section>
  );
}
