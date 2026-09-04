import { defineStore } from 'pinia';
import { widget, requirejsPromise } from '@widget-lab/3ddashboard-utils';
import { ref } from 'vue';
import { setBaseURL } from '@/utils/ds-request';
export const useBaseInfoStore = defineStore('baseInfo', {
	state: () => ({
		spaceUrl: ref<string | undefined | any>(undefined),
		securityContext: ref<string | undefined | any>(undefined),
		// @ts-ignore
		currentUser: parent.dsUserLogin
	}),
	getters: {},
	actions: {
		async fetchSpaceUrl() {
			const i3DXCompassServices = await requirejsPromise('DS/i3DXCompassServices/i3DXCompassServices');
			return new Promise((resolve, reject): void => {
				i3DXCompassServices.getServiceUrl({
					serviceName: '3DSpace',
					platformId: widget.getValue('x3dPlatformId'),
					onComplete: URLResult => {
						console.log('The URL of 3DSpace', URLResult);
						this.spaceUrl = URLResult;
						console.log('the 3dspace ul', this.spaceUrl);
						window.localStorage.setItem('spaceUrl', this.spaceUrl);
						setBaseURL(this.spaceUrl);
						resolve(this.spaceUrl);
						this.getCollaborativeSpace();
					},
					onFailure(error) {
						console.log('Error while fetching the URL', error);
						reject();
					}
				});
			});
		},
		async getCollaborativeSpace() {
			const securityContextURL = '/resources/pno/person/getsecuritycontext';
			const WAFData = await requirejsPromise('DS/WAFData/WAFData');
			return new Promise((resolve, reject) => {
				WAFData.authenticatedRequest(`${this.spaceUrl}${securityContextURL}`, {
					type: 'json',
					onComplete: context => {
						this.securityContext = context.SecurityContext;
						resolve(this.securityContext);
					},
					onFailure(error) {
						console.log('error while fetching the securtiy context ', error);
					}
				});
			});
		}
	}
});
