import {requirejsPromise, widget} from '@widget-lab/3ddashboard-utils';
import type {PagePackage, PlmContext, SearchResult, SearchTarget, SecurityContextInfo} from './types';

const PAGE_CODE_PATTERN = /^[A-Z0-9_]{1,100}$/;

function urlValue(rawUrl: string, name: string): string {
	try {
		const url = new URL(rawUrl, window.location.href);
		const queryValue = url.searchParams.get(name);
		if (queryValue) return queryValue.trim();
		const hashQuery = url.hash.includes('?') ? url.hash.substring(url.hash.indexOf('?') + 1) : '';
		return new URLSearchParams(hashQuery).get(name)?.trim() || '';
	} catch {
		return '';
	}
}

function configuredValue(name: string): string {
	const currentUrlValue = urlValue(window.location.href, name);
	if (currentUrlValue) return currentUrlValue;

	const widgetUrlValue = urlValue(widget.uwaUrl || '', name);
	if (widgetUrlValue) return widgetUrlValue;

	return widget.getValue(name)?.trim() || '';
}

export function getPageCode(): string {
	const pageCode = configuredValue('pageCode');
	if (!PAGE_CODE_PATTERN.test(pageCode)) {
		throw new Error('URL缺少合法的pageCode参数');
	}
	return pageCode;
}

export function getPlmContext(): PlmContext {
	return {
		objectId: configuredValue('objectId'),
		parentOID: configuredValue('parentOID'),
		relId: configuredValue('relId')
	};
}

export async function resolveSpaceUrl(): Promise<string> {
	const compass: any = await requirejsPromise('DS/i3DXCompassServices/i3DXCompassServices');
	return new Promise((resolve, reject) => {
		compass.getServiceUrl({
			serviceName: '3DSpace',
			platformId: widget.getValue('x3dPlatformId'),
			onComplete: (result: unknown) => {
				const value = Array.isArray(result) ? result[0]?.url : result;
				if (!value) {
					reject(new Error('3DSpace服务地址为空'));
					return;
				}
				resolve(String(value).replace(/\/$/, ''));
			},
			onFailure: (error: unknown) => reject(new Error(`无法获取3DSpace地址：${String(error)}`))
		});
	});
}

export async function authenticatedRequest(
	url: string,
	method: 'GET' | 'POST' | 'PUT' | 'DELETE',
	data?: unknown,
	type: 'text' | 'json' = 'text'
): Promise<unknown> {
	const wafData: any = await requirejsPromise('DS/WAFData/WAFData');
	return new Promise((resolve, reject) => {
		wafData.authenticatedRequest(url, {
			type,
			method,
			headers: {'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest'},
			data: data === undefined ? undefined : JSON.stringify(data),
			onComplete: resolve,
			onFailure: (error: unknown) => reject(new Error(`PLM请求失败：${String(error)}`))
		});
	});
}

export async function loadSecurityContext(spaceUrl: string): Promise<SecurityContextInfo> {
	return await authenticatedRequest(
		`${spaceUrl}/resources/pno/person/getsecuritycontext`,
		'GET',
		undefined,
		'json'
	) as SecurityContextInfo;
}

export async function loadPagePackage(spaceUrl: string, pageCode: string): Promise<PagePackage> {
	const result = await authenticatedRequest(
		`${spaceUrl}/common/JF_LowCodePage.jsp?pageCode=${encodeURIComponent(pageCode)}&_=${Date.now()}`,
		'GET'
	);
	return parseJson(result) as PagePackage;
}

export async function executeAction(
	spaceUrl: string,
	pageCode: string,
	actionCode: string,
	data: Record<string, unknown>,
	context: PlmContext
): Promise<unknown> {
	const result = await authenticatedRequest(
		`${spaceUrl}/common/JF_LowCodeAction.jsp?pageCode=${encodeURIComponent(pageCode)}&actionCode=${encodeURIComponent(actionCode)}`,
		'POST',
		{...data, plmContext: context}
	);
	return parseJson(result);
}

function prepareWidgetSearchParams(searchParams: string): string {
	const params = new URLSearchParams(searchParams.replace(/^\?/, ''));
	const wrapProgram = (parameterName: string, delegateParameterName: string, wrapperMethod: string): void => {
		const configuredProgram = params.get(parameterName);
		const wrapperProgram = `JF_LowCode:${wrapperMethod}`;
		if (!configuredProgram || configuredProgram === wrapperProgram) return;
		if (!/^[A-Za-z0-9_$.-]{1,150}:[A-Za-z0-9_$.-]{1,150}$/.test(configuredProgram)) {
			throw new Error(`${parameterName}格式不正确，应为JPO名:方法名`);
		}
		params.set(delegateParameterName, configuredProgram);
		params.set(parameterName, wrapperProgram);
	};
	wrapProgram('includeOIDprogram', 'lowCodeIncludeOIDprogram', 'filterIncludeSearchOIDsLowCode');
	wrapProgram('excludeOIDprogram', 'lowCodeExcludeOIDprogram', 'filterExcludeSearchOIDsLowCode');
	params.set('lowCodeSearchClient', 'widget');
	return params.toString();
}

export function openSearch(spaceUrl: string, target: SearchTarget): Promise<SearchResult> {
	if (!target.searchParams) {
		return Promise.reject(new Error('未配置emxFullSearch参数'));
	}
	const requestId = `jf-lowcode-${Date.now()}-${Math.random().toString(16).slice(2)}`;
	const launcherName = requestId.replace(/[^a-zA-Z0-9_-]/g, '');
	//20260909 update by caipan Widget独立准备搜索参数，不依赖Space Runtime的搜索适配器版本
	const preparedSearchParams = prepareWidgetSearchParams(target.searchParams);

	return new Promise((resolve, reject) => {
		const launcherWidth = 810;
		const launcherHeight = 590;
		const launcherLeft = Math.max(0, Math.round((window.screen.availWidth - launcherWidth) / 2));
		const launcherTop = Math.max(0, Math.round((window.screen.availHeight - launcherHeight) / 2));
		const launcher = window.open(
			'about:blank',
			launcherName,
			`popup=yes,width=${launcherWidth},height=${launcherHeight},left=${launcherLeft},top=${launcherTop}`
		);
		if (!launcher) {
			reject(new Error('搜索窗口被浏览器拦截，请允许弹出窗口'));
			return;
		}
		const launcherWindow = launcher;

		const form = document.createElement('form');
		form.method = 'POST';
		form.action = `${spaceUrl}/common/JF_LowCodeSearchLauncher.jsp`;
		form.target = launcherName;
		form.style.display = 'none';
		[
			{name: 'requestId', value: requestId},
			{name: 'searchParams', value: preparedSearchParams || ''}
		].forEach(item => {
			const input = document.createElement('input');
			input.type = 'hidden';
			input.name = item.name;
			input.value = item.value;
			form.appendChild(input);
		});
		document.body.appendChild(form);
		const messageTargets: Window[] = [window];
		try {
			if (window.top && window.top !== window) messageTargets.push(window.top);
		} catch {
			// Dashboard跨域时只能监听当前Widget窗口。
		}
		messageTargets.forEach(messageTarget => messageTarget.addEventListener('message', onMessage));
		const resultChannel = typeof BroadcastChannel === 'undefined'
			? null
			: new BroadcastChannel(`JF_LOWCODE_SEARCH_${requestId}`);
		if (resultChannel) resultChannel.addEventListener('message', onChannelMessage);

		function cleanup(): void {
			messageTargets.forEach(messageTarget => messageTarget.removeEventListener('message', onMessage));
			if (resultChannel) {
				resultChannel.removeEventListener('message', onChannelMessage);
				resultChannel.close();
			}
			if (!launcherWindow.closed) launcherWindow.close();
		}

		function finish(value: any): void {
			window.clearTimeout(timeout);
			cleanup();
			if (value.cancelled) {
				resolve({objectId: '', cancelled: true});
				return;
			}
			if (!value.objectId) {
				reject(new Error(value.message || '未选择对象'));
				return;
			}
			resolve(value as SearchResult);
		}

		const timeout = window.setTimeout(() => {
			//20260909 update by caipan 搜索窗口关闭消息丢失时按取消静默清理，避免延迟弹出超时提示
			finish({cancelled: true});
		}, 300000);
		function onMessage(event: MessageEvent): void {
			const value = event.data || {};
			if (event.origin !== new URL(spaceUrl).origin
				|| (value.type !== 'JF_LOWCODE_SEARCH_SELECTED' && value.type !== 'JF_LOWCODE_SEARCH_CLOSED')
				|| value.requestId !== requestId) return;
			finish(value);
		}
		function onChannelMessage(event: MessageEvent): void {
			const value = event.data || {};
			if ((value.type === 'JF_LOWCODE_SEARCH_SELECTED' || value.type === 'JF_LOWCODE_SEARCH_CLOSED')
				&& value.requestId === requestId) {
				finish(value);
			}
		}
		form.submit();
		form.remove();
	});
}

export function parseJson(value: unknown): unknown {
	if (typeof value !== 'string') return value;
	if (window.JFLowCodeRuntime?.parseJson) return window.JFLowCodeRuntime.parseJson(value);
	return JSON.parse(value.trim());
}

export async function publishDashboardEvent(topic: string, data?: unknown): Promise<void> {
	const platformApi: any = await requirejsPromise('DS/PlatformAPI/PlatformAPI');
	platformApi.publish(topic, data);
}
