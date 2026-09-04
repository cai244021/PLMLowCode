import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
// https://vite.dev/config/
export default defineConfig({
	base: './',
	plugins: [vue()],
	resolve: {
		alias: {
			'@': path.resolve(__dirname, './src')
		}
	},
	server: {
		cors: true,
		open: true,
		port: 3000,
		proxy: {
			'/api': {
				target: 'https://3dspace.r2025.v6.com/3dspace', // 接口的服务器域名
				changeOrigin: true,
				secure: false,
				rewrite: path => path.replace(/^\/api/, '')
			}
		}
	},
	build: {
		rollupOptions: {
			output: {
				format: 'cjs', // 指定输出格式CommonJS
				entryFileNames: 'bundle.js', // 指定输出文件名
				chunkFileNames: '[name]-[hash].js',
				assetFileNames: (assetInfo: any) => {
					// 根据文件类型判断并返回固定名称的 CSS 文件
					if (assetInfo.name.endsWith('.css')) {
						return 'assets/styles/bundle.css'; // 这里指定固定的 CSS 文件名
					}
					// 对于其他类型的文件，可以使用默认命名规则
					return 'assets/[name].[hash][extname]';
				}
			}
		}
	}
});
