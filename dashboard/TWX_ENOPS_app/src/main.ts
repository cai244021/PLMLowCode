import { widget, disableDefaultCSS, requirejs, onVisibilityChange } from '@widget-lab/3ddashboard-utils';
import './assets/styles/main.scss';

import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import 'element-plus/dist/index.css';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import i18n from './i18n';

import App from './App.vue';
import router from './router';

const start = () => {
	disableDefaultCSS(true);
	widget.setTitle('');
	const app = createApp(App);
	for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
		app.component(key, component);
	}
	const pinia = createPinia();

	// 使用持久化插件
	pinia.use(piniaPluginPersistedstate);
	app.use(pinia);
	app.use(ElementPlus);
	app.use(router);
	app.use(i18n);
	app.mount('app');
	requirejs(['DS/PlatformAPI/PlatformAPI'], (/* PlatformAPI */) => {
		// use 3DDashboard APIs
	});

	onVisibilityChange((/* visibility */) => {
		// widget (or fullpage) visibility has changed
		// you can enable/disable periodic data refresh based on visibility
	});
};

/**
 * Entry point for both standalone & 3DDashboard modes
 */
widget.addEvent('onLoad', () => {
	start();
});
widget.addEvent('onRefresh', () => {
	// TODO an application data refresh
	// meaning only refresh dynamic content based on remote data, or after preference changed.
	// we could reload the frame [ window.location.reload() ], but this is not a good practice, since it reset preferences
});
