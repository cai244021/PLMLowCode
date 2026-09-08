import type {PagePackage, PlmContext, SearchTarget} from './types';
import {executeAction, openSearch} from './platform';

function loadStyle(id: string, url: string): Promise<void> {
	if (document.getElementById(id)) return Promise.resolve();
	return new Promise((resolve, reject) => {
		const link = document.createElement('link');
		link.id = id;
		link.rel = 'stylesheet';
		link.href = url;
		link.onload = () => resolve();
		link.onerror = () => reject(new Error(`样式加载失败：${url}`));
		document.head.appendChild(link);
	});
}

function loadScript(id: string, url: string): Promise<void> {
	const existing = document.getElementById(id) as HTMLScriptElement | null;
	if (existing?.dataset.loaded === 'true') return Promise.resolve();
	return new Promise((resolve, reject) => {
		const script = existing || document.createElement('script');
		if (!existing) {
			script.id = id;
			script.src = url;
			document.head.appendChild(script);
		}
		script.addEventListener('load', () => {
			script.dataset.loaded = 'true';
			resolve();
		}, {once: true});
		script.addEventListener('error', () => reject(new Error(`脚本加载失败：${url}`)), {once: true});
	});
}

export async function loadRuntime(spaceUrl: string): Promise<void> {
	const base = `${spaceUrl}/common/JFLowCode`;
	await Promise.all([
		loadStyle('jf-amis-sdk-css', `${base}/amis/sdk.css`),
		loadStyle('jf-amis-helper-css', `${base}/amis/helper.css`),
		loadStyle('jf-amis-iconfont-css', `${base}/amis/iconfont.css`),
		loadStyle('jf-lowcode-runtime-css', `${base}/runtime.css?v=20260908-2`),
		loadScript('jf-amis-sdk-js', `${base}/amis/sdk.js`)
	]);
	await loadScript('jf-lowcode-runtime-js', `${base}/runtime.js?v=20260908-2`);
	if (!window.JFLowCodeRuntime) throw new Error('PLM低代码解析引擎加载失败');
}

export async function renderPage(
	spaceUrl: string,
	pageCode: string,
	pagePackage: PagePackage,
	context: PlmContext
): Promise<void> {
	if (!pagePackage.schema || !pagePackage.plmConfig) {
		throw new Error(`Page ${pageCode} 的配置包格式不正确`);
	}
	await window.JFLowCodeRuntime!.embed({
		container: '#jf-lowcode-root',
		pagePackage,
		adapter: {
			context,
			executeAction: (actionCode: string, data: Record<string, unknown>) =>
				executeAction(spaceUrl, pageCode, actionCode, data, context),
			openSearch: (target: SearchTarget) => openSearch(spaceUrl, target),
			notifyError: (error: unknown) => window.alert(error instanceof Error ? error.message : String(error)),
			close: () => window.history.back(),
			refresh: () => window.dispatchEvent(new CustomEvent('jf-lowcode-refresh')),
			openDetail: (objectId: string) => {
				if (!objectId) throw new Error('操作结果缺少objectId');
				window.open(`${spaceUrl}/common/emxTree.jsp?objectId=${encodeURIComponent(objectId)}`, '_blank');
			},
			navigate: (target: string) => {
				if (!target.startsWith('/') && !/^https?:\/\//i.test(target)) {
					throw new Error('不允许跳转到未登记地址');
				}
				const url = new URL(target, spaceUrl);
				if (url.origin !== new URL(spaceUrl).origin) {
					throw new Error('不允许跳转到3DSpace以外的地址');
				}
				window.open(url.toString(), '_blank');
			}
		}
	});
}
