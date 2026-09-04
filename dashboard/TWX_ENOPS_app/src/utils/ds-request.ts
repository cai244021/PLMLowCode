import { requirejsPromise } from '@widget-lab/3ddashboard-utils';
const spaceUrl = window.localStorage.getItem('spaceUrl');
import { isDev, baseApi } from './env';
let baseURL = isDev ? baseApi : spaceUrl ? spaceUrl : window.location.href.split('/webapp')[0];
import { ElMessage } from 'element-plus';
export function setBaseURL(newBaseURL: any) {
	baseURL = newBaseURL;
}

const request = async (method: string, url: string, data: any | undefined) => {
	const WAFData = await requirejsPromise('DS/WAFData/WAFData');
	return new Promise((resolve, reject) => {
		console.log('baseURL00000', baseURL);
		WAFData.authenticatedRequest(`${baseURL}${url}`, {
			type: 'json',
			method: method,
			headers: {
				'Content-Type': 'application/json',
				'X-Requested-With': 'XMLHttpRequest',
				'X-3DSLogin-ticket': isDev ? 'NkVCOEEzOUMwMUM0NEMwNzkxMTM4M0M1NzNBOTI4OTV8YWRtaW5fcGxhdGZvcm18fHx8MHw=' : '' // 这里可以替换为实际的登录票据
			},
			data: method == 'POST' || method == 'PUT' ? data : undefined,
			onComplete: res => {
				resolve(res);
			},
			onFailure(error: any) {
				ElMessage({
					duration: 10000,
					message: error,
					type: 'error'
				});
				reject(error);
				console.log('error while fetching the securtiy context ', error);
			}
		});
	});
};
const getParams = (obj: any): string => {
	let result = '';
	let item;
	for (item in obj.params) {
		if (obj[item] && String(obj[item])) {
			result += `&${item}=${obj[item]}`;
		}
	}
	if (result) {
		result = '?' + result.slice(1);
	}
	return result;
};
const get = async (url: string, params?: Record<string, unknown>) => {
	return request('GET', url + getParams(params), undefined);
};
const post = async (url: string, data?: Record<string, unknown>) => {
	return request('POST', url, JSON.stringify(data));
};
const put = async (url: string, data?: Record<string, unknown>) => {
	return request('PUT', url, JSON.stringify(data));
};
const del = async (url: string, params?: Record<string, unknown>) => {
	return request('DELETE', url + getParams(params), undefined);
};
export const http = { get, post, put, delete: del };
export default http;
