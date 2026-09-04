<!--
  创建零件/总成对话框
-->
<template>
	<div>
		<el-dialog
			v-model="isVisible"
			:title="pageTitle"
			width="500px"
			append-to-body
			destroy-on-close
			:close-on-click-modal="false"
			:before-close="handleClose">
			<el-form
				ref="formRef"
				:model="form"
				:rules="formRules"
				label-width="120px"
				label-position="right">
				<el-form-item
					label="标题"
					prop="title">
					<el-input
						v-model="form.title"
						placeholder="请输入标题"
						clearable />
				</el-form-item>

				<el-form-item
					label="类型"
					prop="partType">
					<el-select
						v-model="form.partType"
						placeholder="请选择类型"
						style="width: 100%">
						<el-option
							v-for="option in partTypeOptions"
							:key="option.value"
							:label="option.label"
							:value="option.value" />
					</el-select>
				</el-form-item>

				<el-form-item label="零件中文描述">
					<el-input
						v-model="form.chineseDesc"
						placeholder="请输入零件中文描述"
						type="textarea"
						:rows="2"
						clearable />
				</el-form-item>

				<el-form-item label="零件英文描述">
					<el-input
						v-model="form.englishDesc"
						placeholder="请输入零件英文描述"
						type="textarea"
						:rows="2"
						clearable />
				</el-form-item>

				<el-form-item label="说明">
					<el-input
						v-model="form.remark"
						placeholder="请输入说明"
						type="textarea"
						:rows="3"
						clearable />
				</el-form-item>
			</el-form>

			<template #footer>
				<div class="dialog-footer">
					<el-button @click="handleCancel">取消</el-button>
					<el-button
						type="primary"
						:loading="isSubmitting"
						@click="handleSubmit(false)">
						创建
					</el-button>
					<el-button
						type="success"
						:loading="isSubmitting"
						@click="handleSubmit(true)">
						应用
					</el-button>
				</div>
			</template>
		</el-dialog>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, reactive, computed } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { createVPMReferenceV5ByRest, type CreateVPMReferenceParams, type CreateVPMReferencePartInfo } from '@/api/productApi';

// 定义 props
const props = defineProps({
	dialogVisible: {
		type: Boolean,
		default: false
	},
	// 创建类型：assembly-总成, component-零件
	createType: {
		type: String as () => 'assembly' | 'component',
		default: 'assembly'
	},
	// 父节点ID数组
	parentIds: {
		type: Array as () => string[],
		default: () => []
	}
});

// 定义事件
const emit = defineEmits(['update:dialogVisible', 'create-success']);

// 对话框显示状态
const isVisible = ref(props.dialogVisible);

// 监听 props.dialogVisible 变化
watch(
	() => props.dialogVisible,
	newVal => {
		isVisible.value = newVal;
		if (newVal) {
			// 打开对话框时重置表单
			resetForm();
		}
	}
);

// 页面标题
const pageTitle = computed(() => {
	return props.createType === 'assembly' ? '创建总成' : '创建零件';
});

// 表单引用
const formRef = ref<FormInstance>();

// 表单数据
const form = reactive({
	title: '',
	partType: '',
	chineseDesc: '',
	englishDesc: '',
	remark: ''
});

// 是否正在提交
const isSubmitting = ref(false);

// 零件类型选项
const partTypeOptions = [
	{ label: '请选择', value: '' },
	{ label: '整椅', value: 'C' },
	{ label: '金属件', value: 'M' },
	{ label: '电器件', value: 'E' },
	{ label: '发泡', value: 'U' },
	{ label: '其他', value: 'O' }
];

// 表单验证规则
const formRules = reactive<FormRules>({
	title: [
		{ required: true, message: '请输入标题', trigger: 'blur' },
		{ min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
	],
	partType: [{ required: true, message: '请选择类型', trigger: 'change' }]
});

/**
 * 重置表单
 */
const resetForm = () => {
	// 手动清空表单数据
	form.title = '';
	form.partType = '';
	form.chineseDesc = '';
	form.englishDesc = '';
	form.remark = '';
	// 清除表单验证状态
	if (formRef.value) {
		formRef.value.clearValidate();
	}
};

/**
 * 关闭对话框
 */
const handleClose = () => {
	emit('update:dialogVisible', false);
};

/**
 * 取消
 */
const handleCancel = () => {
	resetForm();
	handleClose();
};

/**
 * 提交表单
 * @param isContinue 是否继续创建（应用按钮）
 */
const handleSubmit = async (isContinue: boolean = false): Promise<void> => {
	if (!formRef.value) return;

	// 表单验证
	const valid = await formRef.value.validate().catch(() => false);
	if (!valid) return;

	isSubmitting.value = true;

	try {
		const partInfo: CreateVPMReferencePartInfo = {
			type: props.createType,
			title: form.title,
			partType: form.partType,
			chineseDesc: form.chineseDesc,
			englishDesc: form.englishDesc,
			remark: form.remark
		};

		const params: CreateVPMReferenceParams = {
			partInfo,
			parentId: props.parentIds.length > 0 ? props.parentIds : undefined
		};

		const response = await createVPMReferenceV5ByRest(params);
		console.log('[CreatePartDialog] 创建成功:', response);

		if (response.status === 'success' && response.result && response.result.length > 0) {
			// 接口返回 physicalid，需要作为 objectId 传递
			const objectId = response.result[0].physicalid;
			ElMessage.success(props.createType === 'assembly' ? '总成创建成功' : '零件创建成功');

			// 触发创建成功事件，传递 objectId
			emit('create-success', objectId);

			if (!isContinue) {
				// 创建：关闭对话框
				handleClose();
			}
			// 应用：保留表单内容，继续创建
		} else {
			throw new Error('创建失败：接口返回异常');
		}
	} catch (error) {
		console.error('[CreatePartDialog] 创建失败:', error);
		ElMessage.error('创建失败: ' + (error as Error).message);
	} finally {
		isSubmitting.value = false;
	}
};
</script>

<style scoped lang="scss">
.dialog-footer {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
}
</style>
