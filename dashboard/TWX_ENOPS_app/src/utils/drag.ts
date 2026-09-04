import type { Ref } from 'vue';

interface ResizeOptions {
	rightRef?: Ref<HTMLElement | null>;
	bottomRef?: Ref<HTMLElement | null>;
}

const useResize = ({ rightRef, bottomRef }: ResizeOptions) => {
	let startX = 0;
	let startWidth = 0;
	let isHorizonResizing = false;
	let startY = 0;
	let startHeight = 0;
	let isVerticalResizing = false;

	const startHorizonResize = (event: MouseEvent) => {
		if (rightRef && rightRef.value) {
			startX = event.clientX;
			startWidth = rightRef.value.offsetWidth;
			isHorizonResizing = true;

			window.addEventListener('mousemove', doHorizonResize);
			window.addEventListener('mouseup', stopHorizonResize);
		}
	};

	const doHorizonResize = (event: MouseEvent) => {
		if (isHorizonResizing && rightRef && rightRef.value) {
			const deltaX = event.clientX - startX;
			rightRef.value.style.width = startWidth - deltaX + 'px';
		}
	};

	const stopHorizonResize = () => {
		isHorizonResizing = false;
		window.removeEventListener('mousemove', doHorizonResize);
		window.removeEventListener('mouseup', stopHorizonResize);
	};

	const startVerticalResize = (event: MouseEvent) => {
		if (bottomRef && bottomRef.value) {
			startY = event.clientY;
			startHeight = bottomRef.value.offsetHeight;
			isVerticalResizing = true;
			window.addEventListener('mousemove', doVerticalResize);
			window.addEventListener('mouseup', stopVerticalResize);
		}
	};

	const doVerticalResize = (event: MouseEvent) => {
		if (isVerticalResizing && bottomRef && bottomRef.value) {
			const deltaY = event.clientY - startY;
			bottomRef.value.style.height = startHeight - deltaY + 'px';
		}
	};

	const stopVerticalResize = () => {
		isVerticalResizing = false;
		window.removeEventListener('mousemove', doVerticalResize);
		window.removeEventListener('mouseup', stopVerticalResize);
	};

	return {
		startHorizonResize,
		startVerticalResize
	};
};

export default useResize;
