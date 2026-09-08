import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
	base: './',
	plugins: [
		vue(),
		{
			name: 'dashboard-versioned-entry',
			transformIndexHtml: {
				order: 'post',
				handler(html, context) {
					if (!context.bundle) return html;
					return html
						.replace(/\s*<script type="module"[^>]*src="\.\/bundle\.js"><\/script>/, '')
						.replace(/\s*<link rel="stylesheet"[^>]*href="\.\/assets\/styles\/bundle\.css">/, '');
				}
			}
		}
	],
	server: {
		cors: true,
		open: '/?pageCode=JF_DA_LIST_DEMO',
		port: 3001
	},
	build: {
		rollupOptions: {
			output: {
				format: 'cjs',
				entryFileNames: 'bundle.js',
				chunkFileNames: '[name]-[hash].js',
				assetFileNames: assetInfo => assetInfo.name?.endsWith('.css')
					? 'assets/styles/bundle.css'
					: 'assets/[name].[hash][extname]'
			}
		}
	}
});
