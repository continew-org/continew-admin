import type { Component } from 'vue';
import { createApp } from 'vue';
import ArcoVueIcon from '@arco-design/web-vue/es/icon';
import ArcoVue from '@arco-design/web-vue';
import { FileItem } from '@/api/system/file';

import FileRenameModal from './FileRenameModal/index.vue';
import FileDetailModal from './FileDetailModal/index.vue';
import PreviewVideoModal from './PreviewVideoModal/index.vue';
import PreviewAudioModal from './PreviewAudioModal/index.vue';

function createModal<T extends { callback?: () => void }>(
  component: Component,
  options?: T,
) {
  // 创建一个挂载容器
  const el: HTMLElement = document.createElement('div');
  // 挂载组件
  document.body.appendChild(el);

  // 实例化组件, createApp 第二个参数是 props
  const instance = createApp(component, {
    ...options,
    onClose: () => {
      setTimeout(() => {
        instance.unmount();
        document.body.removeChild(el);
        // options?.callback && options?.callback();
      }, 350);
    },
  });

  instance.use(ArcoVue);
  instance.use(ArcoVueIcon);
  instance.mount(el);
}

type TFileOptions = { fileInfo: FileItem; callback?: () => void };

/** 打开 文件重命名 弹窗 */
export function openFileRenameModal(fileItem: FileItem) {
  return createModal<TFileOptions>(FileRenameModal, { fileInfo: fileItem });
}

/** 打开 详情 弹窗 */
export function openFileDetailModal(fileItem: FileItem) {
  return createModal<TFileOptions>(FileDetailModal, { fileInfo: fileItem });
}

/** 预览 视频文件 弹窗 */
export function previewFileVideoModal(fileItem: FileItem) {
  return createModal<TFileOptions>(PreviewVideoModal, { fileInfo: fileItem });
}

/** 预览 音频文件 弹窗 */
let fileAudioId: string = '';
export function previewFileAudioModal(fileItem: FileItem) {
  if (fileAudioId) {
    fileAudioId = '';
  }
  fileAudioId = fileItem.id;
  // eslint-disable-next-line consistent-return
  return createModal<TFileOptions>(PreviewAudioModal, {
    fileInfo: fileItem,
    // 关闭的回调
    callback: () => {
      fileAudioId = '';
    },
  });
}
