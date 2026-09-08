import {disableDefaultCSS, widget} from '@widget-lab/3ddashboard-utils';
import {createApp} from 'vue';
import App from './App.vue';
import './style.css';

let mounted = false;

function start(): void {
	if (mounted) return;
	mounted = true;
	disableDefaultCSS(true);
	widget.setTitle('');
	createApp(App).mount('app');
}

widget.addEvent('onLoad', start);
widget.addEvent('onRefresh', () => {
	window.dispatchEvent(new CustomEvent('jf-lowcode-refresh'));
});
