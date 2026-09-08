import http from '@/utils/ds-request';

/**
 * 调用 TWXPublicRest/TWXTicketService 接口获取 VPMReference 信息
 * @param dragData 拖拽进来的数据
 * @returns Promise<any>
 */
export async function getVPMReferenceInfo(dragData: any) {
	const url = `/TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=getVPMReferenceInfo`;
	const response = await http.post(url, dragData);
	return response;
}

/**
 * 展开 VPMReference 获取子节点数据
 * @param params 包含父节点信息的对象
 * @returns Promise<any>
 */
export async function expandVPMReferenceInfo(params: any) {
	const url = `/TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=expandVPMReferenceInfo`;
	const response = await http.post(url, params);
	return response;
}

/**
 * 导出报表响应类型
 */
export interface ExportReportResult {
	code?: string;
	msg?: string;
	fileName?: string;
	content?: string;
	contentType?: string;
	data?: ExportReportResult;
}

/**
 * 导出报表
 * @param params 包含节点ID数组的对象
 * @returns Promise<string | ExportReportResult>
 */
export async function exportReport(params: { nodeIds: string[] }): Promise<string | ExportReportResult> {
	const url = `/TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=exportReport`;
	const response = await http.post(url, params);
	return response as string | ExportReportResult;
}

/**
 * 创建 VPMReference partInfo 参数类型
 */
export interface CreateVPMReferencePartInfo {
	type: 'assembly' | 'component';
	title: string;
	partType: string;
	chineseDesc?: string;
	englishDesc?: string;
	remark?: string;
}

/**
 * 创建 VPMReference 参数类型
 */
export interface CreateVPMReferenceParams {
	partInfo: CreateVPMReferencePartInfo;
	parentId?: string[];
}

/**
 * 创建 VPMReference 响应类型
 */
export interface CreateVPMReferenceResult {
	result?: Array<{
		physicalid: string;
		type: string;
	}>;
	status?: string;
}

/**
 * 创建 VPMReference
 * @param params 创建参数
 * @returns Promise<CreateVPMReferenceResult>
 */
export async function createVPMReferenceV5ByRest(params: CreateVPMReferenceParams): Promise<CreateVPMReferenceResult> {
	const url = `/TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=createVPMReferenceV5ByRest`;
	const response = await http.post(url, params as unknown as Record<string, unknown>);
	return response as CreateVPMReferenceResult;
}

/**
 * 执行当前已发布页面登记的PLM低代码动作
 * @param pageCode 已发布页面编码
 * @param actionCode 设计器配置的动作编码
 * @param params 页面数据和PLM上下文
 * @returns Promise<any> AMIS标准响应
 */
export async function executeLowCodeAction(pageCode: string, actionCode: string, params: Record<string, unknown>): Promise<any> {
	//20260906 update by caipan 动作入口按页面发布快照解析JPO，不再依赖JSP业务白名单。
	const url = `/common/JF_LowCodeAction.jsp?pageCode=${encodeURIComponent(pageCode)}&actionCode=${encodeURIComponent(actionCode)}`;
	return http.post(url, params);
}

/**
 * 检查选中数据是否可以创建
 * @param selectIds 选中的数据ID数组
 * @returns Promise<{result: boolean}>
 */
export async function getCADOriginsTypes(selectIds: string[]): Promise<{result: boolean}> {
	const url = `/TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=getCADOriginsTypes`;
	const response = await http.post(url, { selectIds });
	return response as {result: boolean};
}

/**
 * 零件详细信息字段类型
 */
export interface PartInfoField {
	display: string;
	value: string;
	type?: 'textbox' | 'combobox' | string;
	access?: 'r' | 'w' | string;
	name?: string; // 真实的字段名，用于保存到数据库
}

/**
 * 保存零件信息响应类型
 */
export interface SavePartInfoResult {
	status: 'success' | 'error';
	message?: string;
}

/**
 * 零件详细信息响应类型
 */
export interface PartInfoResponse {
	[id: string]: PartInfoField;
}

/**
 * 获取零件详细信息
 * @param id 零件ID
 * @returns Promise<PartInfoResponse>
 */
export async function getPartInfo(id: string): Promise<PartInfoResponse> {
	const url = `/TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=getPartInfo`;
	const response = await http.post(url, { id });
	return response as PartInfoResponse;
}

/**
 * 保存零件信息
 * @param id 零件ID
 * @param data 零件信息数据
 * @returns Promise<SavePartInfoResult>
 */
export async function savePartInfo(id: string, data: PartInfoResponse): Promise<SavePartInfoResult> {
	const url = `/TWXPublicRest/TWXTicketService?JPOName=JF_DataInterface&FuncName=savePartInfo`;
	const response = await http.post(url, { id, data });
	return response as SavePartInfoResult;
}

export default {
	getVPMReferenceInfo,
	expandVPMReferenceInfo,
	exportReport,
	createVPMReferenceV5ByRest,
	executeLowCodeAction,
	getCADOriginsTypes,
	getPartInfo,
	savePartInfo
};
