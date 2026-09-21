import {useRef, useState} from 'react';
import axios from 'axios';
import type {PlmActionDefinition, PlmActionParameterDefinition} from './types';

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
  inputParameters: [],
  outputParameters: [],
  enabled: true
});

const parameterTypes: PlmActionParameterDefinition['dataType'][] = ['ANY', 'STRING', 'NUMBER', 'BOOLEAN', 'OBJECT', 'ARRAY'];

const inferParameters = (
  parameters: PlmActionParameterDefinition[] | undefined,
  mapping: Record<string, unknown>
): PlmActionParameterDefinition[] => parameters || Object.keys(mapping || {}).map(name => ({
  name, dataType: 'ANY', required: false, description: ''
}));

const validateParameterContract = (
  parameters: PlmActionParameterDefinition[],
  mapping: unknown,
  label: string
): string => {
  if (!mapping || typeof mapping !== 'object' || Array.isArray(mapping)) return `${label}映射必须是JSON对象`;
  const names = parameters.map(parameter => parameter.name.trim());
  if (names.some(name => !/^[A-Za-z_][A-Za-z0-9_.-]{0,99}$/.test(name))) return `${label}参数名称不合法`;
  if (new Set(names).size !== names.length) return `${label}参数名称不能重复`;
  const mappingNames = Object.keys(mapping);
  if (names.some(name => !mappingNames.includes(name)) || mappingNames.some(name => !names.includes(name))) {
    return `${label}参数定义必须与映射名称一一对应`;
  }
  return '';
};

function ActionContractEditor({
  title,
  parameters,
  mappingText,
  mappingError,
  onParametersChange,
  onMappingTextChange
}: {
  title: string;
  parameters: PlmActionParameterDefinition[];
  mappingText: string;
  mappingError?: string;
  onParametersChange: (parameters: PlmActionParameterDefinition[]) => void;
  onMappingTextChange: (value: string) => void;
}) {
  let mapping: Record<string, unknown> | null = null;
  try {
    const parsed = JSON.parse(mappingText || '{}');
    if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) mapping = parsed;
  } catch {
    mapping = null;
  }
  const writeMapping = (next: Record<string, unknown>) => onMappingTextChange(JSON.stringify(next, null, 2));
  const updateParameter = (index: number, patch: Partial<PlmActionParameterDefinition>) => {
    onParametersChange(parameters.map((parameter, itemIndex) => itemIndex === index ? {...parameter, ...patch} : parameter));
  };
  const renameParameter = (index: number, name: string) => {
    const oldName = parameters[index].name;
    const nextName = name.trim();
    if (!nextName || parameters.some((parameter, itemIndex) => itemIndex !== index && parameter.name === nextName)) return false;
    updateParameter(index, {name: nextName});
    if (mapping) {
      const next: Record<string, unknown> = {};
      Object.entries(mapping).forEach(([key, value]) => { next[key === oldName ? nextName : key] = value; });
      writeMapping(next);
    }
    return true;
  };
  const addParameter = () => {
    let index = 1;
    let name = `param${index}`;
    while (parameters.some(parameter => parameter.name === name)) name = `param${++index}`;
    onParametersChange([...parameters, {name, dataType: 'ANY', required: false, description: ''}]);
    if (mapping) writeMapping({...mapping, [name]: `\${${name}}`});
  };
  const removeParameter = (index: number) => {
    const name = parameters[index].name;
    onParametersChange(parameters.filter((_, itemIndex) => itemIndex !== index));
    if (mapping) {
      const next = {...mapping};
      delete next[name];
      writeMapping(next);
    }
  };
  const syncFromMapping = () => {
    if (!mapping) return;
    onParametersChange(Object.keys(mapping).map(name => parameters.find(parameter => parameter.name === name)
      || {name, dataType: 'ANY', required: false, description: ''}));
  };

  return <div className="action-contract span-2">
    <div className="parameter-mapping-head"><div><strong>{title}</strong><small>参数定义与映射名称必须一一对应。</small></div><div className="form-actions"><button type="button" onClick={syncFromMapping} disabled={!mapping}>从映射同步参数</button><button type="button" onClick={addParameter} disabled={!mapping}>新增参数</button></div></div>
    {parameters.map((parameter, index) => <div className="action-contract-row" key={`${parameter.name}-${index}`}>
      <label>参数名称<input defaultValue={parameter.name} onBlur={event => {
        if (!renameParameter(index, event.currentTarget.value)) {
          event.currentTarget.value = parameter.name;
          event.currentTarget.setCustomValidity('参数名称不能为空或重复');
          event.currentTarget.reportValidity();
        } else event.currentTarget.setCustomValidity('');
      }} /></label>
      <label>数据类型<select value={parameter.dataType} onChange={event => updateParameter(index, {dataType: event.target.value as PlmActionParameterDefinition['dataType']})}>{parameterTypes.map(type => <option key={type} value={type}>{type}</option>)}</select></label>
      <label className="contract-required"><input type="checkbox" checked={parameter.required} onChange={event => updateParameter(index, {required: event.target.checked})} />必填</label>
      <label>说明<input value={parameter.description} onChange={event => updateParameter(index, {description: event.target.value})} maxLength={500} /></label>
      <button type="button" className="icon-danger" onClick={() => removeParameter(index)}>删除</button>
    </div>)}
    {!parameters.length && <div className="empty-tip">尚未定义参数。可以从映射同步，或新增参数。</div>}
    <details className="advanced-mapping"><summary>高级映射JSON</summary><label>{title}映射<textarea aria-invalid={Boolean(mappingError)} rows={8} value={mappingText} onChange={event => onMappingTextChange(event.target.value)} />{mappingError && <small className="validation-error">{mappingError}</small>}</label></details>
  </div>;
}

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
    setDraft({...action,
      inputParameters: inferParameters(action.inputParameters, action.inputMapping),
      outputParameters: inferParameters(action.outputParameters, action.outputMapping)
    });
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
    let inputMapping: Record<string, unknown> = {};
    let outputMapping: Record<string, unknown> = {};
    if (!/^[A-Z0-9_]{1,100}$/.test(actionCode)) invalid.actionCode = '请填写动作编码：1～100位大写字母、数字或下划线';
    if (!draft.actionName.trim()) invalid.actionName = '请填写动作名称';
    if (!draft.jpoName.trim()) invalid.jpoName = '请填写JPO名称';
    if (!draft.methodName.trim()) invalid.methodName = '请填写执行方法';
    try { inputMapping = JSON.parse(inputText || '{}'); } catch { invalid.inputMapping = '输入参数映射不是有效JSON'; }
    try { outputMapping = JSON.parse(outputText || '{}'); } catch { invalid.outputMapping = '输出结果映射不是有效JSON'; }
    if (!invalid.inputMapping) invalid.inputMapping = validateParameterContract(draft.inputParameters, inputMapping, '输入');
    if (!invalid.outputMapping) invalid.outputMapping = validateParameterContract(draft.outputParameters, outputMapping, '输出');
    Object.keys(invalid).forEach(key => { if (!invalid[key]) delete invalid[key]; });
    setErrors(invalid);
    if (Object.keys(invalid).length) {
      feedback('未保存，请检查以下内容：' + Object.values(invalid).join('；'));
      requestAnimationFrame(() => formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus());
      return;
    }
    try {
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
            <small>{action.actionKind} · {action.jpoName}.{action.methodName}</small>
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
        <p className="required-help">红色 * 为必填项；页面跳转请在事件成功效果中选择“页面跳转”。</p>
        {notice && <div className="validation-notice" role="status">{notice}</div>}
        <div className="form-grid">
          <label><span>动作编码 {true && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={true} aria-invalid={Boolean(errors.actionCode)} value={draft.actionCode} disabled={Boolean(editingCode)} onChange={(e) => update('actionCode', e.target.value.toUpperCase())} placeholder="例如 CREATE_PART" />{errorText('actionCode')}</label>
          <label><span>动作名称 {true && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={true} aria-invalid={Boolean(errors.actionName)} value={draft.actionName} onChange={(e) => update('actionName', e.target.value)} placeholder="例如 创建零件" />{errorText('actionName')}</label>
          <label><span>动作类型 <span className="required-mark" aria-hidden="true">*</span></span><select required value={draft.actionKind} onChange={(e) => update('actionKind', e.target.value as PlmActionDefinition['actionKind'])}><option value="CREATE">创建</option><option value="UPDATE">更新</option><option value="QUERY">查询</option><option value="ACTION">业务操作</option></select></label>
          <label><span>请求方式 <span className="required-mark" aria-hidden="true">*</span></span><select required value={draft.httpMethod} onChange={(e) => update('httpMethod', e.target.value as PlmActionDefinition['httpMethod'])}><option value="POST">POST（受控动作）</option></select></label>
          <label><span>JPO名称 <span className="required-mark" aria-hidden="true">*</span></span><input aria-required={true} aria-invalid={Boolean(errors.jpoName)} value={draft.jpoName || ''} onChange={(e) => update('jpoName', e.target.value)} placeholder="例如 JF_CompetitiveBOM" />{errorText('jpoName')}</label>
          <label><span>执行方法 <span className="required-mark" aria-hidden="true">*</span></span><input aria-required={true} aria-invalid={Boolean(errors.methodName)} value={draft.methodName || ''} onChange={(e) => update('methodName', e.target.value)} placeholder="例如 createCompetitiveBOM" />{errorText('methodName')}</label>
          <ActionContractEditor title="输入参数契约" parameters={draft.inputParameters} mappingText={inputText} mappingError={errors.inputMapping} onParametersChange={value => update('inputParameters', value)} onMappingTextChange={value => { setInputText(value); setErrors(current => ({...current, inputMapping: ''})); }} />
          <ActionContractEditor title="输出参数契约" parameters={draft.outputParameters} mappingText={outputText} mappingError={errors.outputMapping} onParametersChange={value => update('outputParameters', value)} onMappingTextChange={value => { setOutputText(value); setErrors(current => ({...current, outputMapping: ''})); }} />
          <div className="check-row span-2"><label><input type="checkbox" checked={draft.enabled} onChange={(e) => update('enabled', e.target.checked)} />启用该动作</label></div>
        </div>
      </div>
    </section>
  );
}
