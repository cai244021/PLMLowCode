type DebounceFunction = (...args: any[]) => void;

export const useDebounce = <T extends DebounceFunction>(func: T, wait: number): T => {
	let timeout: ReturnType<typeof setTimeout> | undefined;

	return function (this: any, ...args: Parameters<T>): void {
		const context = this;

		if (timeout) {
			clearTimeout(timeout);
		}

		timeout = setTimeout(() => {
			func.apply(context, args);
		}, wait);
	} as T;
};

type ThrottleFunction = (...args: any[]) => void;

export const throttle = <T extends ThrottleFunction>(func: T, wait: number): T => {
	let inThrottle: boolean = false;

	return function (this: any, ...args: Parameters<T>): void {
		const context = this;

		if (!inThrottle) {
			func.apply(context, args);
			inThrottle = true;
			setTimeout(() => {
				inThrottle = false;
			}, wait);
		}
	} as T;
};
