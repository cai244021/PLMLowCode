import {useRef, useState} from 'react';
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
  const formRef = useRef<HTMLDivElement>(null);
  const [notice, setNotice] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const feedback = (message: string) => { setNotice(message); notify(message); };
  const errorText = (key: string) => errors[key] && <small className="validation-error">{errors[key]}</small>;

  const selectAction = (action: PlmActionDefinition) => {
    setErrors({});
    setNotice('');
    setDraft({...action});
    setInputText(JSON.stringify(action.inputMapping || {}, null, 2));
    setOutputText(JSON.stringify(action.outputMapping || {}, null, 2));
    setEditingCode(action.actionCode);
  };

  const reset = () => {
    setErrors({});
    feedback('已进入新增动作，请填写红色星号必填项，再点击“保存动作”。');
    formRef.current?.scrollTo({top: 0});
    requestAnimationFrame(() => formRef.current?.querySelector<HTMLInputElement>('input')?.focus());
    setDraft(newAction());
    setInputText('{}');
    setOutputText('{}');
    setEditingCode('');
  };

  const update = <K extends keyof PlmActionDefinition>(key: K, value: PlmActionDefinition[K]) => {
    setErrors(current => key === 'actionKind' ? {} : {...current, [key]: ''});
    setDraft((current) => ({...current, [key]: value}));
  };

  const save = async () => {
    const actionCode = draft.actionCode.trim().toUpperCase();
    const invalid: Record<string, string> = {};
    if (!/^[A-Z0-9_]{1,100}$/.test(actionCode)) invalid.actionCode = '请填写动作编码：1～100位大写字母、数字或下划线';
    if (!draft.actionName.trim()) invalid.actionName = '请填写动作名称';
    if (draft.actionKind !== 'NAVIGATION') {
      if (!draft.jpoName.trim()) invalid.jpoName = '请填写JPO名称';
      if (!draft.methodName.trim()) invalid.methodName = '请填写执行方法';
    }
    try { JSON.parse(inputText || '{}'); } catch { invalid.inputMapping = '输入参数映射不是有效JSON'; }
    try { JSON.parse(outputText || '{}'); } catch { invalid.outputMapping = '输出结果映射不是有效JSON'; }
    setErrors(invalid);
    if (Object.keys(invalid).length) {
      feedback('未保存，请检查以下内容：' + Object.values(invalid).join('；'));
      requestAnimationFrame(() => formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus());
      return;
    }
    try {
      const inputMapping = JSON.parse(inputText || '{}');
      const outputMapping = JSON.parse(outputText || '{}');
      await axios.put(`/api/plm-actions/${actionCode}`, {...draft, inputMapping, outputMapping});
      feedback(`动作 ${actionCode} 已保存`);
      await onReload();
      setEditingCode(actionCode);
      setDraft((current) => ({...current, actionCode}));
    } catch (error: any) {
      feedback(error instanceof SyntaxError
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
      <div className="config-form" ref={formRef}>
        <div className="form-heading">
          <div><h2>{editingCode ? '编辑JPO动作' : '新增JPO动作'}</h2><p>运行端通过actionCode查找受控的JPO和方法。</p></div>
          <div className="form-actions">
            {editingCode && <button type="button" className="danger" onClick={remove}>删除</button>}
            <button type="button" className="primary" onClick={save}>保存动作</button>
          </div>
        </div>
        <p className="required-help">红色 * 为必填项；页面跳转动作不要求JPO名称和执行方法。</p>
        {notice && <div className="validation-notice" role="status">{notice}</div>}
        <div className="form-grid">
          <label><span>动作编码 {true && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={true} aria-invalid={Boolean(errors.actionCode)} value={draft.actionCode} disabled={Boolean(editingCode)} onChange={(e) => update('actionCode', e.target.value.toUpperCase())} placeholder="例如 CREATE_PART" />{errorText('actionCode')}</label>
          <label><span>动作名称 {true && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={true} aria-invalid={Boolean(errors.actionName)} value={draft.actionName} onChange={(e) => update('actionName', e.target.value)} placeholder="例如 创建零件" />{errorText('actionName')}</label>
          <label><span>动作类型 <span className="required-mark" aria-hidden="true">*</span></span><select required value={draft.actionKind} onChange={(e) => update('actionKind', e.target.value as PlmActionDefinition['actionKind'])}><option value="CREATE">创建</option><option value="UPDATE">更新</option><option value="QUERY">查询</option><option value="ACTION">业务操作</option><option value="NAVIGATION">页面跳转</option></select></label>
          <label><span>请求方式 <span className="required-mark" aria-hidden="true">*</span></span><select required value={draft.httpMethod} onChange={(e) => update('httpMethod', e.target.value as PlmActionDefinition['httpMethod'])}><option value="POST">POST（受控动作）</option></select></label>
          <label><span>JPO名称 {draft.actionKind !== 'NAVIGATION' && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={draft.actionKind !== 'NAVIGATION'} aria-invalid={Boolean(errors.jpoName)} value={draft.jpoName || ''} onChange={(e) => update('jpoName', e.target.value)} placeholder="例如 JF_CompetitiveBOM" />{errorText('jpoName')}</label>
          <label><span>执行方法 {draft.actionKind !== 'NAVIGATION' && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={draft.actionKind !== 'NAVIGATION'} aria-invalid={Boolean(errors.methodName)} value={draft.methodName || ''} onChange={(e) => update('methodName', e.target.value)} placeholder="例如 createCompetitiveBOM" />{errorText('methodName')}</label>
          <label>输入参数映射JSON<textarea aria-invalid={Boolean(errors.inputMapping)} rows={10} value={inputText} onChange={(e) => { setInputText(e.target.value); setErrors(current => ({...current, inputMapping: ''})); }} />{errorText('inputMapping')}</label>
          <label>输出结果映射JSON<textarea aria-invalid={Boolean(errors.outputMapping)} rows={10} value={outputText} onChange={(e) => { setOutputText(e.target.value); setErrors(current => ({...current, outputMapping: ''})); }} />{errorText('outputMapping')}</label>
          <div className="check-row span-2"><label><input type="checkbox" checked={draft.enabled} onChange={(e) => update('enabled', e.target.checked)} />启用该动作</label></div>
        </div>
      </div>
    </section>
  );
}
