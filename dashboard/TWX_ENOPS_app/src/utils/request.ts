import axios, { AxiosHeaders } from 'axios';

import { isDev, baseApi } from './env';
import { ref } from 'vue';
const prdUrl: any = ref(window.localStorage.getItem('spaceUrl'));
import { ElMessage } from 'element-plus';

const service = axios.create({
	baseURL: isDev ? baseApi : prdUrl.value ? prdUrl.value : window.location.href.split('/webapp')[0],
	timeout: 1000 * 60 * 10,
	headers: {
		'Content-Type': 'application/json;charset=utf-8'
	}
});
export function setBaseURL(newBaseURL: any) {
	service.defaults.baseURL = newBaseURL;
}
// 请求拦截器
service.interceptors.request.use(
	config => {
		// 在这里可以添加token等认证信息
		const token = localStorage.getItem('token');
		if (token && config.headers) {
			// 使用 AxiosHeaders 的实例方法
			(config.headers as AxiosHeaders).set('Authorization', `Bearer ${token}`);
		}
		return config;
	},
	error => {
		ElMessage({
			duration: 10000,
			message: error,
			type: 'error'
		});
		return Promise.reject(error);
	}
);

// 响应拦截器
service.interceptors.response.use(
	response => {
		const { data } = response;
		// 这里可以添加统一的错误处理
		if (data.code !== 200) {
			// 处理错误
			ElMessage({
				duration: 10000,
				message: data.msg,
				type: 'error'
			});
			return Promise.reject(new Error(data.msg || 'Error'));
		}
		return data;
	},
	error => {
		// 处理网络错误
		ElMessage({
			duration: 10000,
			message: error,
			type: 'error'
		});
		console.error('Request Error:', error);
		return Promise.reject(error);
	}
);

export default service;
