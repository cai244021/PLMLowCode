<!--
  零件详细信息抽屉组件
-->
<template>
	<el-drawer
		v-model="isVisible"
		title="零件详细信息"
		size="500px"
		:close-on-click-modal="false"
		:destroy-on-close="true"
		@close="handleClose">
		<div class="detail-container">
			<!-- 操作按钮 -->
			<div class="detail-header">
				<el-button
					v-if="!isEditing"
					type="primary"
					@click="handleEdit">
					<el-icon><Edit /></el-icon>
					编辑
				</el-button>
				<template v-else>
					<el-button
						type="primary"
						:loading="isSaving"
						@click="handleSave">
						<el-icon><Check /></el-icon>
						保存
					</el-button>
					<el-button
						@click="handleCancel">
						<el-icon><Close /></el-icon>
						取消
					</el-button>
				</template>
			</div>

			<!-- 加载状态 -->
			<div v-if="loading" class="loading-wrapper">
				<el-skeleton :rows="10" animated />
			</div>

			<!-- 详情表单 -->
			<el-form
				v-else
				ref="formRef"
				:model="formData"
				label-width="150px"
				label-position="right"
				class="detail-form">
				<template v-for="(field, key) in partInfoData" :key="key">
					<!-- 跳过 id 字段 -->
					<el-form-item
						v-if="key !== 'id'"
						:label="field.display"
						:prop="key">
						<!-- 文本框 -->
						<el-input
							v-if="getFieldType(field) === 'textbox'"
							v-model="formData[key]"
							:disabled="!isEditing || isFieldReadOnly(field)"
							clearable />
						<!-- 下拉框 -->
						<el-select
							v-else-if="getFieldType(field) === 'combobox'"
							v-model="formData[key]"
							:disabled="!isEditing || isFieldReadOnly(field)"
							style="width: 100%">
							<el-option
								v-for="option in getFieldOptions(field)"
								:key="option.value"
								:label="option.label"
								:value="option.value" />
						</el-select>
						<!-- 默认文本框 -->
						<el-input
							v-else
							v-model="formData[key]"
							:disabled="!isEditing || isFieldReadOnly(field)"
							clearable />
					</el-form-item>
				</template>
			</el-form>
		</div>
	</el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { Edit, Check, Close } from '@element-plus/icons-vue';
import { getPartInfo, savePartInfo, type PartInfoResponse, type PartInfoField, type SavePartInfoResult } from '@/api/productApi';

// 定义 props
const props = defineProps({
	visible: {
		type: Boolean,
		default: false
	},
	partId: {
		type: String,
		default: ''
	}
});

// 定义事件
const emit = defineEmits(['update:visible', 'save-success']);

// 抽屉显示状态
const isVisible = ref(props.visible);
// 零件ID
const currentPartId = ref(props.partId);
// 加载状态
const loading = ref(false);
// 是否编辑模式
const isEditing = ref(false);
// 是否保存中
const isSaving = ref(false);
// 原始数据
const partInfoData = ref<PartInfoResponse>({});
// 表单数据
const formData = reactive<Record<string, string>>({});

// 监听 props.visible 变化
watch(
	() => props.visible,
	(newVal) => {
		isVisible.value = newVal;
		if (newVal && props.partId) {
			currentPartId.value = props.partId;
			loadPartInfo();
		}
	}
);

// 监听 props.partId 变化
watch(
	() => props.partId,
	(newVal) => {
		if (newVal) {
			currentPartId.value = newVal;
		}
	}
);

/**
 * 加载零件详细信息
 */
const loadPartInfo = async () => {
	if (!currentPartId.value) return;

	loading.value = true;
	isEditing.value = false;

	try {
		const response = await getPartInfo(currentPartId.value);
		console.log('[PartDetailDrawer] 获取零件信息:', response);

		partInfoData.value = response;

		// 初始化表单数据
		Object.keys(response).forEach(key => {
			if (key !== 'id') {
				formData[key] = response[key].value || '';
			}
		});
	} catch (error) {
		console.error('[PartDetailDrawer] 获取零件信息失败:', error);
		ElMessage.error('获取零件信息失败: ' + (error as Error).message);
	} finally {
		loading.value = false;
	}
};

/**
 * 获取字段类型
 */
const getFieldType = (field: PartInfoField): string => {
	return field.type || 'textbox';
};

/**
 * 判断字段是否只读
 */
const isFieldReadOnly = (field: PartInfoField): boolean => {
	return field.access === 'r';
};

/**
 * 获取下拉框选项（这里可以根据实际需求扩展）
 */
const getFieldOptions = (field: PartInfoField): Array<{label: string; value: string}> => {
	// 这里可以根据字段类型返回不同的选项
	// 暂时返回空数组，实际使用时可以根据 field 配置动态加载
	return [];
};

/**
 * 关闭抽屉
 */
const handleClose = () => {
	isEditing.value = false;
	emit('update:visible', false);
};

/**
 * 进入编辑模式
 */
const handleEdit = () => {
	isEditing.value = true;
};

/**
 * 取消编辑
 */
const handleCancel = () => {
	isEditing.value = false;
	// 恢复原始数据
	Object.keys(partInfoData.value).forEach(key => {
		if (key !== 'id') {
			formData[key] = partInfoData.value[key].value || '';
		}
	});
};

/**
 * 保存数据
 */
const handleSave = async () => {
	if (!currentPartId.value) return;

	isSaving.value = true;

	try {
		// 构造保存数据
		const saveData: PartInfoResponse = {};
		Object.keys(partInfoData.value).forEach(key => {
			saveData[key] = {
				...partInfoData.value[key],
				value: formData[key] || ''
			};
		});

		const response: any = await savePartInfo(currentPartId.value, saveData);
		console.log('[PartDetailDrawer] 保存结果:', response);
		console.log('[PartDetailDrawer] status值:', response.status);
		console.log('[PartDetailDrawer] status类型:', typeof response.status);

		// 处理可能的包装结构
		const result: SavePartInfoResult = response.status ? response : response.result || response;
		console.log('[PartDetailDrawer] 处理后的result:', result);

		if (result.status === 'success') {
			ElMessage.success('保存成功');
			isEditing.value = false;

			// 更新原始数据
			Object.keys(saveData).forEach(key => {
				if (partInfoData.value[key]) {
					partInfoData.value[key].value = saveData[key].value;
				}
			});

			// 触发保存成功事件
			emit('save-success', currentPartId.value);
		} else {
			// 保存失败
			const errorMessage = result.message || '保存失败';
			console.error('[PartDetailDrawer] 保存失败:', errorMessage);
			ElMessage.error('保存失败: ' + errorMessage);
		}
	} catch (error) {
		console.error('[PartDetailDrawer] 保存失败:', error);
		ElMessage.error('保存失败: ' + (error as Error).message);
	} finally {
		isSaving.value = false;
	}
};
</script>

<style scoped lang="scss">
.detail-container {
	padding: 20px;
	height: 100%;
	overflow-y: auto;
}

.detail-header {
	display: flex;
	justify-content: flex-end;
	margin-bottom: 20px;
	padding-bottom: 15px;
	border-bottom: 1px solid #e4e7ed;
}

.loading-wrapper {
	padding: 20px;
}

.detail-form {
	.el-form-item {
		margin-bottom: 18px;
	}
}
</style>
