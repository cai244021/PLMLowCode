<template>
	<el-dialog
		v-model="dialogVisible"
		title="AMIS低代码创建验证"
		width="760px"
		append-to-body
		:close-on-click-modal="false"
		@opened="renderAmis">
		<div
			v-loading="loading"
			class="amis-dialog-body">
			<div id="jf-amis-create-part"></div>
		</div>
	</el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { requirejsPromise } from '@widget-lab/3ddashboard-utils';
import { createVPMReferenceV5ByRest, executeLowCodeAction } from '@/api/productApi';
import { useBaseInfoStore } from '@/store';
import { baseApi, isDev } from '@/utils/env';

const AMIS_PAGE_CODE = 'JF_COMPETITIVE_BOM_CREATE';
const AMIS_CONTAINER_ID = 'jf-amis-create-part';

const props = defineProps({
	visible: {
		type: Boolean,
		default: false
	},
	parentIds: {
		type: Array as () => string[],
		default: () => []
	}
});

const emit = defineEmits(['update:visible', 'create-success', 'refresh']);
const baseInfoStore = useBaseInfoStore();
const loading = ref(false);
const rendered = ref(false);
const submitting = ref(false);

const dialogVisible = computed({
	get: () => props.visible,
	set: value => emit('update:visible', value)
});

const getSpaceBaseUrl = (): string => {
	const spaceUrl = isDev ? baseApi : baseInfoStore.spaceUrl || window.localStorage.getItem('spaceUrl');
	if (!spaceUrl) {
		throw new Error('尚未获取3DSpace服务地址');
	}
	return String(spaceUrl).replace(/\/$/, '');
};

const loadStyle = (id: string, url: string): Promise<void> => {
	const current = document.getElementById(id) as HTMLLinkElement | null;
	if (current) return Promise.resolve();
	return new Promise((resolve, reject) => {
		const link = document.createElement('link');
		link.id = id;
		link.rel = 'stylesheet';
		link.href = url;
		link.onload = () => resolve();
		link.onerror = () => reject(new Error(`AMIS样式加载失败: ${url}`));
		document.head.appendChild(link);
	});
};

const loadScript = (id: string, url: string): Promise<void> => {
	const current = document.getElementById(id) as HTMLScriptElement | null;
	if (current) {
		if (current.dataset.loaded === 'true') return Promise.resolve();
		return new Promise((resolve, reject) => {
			current.addEventListener('load', () => resolve(), { once: true });
			current.addEventListener('error', () => reject(new Error(`AMIS脚本加载失败: ${url}`)), { once: true });
		});
	}
	return new Promise((resolve, reject) => {
	const script = document.createElement('script');
		script.id = id;
		script.src = url;
		script.onload = () => {
			script.dataset.loaded = 'true';
			resolve();
		};
		script.onerror = () => reject(new Error(`AMIS脚本加载失败: ${url}`));
		document.head.appendChild(script);
	});
};

const loadSchema = async (url: string): Promise<any> => {
	const WAFData = await requirejsPromise('DS/WAFData/WAFData');
	return new Promise((resolve, reject) => {
		WAFData.authenticatedRequest(url, {
			type: 'json',
			method: 'GET',
			onComplete: schema => resolve(schema),
			onFailure: error => reject(new Error(`AMIS页面JSON加载失败: ${String(error)}`))
		});
	});
};

const executeAction = async (actionCode: string, data: Record<string, unknown>, context: Record<string, unknown>) => {
	if (submitting.value) {
		throw new Error('正在创建，请勿重复提交');
	}

	submitting.value = true;
	try {
		// Dashboard沿用当前已验证的零件创建REST，其余动作交给3DSpace白名单Action JSP。
		if (actionCode === 'CREATE_COMPETITIVE_BOM') {
			const response = await createVPMReferenceV5ByRest({
				partInfo: {
					type: 'component',
					title: String(data.title || '').trim(),
					partType: String(data.partType || ''),
					chineseDesc: '',
					englishDesc: '',
					remark: String(data.description || '')
				},
				parentId: props.parentIds.length > 0 ? props.parentIds : undefined
			});
			if (response.status !== 'success' || !response.result || response.result.length === 0) {
				throw new Error('创建失败：接口返回异常');
			}
			return { status: 0, msg: '创建成功', data: { objectId: response.result[0].physicalid } };
		}
		return await executeLowCodeAction(actionCode, { ...data, plmContext: context });
	} finally {
		submitting.value = false;
	}
};

const renderAmis = async (): Promise<void> => {
	if (rendered.value || loading.value) return;
	loading.value = true;
	try {
		const spaceBaseUrl = getSpaceBaseUrl();
		const amisBaseUrl = `${spaceBaseUrl}/common/JFLowCode/amis`;
		await Promise.all([
			loadStyle('jf-amis-sdk-css', `${amisBaseUrl}/sdk.css`),
			loadStyle('jf-amis-helper-css', `${amisBaseUrl}/helper.css`),
			loadStyle('jf-amis-iconfont-css', `${amisBaseUrl}/iconfont.css`),
			loadScript('jf-amis-sdk-js', `${amisBaseUrl}/sdk.js`)
		]);
		await loadScript('jf-lowcode-runtime-js', `${spaceBaseUrl}/common/JFLowCode/runtime.js?v=20260906-4`);

		let pagePackage;
		try {
			pagePackage = await loadSchema(
				`${spaceBaseUrl}/common/JF_LowCodePage.jsp?pageCode=${encodeURIComponent(AMIS_PAGE_CODE)}&_=${Date.now()}`
			);
		} catch {
			//尚未发布为Page时兼容原静态JSON部署方式。
			pagePackage = await loadSchema(
				`${spaceBaseUrl}/common/JFLowCode/pages/${AMIS_PAGE_CODE}.json?_=${Date.now()}`
			);
		}
		if (!pagePackage?.schema || !pagePackage?.plmConfig) {
			throw new Error('低代码页面配置包格式不正确');
		}

		await (window as any).JFLowCodeRuntime.embed({
			container: `#${AMIS_CONTAINER_ID}`,
			pagePackage,
			adapter: {
				context: {
					objectId: props.parentIds[0] || '',
					parentOID: props.parentIds[0] || '',
					relId: ''
				},
				executeAction,
				close: () => { dialogVisible.value = false; },
				refresh: () => emit('refresh'),
				openDetail: (objectId: string) => {
					if (!objectId) throw new Error('创建结果缺少objectId');
					emit('create-success', objectId);
					window.setTimeout(() => { dialogVisible.value = false; }, 300);
				},
				navigate: () => { throw new Error('Widget不支持未登记的页面跳转'); }
			}
		});
		rendered.value = true;
	} catch (error) {
		ElMessage.error((error as Error).message);
	} finally {
		loading.value = false;
	}
};
</script>

<style scoped lang="scss">
.amis-dialog-body {
	min-height: 360px;
}
</style>
