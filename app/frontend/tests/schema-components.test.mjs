import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import test from 'node:test';
import ts from 'typescript';

const source = readFileSync(new URL('../src/schemaComponents.ts', import.meta.url), 'utf8');
const {outputText} = ts.transpileModule(source, {compilerOptions: {module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2020}});
const {collectPageComponents} = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`);

test('识别带稳定ID的Service初始化查询组件', () => {
  const components = collectPageComponents({
    type: 'page',
    body: [
      {id: 'u:init-service', type: 'service', body: []},
      {id: 'u:list', type: 'crud', columns: []}
    ]
  });
  assert.deepEqual(components.data.map(item => item.id), ['u:init-service']);
  assert.deepEqual(components.tables.map(item => item.id), ['u:list']);
});
