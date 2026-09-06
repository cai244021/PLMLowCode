import {useRef, useState} from 'react';
import axios from 'axios';
import type {PlmFieldDefinition} from './types';

interface Props {
  fields: PlmFieldDefinition[];
  onReload: () => Promise<void>;
  notify: (message: string) => void;
}

const newField = (): PlmFieldDefinition => ({
  fieldCode: '',
  displayName: '',
  objectType: '*',
  sourceType: 'ATTRIBUTE',
  schemaName: '',
  dataType: 'string',
  required: false,
  editable: true,
  multiple: false,
  i18nKey: '',
  rangeSource: 'NONE',
  rangeConfig: {}
});

export default function PlmFieldManager({fields, onReload, notify}: Props) {
  const [draft, setDraft] = useState<PlmFieldDefinition>(newField());
  const [rangeText, setRangeText] = useState('{}');
  const [editingCode, setEditingCode] = useState('');
  const formRef = useRef<HTMLDivElement>(null);
  const [notice, setNotice] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});

  const feedback = (message: string) => { setNotice(message); notify(message); };
  const errorText = (key: string) => errors[key] && <small className="validation-error">{errors[key]}</small>;

  const selectField = (field: PlmFieldDefinition) => {
    setErrors({});
    setNotice('');
    setDraft({...field});
    setRangeText(JSON.stringify(field.rangeConfig || {}, null, 2));
    setEditingCode(field.fieldCode);
  };

  const reset = () => {
    setErrors({});
    feedback('已进入新增字段，请填写红色星号必填项，再点击“保存字段”。');
    formRef.current?.scrollTo({top: 0});
    requestAnimationFrame(() => formRef.current?.querySelector<HTMLInputElement>('input')?.focus());
    setDraft(newField());
    setRangeText('{}');
    setEditingCode('');
  };

  const update = <K extends keyof PlmFieldDefinition>(key: K, value: PlmFieldDefinition[K]) => {
    setErrors(current => ({...current, [key]: ''}));
    setDraft((current) => ({...current, [key]: value}));
  };

  const save = async () => {
    const fieldCode = draft.fieldCode.trim().toUpperCase();
    const invalid: Record<string, string> = {};
    if (!/^[A-Z0-9_]{1,100}$/.test(fieldCode)) invalid.fieldCode = '请填写字段编码：1～100位大写字母、数字或下划线';
    if (!draft.displayName.trim()) invalid.displayName = '请填写显示名称';
    if (!draft.objectType.trim()) invalid.objectType = '请填写对象类型，通用字段填 *';
    if (!draft.schemaName.trim()) invalid.schemaName = '请填写Scheme字段';
    try {
      const rangeConfig = JSON.parse(rangeText || '{}');
      if (draft.rangeSource === 'PLM_RANGE' && !String(rangeConfig.attributeName || '').trim()) {
        invalid.rangeConfig = 'PLM属性Range必须配置attributeName，例如 JFAffectsFactory';
      }
      if (draft.rangeSource === 'PLM_STATE' && !String(rangeConfig.policyName || '').trim()) {
        invalid.rangeConfig = 'PLM状态必须配置policyName，例如 JFDA';
      }
    } catch { invalid.rangeConfig = 'Range配置不是有效JSON'; }
    setErrors(invalid);
    if (Object.keys(invalid).length) {
      feedback('未保存，请检查以下内容：' + Object.values(invalid).join('；'));
      requestAnimationFrame(() => formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus());
      return;
    }
    try {
      const rangeConfig = JSON.parse(rangeText || '{}');
      await axios.put(`/api/plm-fields/${fieldCode}`, {...draft, rangeConfig});
      feedback(`字段 ${fieldCode} 已保存`);
      await onReload();
      setEditingCode(fieldCode);
      setDraft((current) => ({...current, fieldCode}));
    } catch (error: any) {
      feedback(error instanceof SyntaxError
        ? 'Range配置不是有效JSON'
        : error.response?.data?.message || '字段保存失败');
    }
  };

  const remove = async () => {
    if (!editingCode || !window.confirm(`确定删除字段 ${editingCode}？`)) {
      return;
    }
    await axios.delete(`/api/plm-fields/${editingCode}`);
    notify(`字段 ${editingCode} 已删除`);
    reset();
    await onReload();
  };

  return (
    <section className="management-page">
      <aside className="catalog-list">
        <div className="panel-title">
          <div><strong>PLM字段库</strong><small>{fields.length} 个字段</small></div>
          <button type="button" onClick={reset}>新增字段</button>
        </div>
        {fields.map((field) => (
          <button
            type="button"
            key={field.fieldCode}
            className={`catalog-item ${editingCode === field.fieldCode ? 'selected' : ''}`}
            onClick={() => selectField(field)}
          >
            <strong>{field.displayName}</strong>
            <span>{field.fieldCode}</span>
            <small>{field.objectType} · {field.schemaName}</small>
          </button>
        ))}
      </aside>
      <div className="config-form" ref={formRef}>
        <div className="form-heading">
          <div><h2>{editingCode ? '编辑PLM字段' : '新增PLM字段'}</h2><p>维护AMIS字段与3DEXPERIENCE Scheme字段的映射元数据。</p></div>
          <div className="form-actions">
            {editingCode && <button type="button" className="danger" onClick={remove}>删除</button>}
            <button type="button" className="primary" onClick={save}>保存字段</button>
          </div>
        </div>
        <p className="required-help">红色 * 为必填项；“新增字段”用于清空表单，填写后请点击“保存字段”。</p>
        {notice && <div className="validation-notice" role="status">{notice}</div>}
        <div className="form-grid">
          <label><span>字段编码 {true && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={true} aria-invalid={Boolean(errors.fieldCode)} value={draft.fieldCode} disabled={Boolean(editingCode)} onChange={(e) => update('fieldCode', e.target.value.toUpperCase())} placeholder="例如 PART_TYPE" />{errorText('fieldCode')}</label>
          <label><span>显示名称 {true && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={true} aria-invalid={Boolean(errors.displayName)} value={draft.displayName} onChange={(e) => update('displayName', e.target.value)} placeholder="例如 零件子类型" />{errorText('displayName')}</label>
          <label><span>对象类型 {true && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={true} aria-invalid={Boolean(errors.objectType)} value={draft.objectType} onChange={(e) => update('objectType', e.target.value)} placeholder="例如 JF_CompetitiveBOM" />{errorText('objectType')}</label>
          <label><span>字段来源 <span className="required-mark" aria-hidden="true">*</span></span><select required value={draft.sourceType} onChange={(e) => update('sourceType', e.target.value as PlmFieldDefinition['sourceType'])}><option value="BASIC">对象基础字段</option><option value="ATTRIBUTE">对象属性</option><option value="RELATIONSHIP">关系属性</option><option value="PROGRAM">JPO计算字段</option></select></label>
          <label className="span-2"><span>Scheme字段 {true && <span className="required-mark" aria-hidden="true">*</span>}</span><input aria-required={true} aria-invalid={Boolean(errors.schemaName)} value={draft.schemaName} onChange={(e) => update('schemaName', e.target.value)} placeholder="例如 JF_VPMReference.JF_PartType" />{errorText('schemaName')}</label>
          <label><span>数据类型 <span className="required-mark" aria-hidden="true">*</span></span><select required value={draft.dataType} onChange={(e) => update('dataType', e.target.value as PlmFieldDefinition['dataType'])}><option value="string">单行文本</option><option value="textarea">多行文本</option><option value="number">数字</option><option value="boolean">布尔</option><option value="date">日期</option><option value="datetime">日期时间</option><option value="enum">枚举</option><option value="object">对象选择</option></select></label>
          <label><span>Range来源 <span className="required-mark" aria-hidden="true">*</span></span><select required value={draft.rangeSource} onChange={(e) => update('rangeSource', e.target.value as PlmFieldDefinition['rangeSource'])}><option value="NONE">无</option><option value="FIXED">固定选项</option><option value="PLM_RANGE">PLM属性Range</option><option value="PLM_STATE">PLM Policy状态</option><option value="JPO">JPO获取</option></select></label>
          <label className="span-2">国际化Key<input value={draft.i18nKey || ''} onChange={(e) => update('i18nKey', e.target.value)} placeholder="例如 emxFramework.Attribute.JF_PartType" /></label>
          <label className="span-2">Range配置JSON<textarea aria-invalid={Boolean(errors.rangeConfig)} rows={7} value={rangeText} onChange={(e) => { setRangeText(e.target.value); setErrors(current => ({...current, rangeConfig: ''})); }} placeholder={draft.rangeSource === 'PLM_RANGE' ? '{"attributeName":"JFAffectsFactory"}' : draft.rangeSource === 'PLM_STATE' ? '{"policyName":"JFDA"}' : '{}'} />{errorText('rangeConfig')}</label>
          <div className="check-row span-2">
            <label><input type="checkbox" checked={draft.required} onChange={(e) => update('required', e.target.checked)} />必填</label>
            <label><input type="checkbox" checked={draft.editable} onChange={(e) => update('editable', e.target.checked)} />可编辑</label>
            <label><input type="checkbox" checked={draft.multiple} onChange={(e) => update('multiple', e.target.checked)} />多值</label>
          </div>
        </div>
      </div>
    </section>
  );
}
