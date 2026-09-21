/// <reference types="vite/client" />

interface Window {
	JFLowCodeRuntime?: {
		protocolVersion: number;
		embed(options: Record<string, unknown>): Promise<unknown>;
		parseJson(text: string): unknown;
		notify(level: string, message: string): void;
	};
}
