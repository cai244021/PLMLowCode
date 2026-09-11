export interface PlmContext {
	objectId: string;
	parentOID: string;
	relId: string;
}

export interface SecurityContextInfo {
	SecurityContext?: string;
	[key: string]: unknown;
}

export interface SearchTarget {
	searchParams?: string;
}

export interface SearchResult {
	objectId: string;
	name?: string;
	displayName?: string;
	cancelled?: boolean;
}

export interface PagePackage {
	schema?: Record<string, unknown>;
	plmConfig?: Record<string, unknown>;
	[key: string]: unknown;
}
