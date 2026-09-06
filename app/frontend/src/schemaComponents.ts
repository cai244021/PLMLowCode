import type {SchemaObject} from 'amis';
import type {SchemaComponent} from './types';

const FIELD_TYPES = new Set([
  'input-text', 'textarea', 'input-number', 'select', 'radios', 'checkboxes',
  'checkbox', 'switch', 'input-date', 'input-datetime', 'input-file',
  'input-image', 'input-tree', 'input-tag', 'input-table', 'hidden'
]);
const ACTION_TYPES = new Set(['button', 'form']);
const TABLE_TYPES = new Set(['crud', 'table', 'table2']);
const DATA_TYPES = new Set(['service']);

export interface PageComponents {
  all: SchemaComponent[];
  fields: SchemaComponent[];
  actions: SchemaComponent[];
  data: SchemaComponent[];
  tables: SchemaComponent[];
}

export function collectPageComponents(schema: SchemaObject): PageComponents {
  const all: SchemaComponent[] = [];
  const visited = new Set<string>();

  const walk = (value: unknown) => {
    if (Array.isArray(value)) {
      value.forEach(walk);
      return;
    }
    if (!value || typeof value !== 'object') {
      return;
    }
    const node = value as Record<string, unknown>;
    if (typeof node.id === 'string' && typeof node.type === 'string' && !visited.has(node.id)) {
      visited.add(node.id);
      const name = typeof node.name === 'string' ? node.name : '';
      const title = typeof node.label === 'string'
        ? node.label
        : typeof node.title === 'string' ? node.title : name || node.type;
      all.push({
        id: node.id,
        type: node.type,
        name,
        label: `${title} · ${node.type} · ${node.id}`
      });
    }
    Object.values(node).forEach(walk);
  };

  walk(schema);
  return {
    all,
    fields: all.filter((component) => FIELD_TYPES.has(component.type)),
    actions: all.filter((component) => ACTION_TYPES.has(component.type)),
    data: all.filter((component) => DATA_TYPES.has(component.type)),
    tables: all.filter((component) => TABLE_TYPES.has(component.type))
  };
}
