<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref} from 'vue';
import {getPageCode, getPlmContext, loadPagePackage, resolveSpaceUrl} from './platform';
import {loadRuntime, renderPage} from './runtime';

const loading = ref(true);
const errorMessage = ref('');
let loadingTask: Promise<void> | null = null;

async function loadPage(): Promise<void> {
	if (loadingTask) return loadingTask;
	loadingTask = (async () => {
		loading.value = true;
		errorMessage.value = '';
		try {
			const pageCode = getPageCode();
			const spaceUrl = await resolveSpaceUrl();
			await loadRuntime(spaceUrl);
			const pagePackage = await loadPagePackage(spaceUrl, pageCode);
			document.getElementById('jf-lowcode-root')!.innerHTML = '';
			await renderPage(spaceUrl, pageCode, pagePackage, getPlmContext());
		} catch (error) {
			errorMessage.value = error instanceof Error ? error.message : String(error);
		} finally {
			loading.value = false;
			loadingTask = null;
		}
	})();
	return loadingTask;
}

onMounted(() => {
	window.addEventListener('jf-lowcode-refresh', loadPage);
	void loadPage();
});

onBeforeUnmount(() => window.removeEventListener('jf-lowcode-refresh', loadPage));
</script>

<template>
	<main class="lowcode-shell">
		<div v-if="loading" class="state-message">页面加载中...</div>
		<div v-else-if="errorMessage" class="state-message error">
			<div>{{ errorMessage }}</div>
			<button type="button" @click="loadPage">重新加载</button>
		</div>
		<div id="jf-lowcode-root"></div>
	</main>
</template>
