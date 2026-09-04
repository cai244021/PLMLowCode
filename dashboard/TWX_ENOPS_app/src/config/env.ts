// 环境变量配置
export const config = {
	// 应用标题
	title: import.meta.env.VITE_APP_TITLE,
	// API基础URL
	apiBaseUrl: import.meta.env.VITE_APP_API_BASE_URL,
	// 是否开启调试模式
	debug: import.meta.env.VITE_APP_DEBUG,
	// 当前环境
	env: import.meta.env.NODE_ENV
};
