<template>
	<div class="pse-container">
		<!-- 顶部工具栏 -->
		<div class="pse-toolbar">
			<el-button
				type="primary"
				:icon="Refresh"
				@click="refreshData">
				刷新
			</el-button>
			<el-button
				:icon="Expand"
				@click="expandAll">
				展开全部
			</el-button>
			<el-button
				:icon="Fold"
				@click="collapseAll">
				收起全部
			</el-button>
			<el-divider direction="vertical" />
			<el-button
				type="danger"
				:icon="Delete"
				@click="clearAll"
				:disabled="tableData.length === 0">
				清空
			</el-button>
			<el-button
				type="warning"
				:icon="DeleteFilled"
				@click="clearSelected"
				:disabled="selectedRows.length === 0">
				清除选中
			</el-button>
		</div>

		<!-- 主体内容区 - 3DE拖拽目标区域 -->
		<div
			id="contentDom"
			class="pse-content">
			<el-table
				v-if="tableData.length > 0"
				ref="tableRef"
				:data="tableData"
				style="width: 100%; height: 100%"
				row-key="id"
				border
				stripe
				highlight-current-row
				@selection-change="handleSelectionChange"
				@row-click="handleRowClick"
				v-loading="loading"
				class="custom-tree-table">
				<el-table-column
					type="selection"
					width="55"
					fixed
					reserve-selection />
				<el-table-column
					prop="attribute[PLMEntity.V_Name]"
					label="标题"
					min-width="200"
					show-overflow-tooltip
					fixed>
					<template #default="{ row }">
						<span
							class="title-cell"
							:style="{ paddingLeft: (row._treeLevel || 0) * 16 + 'px' }">
							<!-- 展开/收起图标 -->
							<span
								v-if="row.hasChildren"
								class="custom-tree-icon"
								@click.stop="toggleRowExpand(row)">
								<!-- 加载中显示旋转动画 -->
								<el-icon
									v-if="loadingRows.has(row.id)"
									class="is-loading">
									<Loading />
								</el-icon>
								<el-icon v-else-if="expandedKeys.includes(row.id)"><Minus /></el-icon>
								<el-icon v-else><Plus /></el-icon>
							</span>
							<span
								v-else
								class="tree-icon-placeholder"></span>
							<!-- 类型图标 -->
							<img
								v-if="row.type === 'VPMReference' && row['attribute[PLMEntity.V_usage]'] === '3DPart'"
								src="@/assets/images/3DPart.png"
								class="type-icon"
								alt="3DPart" />
							<img
								v-else-if="row.type === '3DShape' && row['attribute[PLMEntity.V_usage]'] === '3DPart'"
								src="@/assets/images/shape.png"
								class="type-icon"
								alt="Shape" />
							<img
								v-else
								src="@/assets/images/product.png"
								class="type-icon"
								alt="product" />
							{{ row['attribute[PLMEntity.V_Name]'] || '-' }}
						</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="type"
					label="类型"
					width="120" />
				<el-table-column
					prop="physicalid"
					label="ID"
					width="200"
					v-if="showIdColumn">
					<template #default="{ row }">
						<span>{{ row.physicalid || '-' }}</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="name"
					label="名称"
					min-width="180"
					show-overflow-tooltip />
				<el-table-column
					prop="attribute[PLMEntity.V_usage]"
					label="总成/零件"
					width="120">
					<template #default="{ row }">
						<span>{{ row['attribute[PLMEntity.V_usage]'] || '-' }}</span>
					</template>
				</el-table-column>
				<el-table-column
					prop="attribute[XCADExtension.V_CADOrigin]"
					label="V6/V5"
					width="100">
					<template #default="{ row }">
						<img
							v-if="row['attribute[XCADExtension.V_CADOrigin]'] === 'CATIAV5' || row['attribute[XCADRepExtension.V_CADFileOrigin]'] === 'CATIAV5'"
							src="@/assets/images/catiav5.png"
							class="cad-icon"
							alt="CATIAV5" />
						<img
							v-else
							src="@/assets/images/catiav6.png"
							class="cad-icon"
							alt="3DEXPERIENCE" />
					</template>
				</el-table-column>
			</el-table>

			<!-- 空状态提示 -->
			<div
				v-else
				class="empty-tip">
				<el-icon
					:size="60"
					color="#409eff">
					<Upload />
				</el-icon>
				<div class="hint-text">
					<p class="main-text">从 3DEXPERIENCE 拖拽对象到此处</p>
					<p class="sub-text">支持产品、零件、文档等类型</p>
				</div>
			</div>

			<!-- 创建零件/总成对话框 -->
		<CreatePartDialog
			v-model:dialogVisible="createDialogVisible"
			:createType="createDialogType"
			:parentIds="createDialogParentIds"
			@create-success="objectId => handleCreateSuccess(objectId, createDialogParentIds)" />

		<!-- AMIS低代码创建验证对话框 -->
		<AmisCreatePartDialog
			v-model:visible="amisCreateDialogVisible"
			:parentIds="createDialogParentIds"
			@refresh="refreshData"
			@create-success="objectId => handleCreateSuccess(objectId, createDialogParentIds)" />

		<!-- 零件详细信息抽屉 -->
		<PartDetailDrawer
			v-model:visible="detailDrawerVisible"
			:partId="selectedPartId" />
	</div>

		<!-- 底部工具栏 -->
		<div class="bottom-toolbar-container">
			<!-- 主工具栏（标签页样式） -->
			<div
				v-show="!isToolbarCollapsed"
				class="main-toolbar-tabs">
				<div
					:class="['tab-item', { active: activeToolbarTab === 'edit' }]"
					@click="handleTabClick('edit')">
					编辑
				</div>
				<div
					:class="['tab-item', { active: activeToolbarTab === 'view' }]"
					@click="handleTabClick('view')">
					视图
				</div>
				<div
					:class="['tab-item', { active: activeToolbarTab === 'tools' }]"
					@click="handleTabClick('tools')">
					工具
				</div>
				<div
					:class="['tab-item', { active: activeToolbarTab === 'lifecycle' }]"
					@click="handleTabClick('lifecycle')">
					生命周期
				</div>
				<div
					:class="['tab-item', { active: activeToolbarTab === 'collaborate' }]"
					@click="handleTabClick('collaborate')">
					协作
				</div>
				<div
					:class="['tab-item', { active: activeToolbarTab === 'config' }]"
					@click="handleTabClick('config')">
					配置
				</div>
			</div>

			<!-- 子工具栏（图标按钮） -->
			<div
				v-show="!isToolbarCollapsed && activeToolbarTab"
				class="sub-toolbar-icons">
				<!-- 编辑 -->
				<template v-if="activeToolbarTab === 'edit'">
					<el-tooltip
						content="创建总成"
						placement="top">
						<div
							class="icon-btn"
							@click="handleCreateAssembly">
							<el-icon :size="24"><FolderAdd /></el-icon>
						</div>
					</el-tooltip>
					<el-tooltip
						content="创建零件"
						placement="top">
						<div
							class="icon-btn"
							@click="handleCreatePart">
							<el-icon :size="24"><DocumentAdd /></el-icon>
						</div>
					</el-tooltip>
					<el-tooltip
						content="AMIS低代码创建"
						placement="top">
						<div
							class="icon-btn"
							@click="handleAmisCreatePart">
							<el-icon :size="24"><Grid /></el-icon>
						</div>
					</el-tooltip>
				</template>

				<!-- 视图 -->
				<template v-if="activeToolbarTab === 'view'">
					<el-tooltip
						content="查看详细信息"
						placement="top">
						<div
							class="icon-btn"
							@click="handleViewDetails">
							<el-icon :size="24"><View /></el-icon>
						</div>
					</el-tooltip>
				</template>

				<!-- 工具 -->
				<template v-if="activeToolbarTab === 'tools'">
					<el-tooltip
						content="导出报表"
						placement="top">
						<div
							class="icon-btn"
							@click="handleExportReport">
							<el-icon :size="24"><Download /></el-icon>
						</div>
					</el-tooltip>
				</template>

				<!-- 生命周期 -->
				<template v-if="activeToolbarTab === 'lifecycle'">
					<el-tooltip
						content="修订版"
						placement="top">
						<div
							class="icon-btn"
							@click="handleRevision">
							<el-icon :size="24"><RefreshRight /></el-icon>
						</div>
					</el-tooltip>
					<el-tooltip
						content="新修订版"
						placement="top">
						<div
							class="icon-btn"
							@click="handleNewRevision">
							<el-icon :size="24"><Refresh /></el-icon>
						</div>
					</el-tooltip>
					<el-tooltip
						content="状态"
						placement="top">
						<div
							class="icon-btn"
							@click="handleStatus">
							<el-icon :size="24"><InfoFilled /></el-icon>
						</div>
					</el-tooltip>
				</template>

				<!-- 协作 -->
				<template v-if="activeToolbarTab === 'collaborate'">
					<el-tooltip
						content="转移所有者"
						placement="top">
						<div
							class="icon-btn"
							@click="handleTransferOwner">
							<el-icon :size="24"><User /></el-icon>
						</div>
					</el-tooltip>
				</template>

				<!-- 配置 -->
				<template v-if="activeToolbarTab === 'config'">
					<el-tooltip
						content="关联模型"
						placement="top">
						<div
							class="icon-btn"
							@click="handleAssociateModel">
							<el-icon :size="24"><Link /></el-icon>
						</div>
					</el-tooltip>
					<el-tooltip
						content="编辑配置"
						placement="top">
						<div
							class="icon-btn"
							@click="handleEditConfig">
							<el-icon :size="24"><Setting /></el-icon>
						</div>
					</el-tooltip>
				</template>
			</div>

			<!-- 折叠/展开按钮（底部箭头） -->
			<div
				class="toolbar-toggle-bar"
				@click="toggleToolbar">
				<el-icon :class="{ 'is-collapsed': isToolbarCollapsed }">
					<ArrowUp />
				</el-icon>
			</div>
		</div>

		<!-- 底部状态栏 -->
		<div class="pse-footer">
			<div class="footer-left">
				<span>共 {{ tableData.length }} 条记录</span>
				<span
					v-if="selectedRows.length > 0"
					class="selected-count">
					已选中 {{ selectedRows.length }} 条
				</span>
			</div>
			<span
				v-if="lastDroppedId"
				class="last-dropped">
				最后加载: {{ lastDroppedId }}
			</span>
		</div>
	</div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick } from 'vue';
import {
	Refresh,
	Expand,
	Fold,
	Delete,
	DeleteFilled,
	Upload,
	Plus,
	Minus,
	Loading,
	ArrowUp,
	FolderAdd,
	DocumentAdd,
	Grid,
	View,
	Download,
	RefreshRight,
	InfoFilled,
	User,
	Link,
	Setting
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus';
import { getVPMReferenceInfo, expandVPMReferenceInfo, exportReport, getCADOriginsTypes, type ExportReportResult } from '@/api/productApi';
import CreatePartDialog from './DialogView/CreatePartDialog.vue';
import AmisCreatePartDialog from './DialogView/AmisCreatePartDialog.vue';
import PartDetailDrawer from './DialogView/PartDetailDrawer.vue';

// ==================== 3DE 拖拽集成 ====================

// 深拷贝函数 - 确保数据完全独立，避免引用共享问题
const deepClone = (obj: any): any => {
	if (obj === null || typeof obj !== 'object') return obj;
	if (obj instanceof Date) return new Date(obj.getTime());
	if (obj instanceof Array) return obj.map(item => deepClone(item));
	if (obj instanceof Object) {
		const cloned: any = {};
		for (const key in obj) {
			if (obj.hasOwnProperty(key)) {
				cloned[key] = deepClone(obj[key]);
			}
		}
		return cloned;
	}
	return obj;
};

const tableRef = ref<any>(null);
const tableData = ref<any[]>([]);
const selectedRows = ref<any[]>([]);
const lastDroppedId = ref('');
const loading = ref(false);
const showIdColumn = ref(false); // ID列默认隐藏
const expandedKeys = ref<string[]>([]); // 展开行的 key 列表
const loadingRows = ref<Set<string>>(new Set()); // 正在加载子节点的行ID集合

// Shift 连选状态
const lastSelectedIndex = ref<number>(-1);

// 底部工具栏状态
const isToolbarCollapsed = ref(true); // 工具栏是否折叠，默认折叠
const activeToolbarTab = ref<string>(''); // 当前激活的工具栏标签
const lastActiveToolbarTab = ref<string>('edit'); // 上次选中的工具栏标签，默认'编辑'

// 创建对话框状态
const createDialogVisible = ref(false); // 创建对话框是否显示
const amisCreateDialogVisible = ref(false); // AMIS低代码创建对话框是否显示
const createDialogType = ref<'assembly' | 'component'>('assembly'); // 创建类型
const createDialogParentIds = ref<string[]>([]); // 创建对话框的父节点ID数组

// 详情抽屉状态
const detailDrawerVisible = ref(false); // 详情抽屉是否显示
const selectedPartId = ref(''); // 选中的零件ID

// 切换工具栏折叠状态
const toggleToolbar = () => {
	if (isToolbarCollapsed.value) {
		// 展开工具栏：恢复上次选中的tab
		isToolbarCollapsed.value = false;
		activeToolbarTab.value = lastActiveToolbarTab.value;
	} else {
		// 收起工具栏：记住当前选中的tab，然后清空
		if (activeToolbarTab.value) {
			lastActiveToolbarTab.value = activeToolbarTab.value;
		}
		isToolbarCollapsed.value = true;
		activeToolbarTab.value = '';
	}
};

/**
 * 处理工具栏 tab 点击
 * @param tab tab 名称
 */
const handleTabClick = (tab: string): void => {
	activeToolbarTab.value = tab;
	lastActiveToolbarTab.value = tab;
};

// 编辑功能
/**
 * 处理创建按钮点击逻辑
 * @param type 创建类型：assembly-总成, component-零件
 */
const handleCreate = async (type: 'assembly' | 'component', dialogMode: 'native' | 'amis' = 'native') => {
	createDialogType.value = type;
	createDialogParentIds.value = []; // 重置父节点ID数组
	const openCreateDialog = () => {
		if (dialogMode === 'amis') {
			amisCreateDialogVisible.value = true;
		} else {
			createDialogVisible.value = true;
		}
	};

	const selectedCount = selectedRows.value.length;

	if (selectedCount === 0) {
		// 未选中数据，直接打开创建对话框
		openCreateDialog();
	} else {
		// 有选中数据，先调用接口判断是否可以创建
		try {
			const selectIds = selectedRows.value.map(row => row.id).filter(id => id);
			const checkResult = await getCADOriginsTypes(selectIds);

			if (!checkResult.result) {
				ElMessage.warning('当前选中的数据不支持创建,请选择总成进行创建!!!');
				return;
			}

			// 判断通过，继续处理
			if (selectedCount === 1) {
				// 选中1条数据，传递该数据的physicalid
				const physicalId = selectedRows.value[0]?.physicalid;
				if (physicalId) {
					createDialogParentIds.value = [physicalId];
				}
				openCreateDialog();
			} else {
				// 选中2条及以上数据，弹出确认框
				try {
					await ElMessageBox.confirm('是否确定要在多个父项下插入新产品?', '插入新产品', {
						confirmButtonText: '确定',
						cancelButtonText: '关闭',
						type: 'warning'
					});
					// 用户点击确定，收集所有选中数据的physicalid
					const parentIds = selectedRows.value.map(row => row.physicalid).filter(id => id);
					createDialogParentIds.value = parentIds;
					openCreateDialog();
				} catch {
					// 用户点击取消或关闭，不执行任何操作
				}
			}
		} catch (error) {
			console.error('[Create] 检查选中数据失败:', error);
			ElMessage.error('检查选中数据失败: ' + (error as Error).message);
		}
	}
};

/**
 * 打开创建总成对话框
 */
const handleCreateAssembly = () => {
	handleCreate('assembly');
};

/**
 * 打开创建零件对话框
 */
const handleCreatePart = () => {
	handleCreate('component');
};

/**
 * 使用AMIS低代码页面创建零件，父项校验和创建成功刷新逻辑与原创建零件保持一致
 */
const handleAmisCreatePart = () => {
	handleCreate('component', 'amis');
};

/**
 * 处理创建成功
 * @param objectId 创建的对象ID
 * @param parentIds 父节点ID数组
 */
const handleCreateSuccess = async (objectId: string, parentIds: string[]): Promise<void> => {
	console.log('[HomeView] handleCreateSuccess, objectId:', objectId, 'parentIds:', parentIds);

	// 如果有父节点，展开所有父节点并刷新子节点数据
	if (parentIds && parentIds.length > 0) {
		for (const parentId of parentIds) {
			await refreshParentChildren(parentId);
		}
		ElMessage.success('创建成功，已刷新父节点数据');
	} else {
		// 没有父节点，直接追加到表格最后
		await appendNewObjectToTable(objectId);
	}
};

/**
 * 刷新父节点的子节点数据
 * @param parentId 父节点ID
 */
const refreshParentChildren = async (parentId: string): Promise<void> => {
	try {
		// 在 tableData 中查找父节点
		const parentRow = tableData.value.find(item => item.id === parentId || item.physicalid === parentId);

		if (!parentRow) {
			console.warn('[Create] 未找到父节点:', parentId);
			return;
		}

		const rowKey = parentRow.id;

		// 如果父节点已展开，先收起再展开（强制刷新）
		if (expandedKeys.value.includes(rowKey)) {
			// 收起：从 expandedKeys 中移除
			expandedKeys.value = expandedKeys.value.filter(key => key !== rowKey);
			console.log('[Create] 收起父节点:', rowKey);

			// 删除所有子节点
			if (parentRow._children && parentRow._children.length > 0) {
				const allDescendantIds = getAllDescendantIds(parentRow);
				const idsToRemove = new Set(allDescendantIds);
				tableData.value = tableData.value.filter(item => !idsToRemove.has(item.id));
				expandedKeys.value = expandedKeys.value.filter(key => !idsToRemove.has(key));
				console.log('[Create] 已删除旧子节点，数量:', allDescendantIds.length);
			}
		}

		// 展开父节点并加载最新子节点数据
		expandedKeys.value.push(rowKey);
		loadingRows.value.add(rowKey);

		try {
			const params = { id: parentId };
			const response = await expandVPMReferenceInfo(params);
			console.log('[Create] 获取子节点数据:', response);

			if (response && Array.isArray(response)) {
				// 处理子节点数据
				parentRow._children = response.map((item: any) => ({
					...item,
					id: item.id || item.physicalid,
					hasChildren: true,
					_treeLevel: (parentRow._treeLevel || 0) + 1,
					_parentId: parentId
				}));

				// 将子节点插入到父节点后面
				const rowIndex = tableData.value.findIndex(item => item.id === rowKey);
				if (rowIndex !== -1) {
					tableData.value.splice(rowIndex + 1, 0, ...parentRow._children);
					console.log('[Create] 子节点已插入到索引:', rowIndex + 1, '数量:', parentRow._children.length);
				}
			}
		} catch (error) {
			console.error('[Create] 加载子节点失败:', error);
			ElMessage.error('加载子节点失败');
			expandedKeys.value = expandedKeys.value.filter(key => key !== rowKey);
		} finally {
			loadingRows.value.delete(rowKey);
		}
	} catch (error) {
		console.error('[Create] 刷新父节点失败:', error);
	}
};

/**
 * 将新对象追加到表格最后
 * @param objectId 对象ID
 */
const appendNewObjectToTable = async (objectId: string): Promise<void> => {
	try {
		// 构造类似拖拽的数据格式
		const dragData = {
			data: {
				items: [{ objectId: objectId }]
			}
		};
		console.log('[HomeView] dragData:', JSON.stringify(dragData));

		// 调用 getVPMReferenceInfo 获取完整数据
		const response = await getVPMReferenceInfo(dragData);
		console.log('[Create] 获取创建的数据:', response);

		if (response && Array.isArray(response)) {
			// 处理数据（和拖拽逻辑类似）
			const processedData = response.map((item: any) => {
				const clonedItem = JSON.parse(JSON.stringify(item));
				return {
					...clonedItem,
					id: clonedItem.id || clonedItem.physicalid,
					_treeLevel: 0,
					_parentId: undefined,
					hasChildren: true
				};
			});

			// 获取现有数据的ID集合，用于去重
			const existingIds = new Set(tableData.value.map(item => item.id));

			// 过滤掉已存在的数据
			const newData = processedData.filter((item: any) => !existingIds.has(item.id));

			if (newData.length > 0) {
				tableData.value = [...tableData.value, ...newData];
				ElMessage.success(`已添加 ${newData.length} 个对象到表格`);
			}
		}
	} catch (error) {
		console.error('[Create] 添加数据到表格失败:', error);
		ElMessage.error('添加数据到表格失败: ' + (error as Error).message);
	}
};

// 视图功能
/**
 * 查看详细信息
 */
const handleViewDetails = () => {
	const selectedCount = selectedRows.value.length;

	if (selectedCount === 0) {
		ElMessage.warning('请先选择一条数据');
		return;
	}

	if (selectedCount > 1) {
		ElMessage.warning('只支持单选查看信息');
		return;
	}

	// 单选，显示详情抽屉
	const selectedRow = selectedRows.value[0];
	const partId = selectedRow?.id || selectedRow?.physicalid;

	if (partId) {
		selectedPartId.value = partId;
		detailDrawerVisible.value = true;
	} else {
		ElMessage.warning('无法获取选中数据的ID');
	}
};

// 工具功能
/**
 * 检查节点是否有任何祖先节点在选中列表中（递归检查祖父、曾祖父等）
 * 使用完整的 tableData 来查找父节点关系
 * @param node 当前节点
 * @param selectedIds 选中节点的ID集合
 * @param fullNodeMap 完整节点ID到节点的映射（从tableData构建）
 * @returns 如果有祖先节点在选中列表中返回true
 */
const hasAncestorInSelected = (node: any, selectedIds: Set<string>, fullNodeMap: Map<string, any>, depth: number = 0): boolean => {
	const indent = '  '.repeat(depth);
	console.log(`${indent}[hasAncestorInSelected] 检查节点: ${node.id}, _parentId: ${node._parentId}`);

	// 如果没有父节点，说明是根节点
	if (!node._parentId) {
		console.log(`${indent}  -> 无父节点，返回 false`);
		return false;
	}

	// 如果直接父节点在选中列表中
	if (selectedIds.has(node._parentId)) {
		console.log(`${indent}  -> 父节点 ${node._parentId} 在选中列表中！返回 true`);
		return true;
	}

	// 递归检查祖父节点（从完整数据中查找）
	const parentNode = fullNodeMap.get(node._parentId);
	console.log(`${indent}  -> 父节点 ${node._parentId} 不在选中列表中，从 fullNodeMap 查找...`);
	if (parentNode) {
		console.log(`${indent}  -> 找到父节点 ${parentNode.id}，继续递归检查`);
		return hasAncestorInSelected(parentNode, selectedIds, fullNodeMap, depth + 1);
	}

	console.log(`${indent}  -> 在 fullNodeMap 中未找到父节点 ${node._parentId}`);
	return false;
};

/**
 * 过滤选中节点，只保留最顶层父节点（去除子节点、孙子节点等）
 * 如果选中了父节点和子节点（包括孙子节点），只保留最顶层的父节点
 * @param selectedNodes 选中的节点数组
 * @returns 过滤后的节点ID数组
 */
const filterTopLevelNodes = (selectedNodes: any[]): string[] => {
	if (!selectedNodes || selectedNodes.length === 0) {
		return [];
	}

	// 获取所有选中节点的ID集合，用于快速查找
	const selectedIds = new Set(selectedNodes.map(node => node.id));

	// 从完整的 tableData 构建节点ID到节点的映射，用于查找父节点关系
	const fullNodeMap = new Map<string, any>();
	tableData.value.forEach(node => {
		fullNodeMap.set(node.id, node);
	});

	console.log('[Export] tableData 总节点数:', tableData.value.length);
	console.log('[Export] fullNodeMap 大小:', fullNodeMap.size);
	console.log('[Export] 选中节点详情:');
	selectedNodes.forEach(node => {
		console.log(`  - ${node.id}: _parentId=${node._parentId}, _treeLevel=${node._treeLevel}`);
	});

	// 过滤出最顶层节点（其任何祖先节点都不在选中列表中）
	const topLevelNodes = selectedNodes.filter(node => {
		// 检查该节点是否有任何祖先节点在选中列表中
		console.log(`[Export] 开始检查节点: ${node.id}`);
		const hasAncestor = hasAncestorInSelected(node, selectedIds, fullNodeMap);
		console.log(`[Export] 节点 ${node.id} 有祖先在选中列表中: ${hasAncestor}`);
		return !hasAncestor;
	});

	const filteredIds = topLevelNodes.map(node => node.id);
	console.log('[Export] 原始选中节点:', selectedNodes.length, '个');
	console.log('[Export] 过滤后顶层节点:', filteredIds.length, '个');
	console.log('[Export] 被过滤的子节点:', selectedNodes.length - topLevelNodes.length, '个');

	return filteredIds;
};

// 工具功能
const handleExportReport = async (): Promise<void> => {
	let nodeIds: string[] = [];

	// 1. 检查是否有选中的节点
	if (selectedRows.value && selectedRows.value.length > 0) {
		// 过滤选中节点，只保留最顶层父节点
		nodeIds = filterTopLevelNodes(selectedRows.value);
		console.log('[Export] 使用选中的顶层节点:', nodeIds);
	} else {
		// 2. 没有选中节点，获取所有根节点ID
		// 根节点是 _treeLevel 为 0 的节点
		nodeIds = tableData.value.filter(row => row._treeLevel === 0).map(row => row.id);
		console.log('[Export] 使用所有根节点:', nodeIds);
	}

	if (nodeIds.length === 0) {
		ElMessage.warning('没有可导出的数据');
		return;
	}

	// 3. 显示加载窗口
	const loadingInstance = ElLoading.service({
		lock: true,
		text: '正在生成报表，请稍候...',
		background: 'rgba(0, 0, 0, 0.7)'
	});

	try {
		// 4. 调用导出接口，获取 Base64 编码的文件
		const response = await exportReport({ nodeIds });
		console.log('[Export] 接口返回:', response);

		// 5. 解析响应
		let result: ExportReportResult;
		if (typeof response === 'string') {
			result = JSON.parse(response) as ExportReportResult;
		} else if (response.data) {
			result = typeof response.data === 'string' ? (JSON.parse(response.data) as ExportReportResult) : response.data;
		} else {
			result = response;
		}

		// 6. 检查返回结果
		if (result.code && result.code !== '200') {
			throw new Error(result.msg || '导出失败');
		}

		const fileName = result.fileName || `report_${new Date().getTime()}.xlsx`;
		const base64Content = result.content;

		if (!base64Content) {
			throw new Error('未获取到文件内容');
		}

		console.log('[Export] 文件名:', fileName, 'Base64长度:', base64Content.length);

		// 7. Base64 解码并下载
		downloadBase64File(base64Content, fileName);

		ElMessage.success('报表导出成功');
	} catch (error) {
		console.error('[Export] 导出失败:', error);
		ElMessage.error('报表导出失败: ' + (error as Error).message);
	} finally {
		// 8. 关闭加载窗口
		loadingInstance.close();
	}
};

/**
 * 下载 Base64 编码的文件
 * @param base64Content Base64 编码的文件内容
 * @param fileName 下载后的文件名
 */
const downloadBase64File = (base64Content: string, fileName: string): void => {
	// 1. Base64 解码
	const byteCharacters = atob(base64Content);
	const byteNumbers: number[] = [];
	for (let i = 0; i < byteCharacters.length; i++) {
		byteNumbers.push(byteCharacters.charCodeAt(i));
	}
	const byteArray = new Uint8Array(byteNumbers);

	// 2. 创建 Blob
	const blob = new Blob([byteArray], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

	// 3. 触发下载
	const url = window.URL.createObjectURL(blob);
	const link = document.createElement('a');
	link.href = url;
	link.download = fileName;
	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);
	window.URL.revokeObjectURL(url);

	console.log('[Download] 文件下载完成:', fileName);
};

// 生命周期功能
const handleRevision = () => {
	ElMessage.info('修订版');
};

const handleNewRevision = () => {
	ElMessage.info('新修订版');
};

const handleStatus = () => {
	ElMessage.info('状态');
};

// 协作功能
const handleTransferOwner = () => {
	ElMessage.info('转移所有者');
};

// 配置功能
const handleAssociateModel = () => {
	ElMessage.info('关联模型');
};

const handleEditConfig = () => {
	ElMessage.info('编辑配置');
};

// 切换行展开/收起 - 使用 el-table 树形数据标准写法
const toggleRowExpand = async (row: any) => {
	console.log('[Toggle] 点击展开/收起:', { id: row.id, name: row.name });

	const rowKey = row.id;
	const isExpanded = expandedKeys.value.includes(rowKey);

	if (isExpanded) {
		// 收起：从 expandedKeys 中移除
		expandedKeys.value = expandedKeys.value.filter(key => key !== rowKey);
		console.log('[Toggle] 收起行:', rowKey);

		// 递归删除所有后代节点（包括孙子级）
		if (row._children && row._children.length > 0) {
			// 收集所有后代节点ID
			const allDescendantIds = getAllDescendantIds(row);
			const idsToRemove = new Set(allDescendantIds);

			// 从 tableData 中移除所有后代节点
			tableData.value = tableData.value.filter(item => !idsToRemove.has(item.id));

			// 从 expandedKeys 中移除所有后代节点的展开状态
			expandedKeys.value = expandedKeys.value.filter(key => !idsToRemove.has(key));

			console.log('[Toggle] 所有后代节点已删除，数量:', allDescendantIds.length);
		}
	} else {
		// 展开：添加到 expandedKeys
		expandedKeys.value.push(rowKey);
		console.log('[Toggle] 展开行:', rowKey);

		// 如果还没有加载子节点，先加载
		if (!row._children || row._children.length === 0) {
			// 标记当前行正在加载
			loadingRows.value.add(rowKey);
			try {
				const params = { id: row.id };
				const response = await expandVPMReferenceInfo(params);
				console.log('[Toggle] API返回数据:', response);

				if (response && Array.isArray(response)) {
					// 直接赋值给 row._children，使用自定义属性名避免 el-table 自动树形渲染
					row._children = response.map((item: any) => ({
						...item,
						id: item.id || item.physicalid,
						hasChildren: true, // 标记有子节点，显示展开图标
						_treeLevel: (row._treeLevel || 0) + 1, // 子节点层级 +1
						_parentId: row.id // 设置父节点ID
					}));
					console.log('[Toggle] 子节点已加载:', row._children.length, '个');

					// 将子节点插入到当前行后面
					const rowIndex = tableData.value.findIndex(item => item.id === row.id);
					if (rowIndex !== -1) {
						tableData.value.splice(rowIndex + 1, 0, ...row._children);
						console.log('[Toggle] 子节点已插入到索引:', rowIndex + 1);
					}
				}
			} catch (error) {
				console.error('[Toggle] 加载子节点失败:', error);
				ElMessage.error('加载子节点失败');
				// 加载失败，从展开Keys中移除
				expandedKeys.value = expandedKeys.value.filter(key => key !== rowKey);
			} finally {
				// 移除加载状态
				loadingRows.value.delete(rowKey);
			}
		} else {
			// 已经加载过子节点，直接插入
			const rowIndex = tableData.value.findIndex(item => item.id === row.id);
			if (rowIndex !== -1) {
				tableData.value.splice(rowIndex + 1, 0, ...row._children);
				console.log('[Toggle] 子节点已插入到索引:', rowIndex + 1);
			}
		}
	}
};

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
	selectedRows.value = selection;
};

/**
 * 处理行点击事件
 * 点击行时：高亮当前行，选中当前行，取消其他所有选中
 * @param row 点击的行数据
 */
const handleRowClick = (row: any): void => {
	if (!tableRef.value) return;

	// 1. 清除所有选中
	tableRef.value.clearSelection();

	// 2. 选中当前行
	tableRef.value.toggleRowSelection(row, true);

	// 3. 设置当前高亮行（el-table 的 highlight-current-row 会自动处理）
	tableRef.value.setCurrentRow(row);
};

// 初始化 3DE 拖拽
onMounted(() => {
	const ctdom = document.getElementById('contentDom') as HTMLElement;

	// @ts-ignore
	parent.require(['DS/DataDragAndDrop/DataDragAndDrop'], function (DataDragAndDrop: any) {
		'use strict';

		DataDragAndDrop.droppable(ctdom, {
			drop: async function (data: string) {
				console.log('[3DE Drop] 拖拽的数据信息：', data);

				try {
					const jsonData = JSON.parse(data);
					const items = jsonData.data?.items || [];

					if (items.length > 0) {
						// 提取第一个对象的ID
						const rootId = items[0]?.objectId;
						lastDroppedId.value = rootId;

						loading.value = true;
						try {
							// 调用后端接口获取完整数据
							const response = await getVPMReferenceInfo(jsonData);
							console.log('[API] 获取到的数据:', response);

							if (response && Array.isArray(response)) {
								// 处理数据，添加树形结构标记和层级
								// 所有拖拽的对象都作为独立的根节点处理
								// 所有节点都当成非叶子节点处理（都有展开按钮）
								const processedData = response.map((item: any) => {
									// 使用深拷贝确保数据完全独立，避免引用共享
									const clonedItem = deepClone(item);
									return {
										...clonedItem,
										id: clonedItem.id || clonedItem.physicalid,
										_treeLevel: 0, // 所有拖拽的对象都是根节点
										_parentId: undefined, // 根节点没有父节点
										hasChildren: true // 所有节点都当成非叶子节点
									};
								});

								// 获取现有数据的ID集合，用于去重判断
								const existingIds = new Set(tableData.value.map(item => item.id));

								// 过滤掉已存在的数据，只添加新数据
								const newData = processedData.filter((item: any) => !existingIds.has(item.id));

								if (newData.length > 0) {
									// 追加新数据到现有数据
									tableData.value = [...tableData.value, ...newData];
									// 将新添加的行从展开状态中移除（显示为未展开）
									const newIds = new Set(newData.map(item => item.id));
									expandedKeys.value = expandedKeys.value.filter(key => !newIds.has(key));
									ElMessage.success(`成功追加 ${newData.length} 个新对象，共 ${tableData.value.length} 个对象`);
								} else {
									ElMessage.info('所有对象已存在，未添加重复数据');
								}
							} else {
								ElMessage.warning('返回数据格式不正确');
							}
						} catch (apiError) {
							console.error('[API] 调用接口失败:', apiError);
							ElMessage.error('调用后端接口失败');
						} finally {
							loading.value = false;
						}
					}
				} catch (error) {
					console.error('[3DE Drop] 解析数据失败:', error);
					ElMessage.error('解析拖拽数据失败');
				}
			},
			enter: function () {
				console.log('[3DE Drop] 拖拽进入');
			},
			over: function () {},
			leave: function () {
				console.log('[3DE Drop] 拖拽离开');
			}
		});
	});
});

// ==================== 工具栏功能 ====================

// 递归清空节点的所有后代数据
const clearDescendantsData = (row: any) => {
	if (row._children && row._children.length > 0) {
		for (const child of row._children) {
			// 递归清空孙节点及更深层的节点
			clearDescendantsData(child);
			// 清空当前子节点的 _children
			child._children = [];
		}
	}
};

const refreshData = () => {
	// 获取所有根节点
	const rootNodes = tableData.value.filter(row => (row._treeLevel || 0) === 0);

	// 收集需要移除的子节点ID
	const idsToRemove = new Set<string>();
	// 收集需要从 expandedKeys 中移除的节点ID（包括根节点及其所有后代）
	const expandedIdsToRemove = new Set<string>();

	for (const row of rootNodes) {
		// 添加根节点ID到移除列表
		expandedIdsToRemove.add(row.id);
		// 先收集所有后代节点ID（在清空 _children 之前）
		const descendantIds = getAllDescendantIds(row);
		descendantIds.forEach(id => {
			idsToRemove.add(id);
			expandedIdsToRemove.add(id); // 同时添加到 expandedKeys 移除列表
		});
		// 递归清空所有后代节点的 _children
		clearDescendantsData(row);
		// 清空根节点的子节点数据
		row._children = [];
	}

	// 从 tableData 中移除所有根节点的后代
	if (idsToRemove.size > 0) {
		tableData.value = tableData.value.filter(item => !idsToRemove.has(item.id));
	}

	// 从 expandedKeys 中移除根节点及其所有后代的展开状态
	expandedKeys.value = expandedKeys.value.filter(key => !expandedIdsToRemove.has(key));

	ElMessage.success('数据已刷新，根节点已重置为未展开状态');
};

// 递归展开单个节点及其所有子节点
const expandNodeRecursively = async (row: any): Promise<number> => {
	let expandedCount = 0;
	const isAlreadyExpanded = expandedKeys.value.includes(row.id);

	// 如果当前节点未展开，则展开它
	if (!isAlreadyExpanded) {
		// 添加到展开状态
		expandedKeys.value.push(row.id);
		expandedCount++;

		// 如果还没有加载子节点，先加载
		if (!row._children || row._children.length === 0) {
			// 标记当前行正在加载
			loadingRows.value.add(row.id);
			try {
				const params = { id: row.id };
				const response = await expandVPMReferenceInfo(params);

				if (response && Array.isArray(response)) {
					row._children = response.map((item: any) => ({
						...item,
						id: item.id || item.physicalid,
						hasChildren: true,
						_treeLevel: (row._treeLevel || 0) + 1,
						_parentId: row.id // 设置父节点ID
					}));

					// 将子节点插入到当前行后面
					const rowIndex = tableData.value.findIndex(item => item.id === row.id);
					if (rowIndex !== -1) {
						tableData.value.splice(rowIndex + 1, 0, ...row._children);
					}
				}
			} catch (error) {
				console.error(`[ExpandAll] 加载节点 ${row.id} 的子节点失败:`, error);
				// 加载失败，从展开状态中移除
				expandedKeys.value = expandedKeys.value.filter(key => key !== row.id);
				return expandedCount - 1; // 减去当前节点
			} finally {
				// 移除加载状态
				loadingRows.value.delete(row.id);
			}
		} else {
			// 已经加载过子节点，直接插入
			const rowIndex = tableData.value.findIndex(item => item.id === row.id);
			if (rowIndex !== -1) {
				tableData.value.splice(rowIndex + 1, 0, ...row._children);
			}
		}
	}

	// 递归展开所有子节点（无论当前节点是否已展开，都要递归处理子节点）
	if (row._children && row._children.length > 0) {
		for (const child of row._children) {
			const childExpandedCount = await expandNodeRecursively(child);
			expandedCount += childExpandedCount;
		}
	}

	return expandedCount;
};

const expandAll = async () => {
	// 判断是否有选中的行
	const hasSelection = selectedRows.value.length > 0;

	// 如果有选中行，则展开选中的行；否则展开所有根节点
	let targetRows: any[];
	if (hasSelection) {
		// 只展开选中的行
		targetRows = selectedRows.value;
	} else {
		// 展开所有根节点（_treeLevel 为 0 或未定义的节点）
		targetRows = tableData.value.filter(row => (row._treeLevel || 0) === 0);
	}

	if (targetRows.length === 0) {
		ElMessage.info('没有可展开的数据');
		return;
	}

	let totalExpandedCount = 0;

	try {
		// 遍历目标行递归展开
		for (const row of targetRows) {
			const count = await expandNodeRecursively(row);
			totalExpandedCount += count;
		}

		if (hasSelection) {
			ElMessage.success(`已展开选中的 ${totalExpandedCount} 个节点`);
		} else {
			ElMessage.success(`已展开 ${totalExpandedCount} 个节点`);
		}
	} catch (error) {
		console.error('[ExpandAll] 展开全部失败:', error);
		ElMessage.error('展开全部失败');
	}
};

const collapseAll = () => {
	// 获取所有展开的节点
	const expandedRows = tableData.value.filter(row => expandedKeys.value.includes(row.id));

	// 收集所有需要删除的子节点ID
	const idsToRemove = new Set<string>();
	for (const row of expandedRows) {
		const descendantIds = getAllDescendantIds(row);
		descendantIds.forEach(id => idsToRemove.add(id));
	}

	// 从 tableData 中移除所有子节点
	if (idsToRemove.size > 0) {
		tableData.value = tableData.value.filter(item => !idsToRemove.has(item.id));
	}

	// 清空展开状态
	expandedKeys.value = [];

	ElMessage.success('已收起全部');
};

const clearAll = async () => {
	try {
		await ElMessageBox.confirm('确定要清空所有数据吗？', '确认清空', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		});

		tableData.value = [];
		selectedRows.value = [];
		lastDroppedId.value = '';
		expandedKeys.value = []; // 清空展开状态
		ElMessage.success('已清空所有数据');
	} catch {
		// 用户取消
	}
};

// 递归获取所有子节点（包括孙子节点）的ID
const getAllDescendantIds = (row: any): string[] => {
	const ids: string[] = [];
	if (row._children && row._children.length > 0) {
		for (const child of row._children) {
			ids.push(child.id);
			// 递归获取孙节点
			ids.push(...getAllDescendantIds(child));
		}
	}
	return ids;
};

// 清除选中的数据
const clearSelected = async () => {
	try {
		await ElMessageBox.confirm(`确定要清除选中的 ${selectedRows.value.length} 条数据吗？`, '确认清除', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning'
		});

		// 获取选中的ID列表，包括所有子节点和孙节点
		const idsToRemove = new Set<string>();
		for (const row of selectedRows.value) {
			idsToRemove.add(row.id);
			// 添加所有后代节点的ID
			const descendantIds = getAllDescendantIds(row);
			descendantIds.forEach(id => idsToRemove.add(id));
		}

		// 过滤掉选中的行及其所有后代
		tableData.value = tableData.value.filter(item => !idsToRemove.has(item.id));

		// 从 expandedKeys 中移除已删除的节点
		expandedKeys.value = expandedKeys.value.filter(key => !idsToRemove.has(key));

		// 清空表格选中状态
		tableRef.value?.clearSelection?.();

		// 清空选中状态
		selectedRows.value = [];

		ElMessage.success('已清除选中的数据');
	} catch {
		// 用户取消
	}
};
</script>

<style lang="scss" scoped>
.pse-container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background-color: #f5f7fa;

	.pse-toolbar {
		display: flex;
		align-items: center;
		padding: 12px 16px;
		background-color: #fff;
		border-bottom: 1px solid #e4e7ed;
		gap: 8px;
	}

	.pse-content {
		flex: 1;
		padding: 16px;
		overflow: auto;
		background-color: #fff;

		.el-table {
			width: 100%;
			height: 100%;
		}

		.empty-tip {
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-items: center;
			height: 100%;
			padding: 40px;
			border: 2px dashed #dcdfe6;
			border-radius: 8px;
			background-color: #fafafa;

			.hint-text {
				margin-top: 20px;
				text-align: center;

				.main-text {
					font-size: 16px;
					font-weight: 500;
					color: #303133;
				}

				.sub-text {
					font-size: 14px;
					color: #909399;
					margin-top: 8px;
				}
			}
		}
	}

	.pse-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 10px 16px;
		background-color: #fff;
		border-top: 1px solid #e4e7ed;
		font-size: 13px;
		color: #606266;

		.footer-left {
			display: flex;
			align-items: center;
			gap: 16px;

			.selected-count {
				color: #e6a23c;
				font-weight: 500;
			}
		}

		.last-dropped {
			color: #409eff;
			font-weight: 500;
		}
	}

	// 底部工具栏容器
	.bottom-toolbar-container {
		background-color: #f5f7fa;
		border-top: 1px solid #dcdfe6;

		// 主工具栏（标签页样式）
		.main-toolbar-tabs {
			display: flex;
			justify-content: center;
			background-color: #e4e7ed;
			border-bottom: 1px solid #dcdfe6;

			.tab-item {
				padding: 8px 20px;
				cursor: pointer;
				font-size: 14px;
				color: #606266;
				background-color: #e4e7ed;
				border-right: 1px solid #dcdfe6;
				transition: all 0.3s;

				&:hover {
					background-color: #d4d7de;
				}

				&.active {
					background-color: #909399;
					color: #fff;
				}

				&:last-child {
					border-right: none;
				}
			}
		}

		// 子工具栏（图标按钮）
		.sub-toolbar-icons {
			display: flex;
			justify-content: center;
			align-items: center;
			padding: 10px 16px;
			gap: 16px;
			background-color: #f5f7fa;
			border-bottom: 1px solid #dcdfe6;

			.icon-btn {
				width: 36px;
				height: 36px;
				display: flex;
				align-items: center;
				justify-content: center;
				cursor: pointer;
				border-radius: 4px;
				transition: all 0.3s;
				color: #606266;

				&:hover {
					background-color: #e4e7ed;
					color: #409eff;
				}

				.el-icon {
					font-size: 24px;
				}
			}
		}

		// 折叠/展开按钮（底部箭头）
		.toolbar-toggle-bar {
			display: flex;
			justify-content: center;
			align-items: center;
			padding: 4px 0;
			cursor: pointer;
			background-color: #e4e7ed;
			transition: background-color 0.3s;

			&:hover {
				background-color: #d4d7de;
			}

			.el-icon {
				font-size: 14px;
				color: #606266;
				transition: transform 0.3s;

				&.is-collapsed {
					transform: rotate(180deg);
				}
			}
		}
	}
}

// 标题单元格样式
.title-cell {
	display: inline-flex;
	align-items: center;
}

// 隐藏 el-table 默认的树形元素
.custom-tree-table {
	:deep(.el-table__expand-icon) {
		display: none !important;
	}
	:deep(.el-table__indent) {
		display: none !important;
	}
	:deep(.el-table__placeholder) {
		display: none !important;
	}

	// 高亮当前行样式
	:deep(.el-table__body tr.current-row > td) {
		background-color: #ecf5ff !important;
	}

	// 选中行样式（复选框选中）
	:deep(.el-table__body tr.el-table__row--striped.selected-row > td, .el-table__body tr.selected-row > td) {
		background-color: #f5f7fa;
	}
}

// 类型图标样式
.type-icon {
	width: 20px;
	height: 20px;
	margin-right: 6px;
	vertical-align: middle;
}

// CAD 图标样式
.cad-icon {
	width: 24px;
	height: 24px;
	vertical-align: middle;
}

// 自定义展开图标
.custom-tree-icon {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	width: 16px;
	height: 16px;
	margin-right: 6px;
	cursor: pointer;
	border: 1px solid #909399;
	border-radius: 2px;
	background-color: #fff;
	flex-shrink: 0;

	&:hover {
		background-color: #f5f7fa;
		border-color: #409eff;
	}

	.el-icon {
		font-size: 12px;
		color: #606266;
	}

	&:hover .el-icon {
		color: #409eff;
	}
}

// 占位符（没有子节点时）
.tree-icon-placeholder {
	display: inline-block;
	width: 16px;
	height: 16px;
	margin-right: 6px;
	flex-shrink: 0;
}
</style>

<style>
/* 非 scoped 全局样式 - 确保默认展开图标被隐藏 */
.el-table__expand-icon {
	display: none !important;
}
</style>
