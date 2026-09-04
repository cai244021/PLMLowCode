# vite-project

## 当前项目位置

本模块工作目录：`D:\PLMLowCode\dashboard\TWX_ENOPS_app`。
下文安装、启动和构建命令均在本目录执行；构建输出为本目录下的 `dist`。

编辑器在 `D:\PLMLowCode\app`，PLM增量在 `D:\PLMLowCode\plm`。
共用Runtime源码位于 `D:\PLMLowCode\plm\3dspace\common\JFLowCode`。
Widget运行时仍通过PLM服务地址加载 `/common/JFLowCode/` 资源，不读取本机源码目录。

This template should help get you started developing with Vue 3 in Vite.

## Recommended IDE Setup

[VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur).

## Type Support for `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) to make the TypeScript language service aware of `.vue` types.

## Customize configuration

See [Vite Configuration Reference](https://vite.dev/config/).

## Project Setup

```sh
pnpm install
```

### Compile and Hot-Reload for Development

```sh
pnpm dev
```

### Type-Check, Compile and Minify for Production

```sh
pnpm build
```

### Lint with [ESLint](https://eslint.org/)

<!-- 语法格式检查 -->

```sh
pnpm lint
```

### Lint style

<!-- 样式检查 -->

```sh
pnpm lint:stylelint
```
