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
  rangeSource: 'NONE' | 'FIXED' | 'PLM_RANGE' | 'PLM_STATE' | 'JPO';
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

export interface DataBinding {
  componentId: string;
  trigger: 'INIT';
  actionCode: string;
}

export interface TableBinding {
  componentId: string;
  queryActionCode: string;
  itemsPath: string;
  totalPath: string;
  objectIdField: string;
  relIdField: string;
  columnBindings: TableColumnBinding[];
}

export interface TableColumnBinding {
  columnName: string;
  fieldCode: string;
}

export interface SearchBinding {
  componentId: string;
  formId: string;
  valueField: string;
  labelField: string;
  searchParams: string;
}

export interface PlmPageConfig {
  fieldCodes: string[];
  actionCodes: string[];
  context: {
    objectIdParam: string;
    parentOidParam: string;
    relIdParam: string;
  };
  fieldBindings: FieldBinding[];
  dataBindings: DataBinding[];
  actionBindings: ActionBinding[];
  tableBindings: TableBinding[];
  searchBindings: SearchBinding[];
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
  fieldCodes: [],
  actionCodes: [],
  context: {
    objectIdParam: 'objectId',
    parentOidParam: 'parentOID',
    relIdParam: 'relId'
  },
  fieldBindings: [],
  dataBindings: [],
  actionBindings: [],
  tableBindings: [],
  searchBindings: []
});

export const normalizePlmConfig = (value?: Partial<PlmPageConfig>): PlmPageConfig => {
  const empty = emptyPlmConfig();
  return {
    fieldCodes: [...new Set([...(value?.fieldCodes || []), ...(value?.fieldBindings || []).map(item => item.fieldCode), ...(value?.tableBindings || []).flatMap(item => (item.columnBindings || []).map(column => column.fieldCode))].filter(Boolean))],
    actionCodes: [...new Set([...(value?.actionCodes || []), ...(value?.dataBindings || []).map(item => item.actionCode), ...(value?.actionBindings || []).map(item => item.actionCode), ...(value?.tableBindings || []).map(item => item.queryActionCode)].filter(Boolean))],
    context: {...empty.context, ...(value?.context || {})},
    fieldBindings: value?.fieldBindings || [],
    dataBindings: value?.dataBindings || [],
    actionBindings: value?.actionBindings || [],
    tableBindings: (value?.tableBindings || []).map(item => ({...item, columnBindings: item.columnBindings || []})),
    searchBindings: value?.searchBindings || []
  };
};
