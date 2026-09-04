declare module '@/assets/images/*.png?url' {
	const src: string;
	export default src;
}

declare module '@/assets/images/*.png' {
	const src: string;
	export default src;
}

declare module '*.jpg' {
	const src: string;
	export default src;
}

declare module '*.jpeg' {
	const src: string;
	export default src;
}

declare module '*.png' {
	const src: string;
	export default src;
}

declare module '*.gif' {
	const src: string;
	export default src;
}

declare module '*.svg' {
	const src: string;
	export default src;
}
declare module 'element-plus/dist/locale/en.mjs' {
	// 这里您需要根据`en.mjs`的实际导出内容定义类型。
	// 假设`en.mjs`导出的是一个对象，您可以这样声明：
	const en: Record<string, any>;
	export default en;
}
declare module 'element-plus/dist/locale/zh-cn.mjs' {
	// 这里您需要根据`en.mjs`的实际导出内容定义类型。
	// 假设`en.mjs`导出的是一个对象，您可以这样声明：
	const zhCn: Record<string, any>;
	export default zhCn;
}
