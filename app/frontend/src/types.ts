import type {SchemaObject} from 'amis';

export interface PlmFieldDefinition {
  fieldCode: string;
  displayName: string;
  objectType: string;
  sourceType: 'BASIC' | 'ATTRIBUTE' | 'RELATIONSHIP' | 'PROGRAM';
  schemaName: string;
  dataType: 'string' | 'textarea' | 'number' | 'boolean' | 'date' | 'datetime' | 'enum' | 'object';
  required: boolean;
  editable: boolean;
  multiple: boolean;
  i18nKey: string;
  rangeSource: 'NONE' | 'FIXED' | 'PLM_RANGE' | 'JPO';
  rangeConfig: Record<string, unknown>;
  updatedAt?: string;
}

export interface PlmActionDefinition {
  actionCode: string;
  actionName: string;
  actionKind: 'CREATE' | 'UPDATE' | 'QUERY' | 'ACTION' | 'NAVIGATION';
  jpoName: string;
  methodName: string;
  httpMethod: 'GET' | 'POST';
  inputMapping: Record<string, unknown>;
  outputMapping: Record<string, unknown>;
  enabled: boolean;
  updatedAt?: string;
}

export interface FieldBinding {
  componentId: string;
  fieldCode: string;
  valueKey: string;
}

export interface ActionBinding {
  componentId: string;
  event: 'click' | 'submit' | 'change';
  actionCode: string;
  successAction: 'NONE' | 'REFRESH' | 'CLOSE' | 'OPEN_DETAIL';
}

export interface TableBinding {
  componentId: string;
  queryActionCode: string;
  itemsPath: string;
  totalPath: string;
  objectIdField: string;
  relIdField: string;
}

export interface PlmPageConfig {
  context: {
    objectIdParam: string;
    parentOidParam: string;
    relIdParam: string;
  };
  fieldBindings: FieldBinding[];
  actionBindings: ActionBinding[];
  tableBindings: TableBinding[];
}

export interface PageResponse {
  pageCode: string;
  pageName: string;
  currentVersion: number;
  schema: SchemaObject;
  plmConfig?: Partial<PlmPageConfig>;
  updatedAt: string;
}

export interface SchemaComponent {
  id: string;
  type: string;
  name: string;
  label: string;
}

export const emptyPlmConfig = (): PlmPageConfig => ({
  context: {
    objectIdParam: 'objectId',
    parentOidParam: 'parentOID',
    relIdParam: 'relId'
  },
  fieldBindings: [],
  actionBindings: [],
  tableBindings: []
});

export const normalizePlmConfig = (value?: Partial<PlmPageConfig>): PlmPageConfig => {
  const empty = emptyPlmConfig();
  return {
    context: {...empty.context, ...(value?.context || {})},
    fieldBindings: value?.fieldBindings || [],
    actionBindings: value?.actionBindings || [],
    tableBindings: value?.tableBindings || []
  };
};
