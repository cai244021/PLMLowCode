import request from '@/utils/ds-request';
import type { ApiResponse } from './types';
export default class BaseAPI {
	protected baseURL: string;

	constructor(baseURL: string = '') {
		this.baseURL = baseURL;
	}

	protected async get<T = unknown>(url: string, params?: Record<string, unknown>): Promise<any> {
		const response = (await request.get(this.baseURL + url, {
			params
		})) as ApiResponse<T>;
		return response;
	}

	protected async post<T = unknown>(url: string, data?: Record<string, unknown>): Promise<any> {
		const response = (await request.post(this.baseURL + url, data)) as ApiResponse<T>;
		return response;
	}

	protected async put<T = unknown>(url: string, data?: Record<string, unknown>): Promise<any> {
		const response = (await request.put(this.baseURL + url, data)) as ApiResponse<T>;
		return response;
	}

	protected async deleteRequest<T = unknown>(url: string, params?: Record<string, unknown>): Promise<any> {
		const response = (await request.delete(this.baseURL + url, {
			params
		})) as ApiResponse<T>;
		return response;
	}
}
