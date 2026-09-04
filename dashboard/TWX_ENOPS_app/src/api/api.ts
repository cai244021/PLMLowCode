import BaseAPI from './base';

class API extends BaseAPI {
	constructor() {
		super('/');
	}

	// 通用请求方法
	async getFileData(params: any) {
		const response = await this.post(`xxx/xxx`, params);
		return response;
	}
}

export default new API();
