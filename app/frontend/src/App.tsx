import {useCallback, useEffect, useMemo, useState} from 'react';
import axios from 'axios';
import {Editor} from 'amis-editor';
import type {SchemaObject} from 'amis';
import PagePlmBinding from './PagePlmBinding';
import PlmActionManager from './PlmActionManager';
import PlmFieldManager from './PlmFieldManager';
import {
  emptyPlmConfig,
  normalizePlmConfig,
  type PageResponse,
  type PlmActionDefinition,
  type PlmFieldDefinition,
  type PlmPageConfig
} from './types';

const DEFAULT_PAGE_CODE = 'JF_COMPETITIVE_BOM_CREATE';
type PageSummary = Pick<PageResponse, 'pageCode' | 'pageName' | 'currentVersion'> & {updatedAt?: string};
const snapshot = (pageName: string, schema: SchemaObject, plmConfig: PlmPageConfig) =>
  JSON.stringify({pageName, schema, plmConfig});

const initialSchema: SchemaObject = {
  type: 'page',
  title: 'PLM低代码页面',
  body: [
    {
      type: 'alert',
      level: 'info',
      body: '请从左侧拖入组件，设计完成后点击保存。'
    }
  ]
};

type WorkspaceView = 'DESIGNER' | 'FIELDS' | 'ACTIONS' | 'BINDINGS' | 'PAGES';

export default function App() {
  const [pageCode, setPageCode] = useState('');
  const [pages, setPages] = useState<PageSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [savedSnapshot, setSavedSnapshot] = useState('');
  const [createMode, setCreateMode] = useState<'NEW' | 'COPY' | null>(null);
  const [newCode, setNewCode] = useState('');
  const [newName, setNewName] = useState('');
  const [createError, setCreateError] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<PageSummary | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [schema, setSchema] = useState<SchemaObject>(initialSchema);
  const [pageName, setPageName] = useState('示例页面');
  const [version, setVersion] = useState(0);
  const [message, setMessage] = useState('正在读取页面...');
  const [saving, setSaving] = useState(false);
  const [isPreview, setIsPreview] = useState(false);
  const [view, setView] = useState<WorkspaceView>('DESIGNER');
  const [plmConfig, setPlmConfig] = useState<PlmPageConfig>(emptyPlmConfig());
  const [fields, setFields] = useState<PlmFieldDefinition[]>([]);
  const [actions, setActions] = useState<PlmActionDefinition[]>([]);
  const dirty = !!pageCode && savedSnapshot !== snapshot(pageName, schema, plmConfig);
  const busy = loading || saving || deleting;

  const deletePage = async () => {
    if (!deleteTarget || busy) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await axios.delete(`/api/pages/${deleteTarget.pageCode}`);
      setPages(current => current.filter(page => page.pageCode !== deleteTarget.pageCode));
      if (deleteTarget.pageCode === pageCode) {
        setPageCode('');
        setPageName('');
        setVersion(0);
        setSchema(initialSchema);
        setPlmConfig(emptyPlmConfig());
        setSavedSnapshot('');
        setIsPreview(false);
        setView('PAGES');
      }
      setDeleteTarget(null);
      setMessage(`已删除 ${deleteTarget.pageCode}，历史数据保留；不影响已导出或部署的页面`);
      try { await refreshPages(); } catch { setMessage('删除已完成，但列表刷新失败，请刷新列表'); }
    } catch (error: any) {
      setDeleteError(error.response?.status === 404 ? '页面不存在，请取消后刷新列表。' : '删除失败，请检查连接后重试；当前编辑内容仍保留。');
    } finally { setDeleting(false); }
  };

  const applyPage = (data: PageResponse) => {
    const config = normalizePlmConfig(data.plmConfig);
    setPageCode(data.pageCode);
    setSchema(data.schema);
    setPageName(data.pageName);
    setVersion(data.currentVersion);
    setPlmConfig(config);
    setSavedSnapshot(snapshot(data.pageName, data.schema, config));
    setIsPreview(false);
    setView('DESIGNER');
    setMessage(`已加载 ${data.pageCode} · V${data.currentVersion}`);
  };

  const refreshPages = async () => {
    const {data} = await axios.get<PageSummary[]>('/api/pages');
    setPages(data);
    return data;
  };

  const openPage = async (code: string) => {
    if (code === pageCode) { setView('DESIGNER'); return; }
    if (dirty && !window.confirm('当前页面有未保存内容，切换将放弃这些修改。是否继续？')) return;
    setLoading(true);
    try {
      const {data} = await axios.get<PageResponse>(`/api/pages/${code}`);
      applyPage(data);
    } catch {
      setMessage('页面读取失败，当前内容未改变，请刷新列表后重试');
    } finally { setLoading(false); }
  };

  const beginCreate = (mode: 'NEW' | 'COPY') => {
    setNewCode('');
    setNewName(mode === 'COPY' ? `${pageName} 副本`.slice(0, 200) : '');
    setCreateError('');
    setCreateMode(mode);
  };

  const createPage = async (event: React.FormEvent) => {
    event.preventDefault();
    if (busy) return;
    const code = newCode.trim();
    const name = newName.trim();
    if (!/^[A-Z0-9_]{1,100}$/.test(code) || !name || name.length > 200) {
      setCreateError('请填写有效名称和编码；编码只允许大写字母、数字、下划线，最多100位。');
      return;
    }
    if (dirty && !window.confirm(createMode === 'COPY'
      ? '将复制当前未保存的内容到新页面，原页面保持上次保存状态。是否继续？'
      : '当前页面有未保存内容，新建后将放弃这些修改。是否继续？')) return;
    setSaving(true);
    setCreateError('');
    try {
      const {data} = await axios.post<PageResponse>(`/api/pages/${code}`, {
        pageName: name,
        schema: createMode === 'COPY' ? schema : {type: 'page', title: name, body: []},
        plmConfig: createMode === 'COPY' ? plmConfig : emptyPlmConfig()
      });
      applyPage(data);
      setCreateMode(null);
      setMessage(`新页面 ${code} 已创建并保存为 V${data.currentVersion}`);
      try { await refreshPages(); } catch { setMessage('页面已创建，但列表刷新失败，请点击刷新列表'); }
    } catch (error: any) {
      setCreateError(error.response?.status === 409 ? '页面编码已存在（含已删除页面），请更换编码，不会覆盖原页面。' : '创建失败，请检查服务连接后重试。');
    } finally { setSaving(false); }
  };

  useEffect(() => {
    const warn = (event: BeforeUnloadEvent) => { event.preventDefault(); event.returnValue = ''; };
    if (dirty) window.addEventListener('beforeunload', warn);
    return () => window.removeEventListener('beforeunload', warn);
  }, [dirty]);

  const amisEnv = useMemo(() => ({
    fetcher: ({url, method, data, headers}: any) => axios.request({
      url,
      method: method || 'get',
      data,
      headers
    }),
    notify: (type: string, msg: string) => setMessage(`${type}: ${msg}`),
    alert: (msg: string) => window.alert(msg),
    confirm: (msg: string) => Promise.resolve(window.confirm(msg))
  }), []);

  useEffect(() => {
    (async () => {
      try {
        const list = await refreshPages();
        const code = list.find(page => page.pageCode === DEFAULT_PAGE_CODE)?.pageCode || list[0]?.pageCode;
        if (code) {
          const {data} = await axios.get<PageResponse>(`/api/pages/${code}`);
          applyPage(data);
        } else {
          setView('PAGES');
          setMessage('暂无页面，请点击新建页面');
        }
      } catch {
        setView('PAGES');
        setMessage('页面读取失败，请确认后端服务已启动，然后刷新列表');
      } finally { setLoading(false); }
    })();
  }, []);

  const loadFields = useCallback(async () => {
    const {data} = await axios.get<PlmFieldDefinition[]>('/api/plm-fields');
    setFields(data);
  }, []);

  const loadActions = useCallback(async () => {
    const {data} = await axios.get<PlmActionDefinition[]>('/api/plm-actions');
    setActions(data);
  }, []);

  useEffect(() => {
    Promise.all([loadFields(), loadActions()]).catch(() => {
      setMessage('PLM配置库读取失败，请确认后端服务已启动');
    });
  }, [loadActions, loadFields]);

  const savePage = async () => {
    if (busy || !pageCode) return;
    setSaving(true);
    try {
      const {data} = await axios.put<PageResponse>(`/api/pages/${pageCode}`, {
        pageName,
        schema,
        plmConfig
      });
      setVersion(data.currentVersion);
      setSavedSnapshot(snapshot(data.pageName, data.schema, normalizePlmConfig(data.plmConfig)));
      setPageName(data.pageName);
      setMessage(`保存成功，当前版本 V${data.currentVersion}`);
      try { await refreshPages(); } catch { setMessage('页面已保存，但列表刷新失败，请点击刷新列表'); }
    } catch (error: any) {
      setMessage(error.response?.status === 410 ? '页面已被删除，无法保存，请返回页面管理。' : error.response?.data?.message || '保存失败');
    } finally {
      setSaving(false);
    }
  };

  const exportPackage = () => {
    const content = JSON.stringify({
      formatVersion: 1,
      pageCode,
      pageName,
      version,
      schema,
      plmConfig
    }, null, 2);
    const url = URL.createObjectURL(new Blob([content], {type: 'application/json'}));
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `${pageCode}-${dirty ? 'DRAFT' : `V${version}`}.json`;
    anchor.click();
    URL.revokeObjectURL(url);
    setMessage(dirty ? '已导出当前未保存草稿，数据库中的版本未改变' : '配置包已导出');
  };

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <h1>PLM低代码页面设计器</h1>
          <span>{pageCode || '尚未选择页面'} · V{version}{dirty ? ' · 未保存' : ''}</span>
        </div>
        <div className="actions">
          {view === 'DESIGNER' && <div className="mode-switch" aria-label="页面模式">
            <button
              type="button"
              className={!isPreview ? 'active' : ''}
              onClick={() => setIsPreview(false)}
            >
              设计
            </button>
            <button
              type="button"
              className={isPreview ? 'active' : ''}
              onClick={() => setIsPreview(true)}
            >
              预览
            </button>
          </div>}
          <input
            aria-label="页面名称"
            value={pageName}
            disabled={busy || !pageCode}
            maxLength={200}
            onChange={(event) => setPageName(event.target.value)}
          />
          <button type="button" onClick={savePage} disabled={busy || !pageCode || !pageName.trim()}>
            {saving ? '保存中...' : '保存页面'}
          </button>
        </div>
      </header>
      <nav className="workspace-nav">
        <button type="button" className={view === 'PAGES' ? 'active' : ''} onClick={() => setView('PAGES')}>页面管理</button>
        <button type="button" disabled={!pageCode} className={view === 'DESIGNER' ? 'active' : ''} onClick={() => setView('DESIGNER')}>页面设计</button>
        <button type="button" className={view === 'FIELDS' ? 'active' : ''} onClick={() => setView('FIELDS')}>PLM字段库</button>
        <button type="button" className={view === 'ACTIONS' ? 'active' : ''} onClick={() => setView('ACTIONS')}>JPO动作库</button>
        <button type="button" disabled={!pageCode} className={view === 'BINDINGS' ? 'active' : ''} onClick={() => setView('BINDINGS')}>页面PLM绑定</button>
      </nav>
      <div className="statusbar">{message}</div>
      {view === 'PAGES' && <section className="binding-page">
        <div className="binding-heading">
          <div><h2>页面管理</h2><p>每个页面拥有独立编码、布局、PLM绑定和保存版本。</p></div>
          <div className="form-actions">
            <button onClick={() => refreshPages().then(() => setMessage('页面列表已刷新')).catch(() => setMessage('列表读取失败，请重试'))}>刷新列表</button>
            <button disabled={!pageCode || busy} onClick={() => beginCreate('COPY')}>复制当前页面</button>
            <button className="primary" disabled={busy} onClick={() => beginCreate('NEW')}>新建页面</button>
          </div>
        </div>
        <p>当前页面：{pageCode ? `${pageName}（${pageCode}）${dirty ? '，有未保存修改' : ''}` : '未选择'}</p>
        <div className="page-list">
          {pages.map(page => <div key={page.pageCode} className="page-card"><button className={`catalog-item ${page.pageCode === pageCode ? 'selected' : ''}`}
            disabled={busy} onClick={() => openPage(page.pageCode)}>
            <strong>{page.pageName}</strong><span>{page.pageCode}</span>
            <small>V{page.currentVersion} · {page.updatedAt ? new Date(page.updatedAt).toLocaleString() : ''}</small>
            <small>{page.pageCode === pageCode ? '当前页面 · 点击返回设计' : '点击打开设计'}</small>
          </button><div className="form-actions"><button type="button" className="danger" aria-label={`删除页面 ${page.pageCode}`} disabled={busy}
            onClick={() => { setDeleteError(''); setDeleteTarget(page); }}>删除页面</button></div></div>)}
          {!pages.length && <p>暂无页面，请新建页面。</p>}
        </div>
      </section>}
      {view === 'DESIGNER' && <section className="editor-host">
        <Editor
          key={pageCode}
          value={schema}
          onChange={setSchema}
          amisEnv={amisEnv}
          theme="cxd"
          preview={isPreview}
          onPreview={setIsPreview}
        />
      </section>}
      {view === 'FIELDS' && <PlmFieldManager fields={fields} onReload={loadFields} notify={setMessage} />}
      {view === 'ACTIONS' && <PlmActionManager actions={actions} onReload={loadActions} notify={setMessage} />}
      {view === 'BINDINGS' && (
        <PagePlmBinding
          schema={schema}
          config={plmConfig}
          fields={fields}
          actions={actions}
          onChange={setPlmConfig}
          onSave={savePage}
          onExport={exportPackage}
        />
      )}
      {createMode && <div className="page-dialog-mask">
        <form className="page-dialog" role="dialog" aria-modal="true" aria-labelledby="create-page-heading" onSubmit={createPage}>
          <h2 id="create-page-heading">{createMode === 'COPY' ? '复制当前页面' : '新建页面'}</h2>
          <p>{createMode === 'COPY' ? `复制“${pageName}”当前布局和PLM绑定，创建独立的V1页面。` : '创建空白页面，并保存为独立的V1版本。'}</p>
          <div className="form-grid">
            <label className="span-2">页面编码<input autoFocus required maxLength={100} pattern="[A-Z0-9_]+" placeholder="例如 JF_PART_QUERY" value={newCode} onChange={event => setNewCode(event.target.value)} /></label>
            <label className="span-2">页面名称<input required maxLength={200} placeholder="例如 零件查询页面" value={newName} onChange={event => setNewName(event.target.value)} /></label>
          </div>
          <p>编码仅支持大写字母、数字、下划线，创建后不可修改。名称可修改。</p>
          {createError && <p role="alert" className="empty-tip">{createError}</p>}
          <div className="form-actions"><button type="submit" className="primary" disabled={busy}>创建并打开</button><button type="button" disabled={busy} onClick={() => setCreateMode(null)}>取消</button></div>
        </form>
      </div>}
      {deleteTarget && <div className="page-dialog-mask">
        <div className="page-dialog" role="dialog" aria-modal="true" aria-labelledby="delete-page-heading">
          <h2 id="delete-page-heading">确认删除页面</h2>
          <p>即将删除：<strong>{deleteTarget.pageName}</strong>（{deleteTarget.pageCode}）</p>
          <p>页面将从列表隐藏，已保存内容与历史版本仍保留。编码不可重复使用；本版暂无回收站，恢复需要管理员处理。不影响已导出的JSON和已部署到PLM的页面。</p>
          {deleteTarget.pageCode === pageCode && dirty && <p className="empty-tip">当前页面有未保存修改，确认删除后这些修改将丢失。</p>}
          {deleteError && <p role="alert" className="empty-tip">{deleteError}</p>}
          <div className="form-actions">
            <button type="button" autoFocus disabled={busy} onClick={() => setDeleteTarget(null)}>取消</button>
            <button type="button" className="danger" disabled={busy} onClick={deletePage}>确认删除</button>
          </div>
        </div>
      </div>}
      {busy && <div className="page-busy" role="status">{deleting ? '正在删除页面…' : loading ? '正在加载页面…' : '正在保存页面…'}</div>}
    </main>
  );
}
