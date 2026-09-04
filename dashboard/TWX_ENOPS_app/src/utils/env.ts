export const isDev = import.meta.env.VITE_APP_ENV === 'development';
export const isStaging = import.meta.env.VITE_APP_ENV === 'staging';
export const isProd = import.meta.env.VITE_APP_ENV === 'production';

export let baseApi = import.meta.env.VITE_APP_BASE_API;
export const baseUrl = import.meta.env.VITE_APP_BASE_URL;
export function setBaseApi(url: string) {
	baseApi = url;
}
export function getEnvConfig() {
	return {
		env: import.meta.env.VITE_APP_ENV,
		baseApi,
		baseUrl,
		isDev,
		isStaging,
		isProd
	};
}
