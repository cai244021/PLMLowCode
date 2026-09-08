/// <reference types="vite/client" />

interface Window {
	JFLowCodeRuntime?: {
		embed(options: Record<string, unknown>): Promise<unknown>;
		parseJson(text: string): unknown;
	};
}
