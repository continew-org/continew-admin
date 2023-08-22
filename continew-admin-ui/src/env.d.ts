/// <reference types="vite/client" />

declare module '*.vue' {
  import { DefineComponent } from 'vue';
  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
  const component: DefineComponent<{}, {}, any>;
  export default component;
}
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
}

declare module '@kangc/v-md-editor';
declare module '@kangc/v-md-editor/lib/preview';
declare module '@kangc/v-md-editor/lib/theme/github.js';
declare module '@kangc/v-md-editor/lib/plugins/emoji/index';
declare module '@kangc/v-md-editor/lib/plugins/copy-code/index';
declare module '@kangc/v-md-editor/lib/plugins/todo-list/index';
declare module 'highlight.js';
declare module 'highlight.js/lib/languages/json';
