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
  actionKind: 'CREATE' | 'UPDATE' | 'QUERY' | 'ACTION';
  jpoName: string;
  methodName: string;
  httpMethod: 'POST';
  inputMapping: Record<string, unknown>;
  outputMapping: Record<string, unknown>;
  inputParameters: PlmActionParameterDefinition[];
  outputParameters: PlmActionParameterDefinition[];
  enabled: boolean;
  updatedAt?: string;
}

export interface PlmActionParameterDefinition {
  name: string;
  dataType: 'ANY' | 'STRING' | 'NUMBER' | 'BOOLEAN' | 'OBJECT' | 'ARRAY';
  required: boolean;
  description: string;
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

export type PlmEventName = 'init' | 'click' | 'submit' | 'change' | 'rowClick' | 'selectionChange' | 'drop';

export type PlmEffectType = 'SET_DATA' | 'RELOAD' | 'REFRESH_ROW' | 'APPEND_ROWS' | 'REMOVE_ROWS'
  | 'RESET' | 'OPEN_DIALOG' | 'OPEN_DRAWER' | 'CLOSE' | 'OPEN_DETAIL' | 'NAVIGATE'
  | 'NOTIFY' | 'CHAIN_ACTION';

export interface PlmActionInvocation {
  actionCode: string;
  inputMapping: Record<string, unknown>;
}

export interface PlmEventEffect {
  type: PlmEffectType;
  target?: string;
  mapping?: unknown;
  level?: 'success' | 'info' | 'warning' | 'error';
  message?: string;
  position?: 'first' | 'last';
  deduplicateBy?: string;
  action?: PlmActionInvocation;
}

export interface PlmEventBindingV2 {
  id: string;
  source: {
    componentId: string;
    event: PlmEventName;
    acceptedTypes?: string[];
  };
  when?: string;
  action: PlmActionInvocation;
  success: PlmEventEffect[];
  failure: PlmEventEffect[];
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
  drop?: TableDropBinding;
}

export interface TableColumnBinding {
  columnName: string;
  fieldCode: string;
}

export interface TableDropBinding {
  actionCode: string;
  acceptedTypes: string[];
}

export interface SearchBinding {
  componentId: string;
  formId: string;
  valueField: string;
  labelField: string;
  searchParams: string;
}

export interface PlmPageConfig {
  bindingVersion: 1 | 2;
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
  eventBindings: PlmEventBindingV2[];
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
  bindingVersion: 2,
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
  searchBindings: [],
  eventBindings: []
});

export const normalizePlmConfig = (value?: Partial<PlmPageConfig>): PlmPageConfig => {
  const empty = emptyPlmConfig();
  const eventBindings = value?.eventBindings || [];
  const eventActionCodes = eventBindings.flatMap(binding => [
    binding.action?.actionCode,
    ...[...(binding.success || []), ...(binding.failure || [])]
      .filter(effect => effect.type === 'CHAIN_ACTION')
      .map(effect => effect.action?.actionCode)
  ]);
  return {
    bindingVersion: value?.bindingVersion === 2 || eventBindings.length ? 2 : value ? 1 : 2,
    fieldCodes: [...new Set([...(value?.fieldCodes || []), ...(value?.fieldBindings || []).map(item => item.fieldCode), ...(value?.tableBindings || []).flatMap(item => (item.columnBindings || []).map(column => column.fieldCode))].filter(Boolean))],
    actionCodes: [...new Set([...(value?.actionCodes || []), ...(value?.dataBindings || []).map(item => item.actionCode), ...(value?.actionBindings || []).map(item => item.actionCode), ...(value?.tableBindings || []).flatMap(item => [item.queryActionCode, item.drop?.actionCode]), ...eventActionCodes].filter(Boolean) as string[])],
    context: {...empty.context, ...(value?.context || {})},
    fieldBindings: value?.fieldBindings || [],
    dataBindings: value?.dataBindings || [],
    actionBindings: value?.actionBindings || [],
    tableBindings: (value?.tableBindings || []).map(item => ({
      ...item,
      columnBindings: item.columnBindings || [],
      drop: item.drop?.actionCode ? {...item.drop, acceptedTypes: item.drop.acceptedTypes || []} : undefined
    })),
    searchBindings: value?.searchBindings || [],
    eventBindings
  };
};

export const upgradePlmConfigToV2 = (config: PlmPageConfig): PlmPageConfig => {
  const convertedEvents: PlmEventBindingV2[] = config.actionBindings.map((binding, index) => {
    const success: PlmEventEffect[] = [];
    if (binding.successAction === 'REFRESH') success.push({type: 'RELOAD'});
    if (binding.successAction === 'CLOSE') success.push({type: 'CLOSE'});
    if (binding.successAction === 'OPEN_DETAIL') {
      success.push({type: 'OPEN_DETAIL', mapping: '${response.data.objectId}'});
    }
    return {
      id: `v1-${binding.componentId}-${binding.event}-${index}`,
      source: {componentId: binding.componentId, event: binding.event},
      action: {actionCode: binding.actionCode, inputMapping: {}},
      success,
      failure: [{type: 'NOTIFY', level: 'error', message: '${response.msg}'}]
    };
  });
  return normalizePlmConfig({
    ...config,
    bindingVersion: 2,
    actionBindings: [],
    eventBindings: [...config.eventBindings, ...convertedEvents]
  });
};
