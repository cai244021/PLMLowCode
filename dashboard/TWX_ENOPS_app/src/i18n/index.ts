import { createI18n } from 'vue-i18n';
import zh from './lang/zh-CN';
import en from './lang/en-US';

const i18n = createI18n({
	legacy: false, // 使用 Composition API 模式
	locale: localStorage.getItem('language') || 'zh',
	fallbackLocale: 'zh-CN',
	messages: {
		zh: zh,
		en: en
	}
});

export default i18n;
