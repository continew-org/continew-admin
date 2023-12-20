<template>
  <a-modal
    v-model:visible="visible"
    title="移动到"
    width="90%"
    modal-animation-name="el-dialog"
    mask-animation-name="el-mask"
    :modal-style="{ maxWidth: '500px' }"
    @close="cancel"
    @before-ok="save"
  >
    <a-form
      ref="FormRef"
      :model="form"
      :style="{ width: '100%' }"
      auto-label-width
    >
      <a-form-item
        field="path"
        label="目标路径"
        :rules="[{ required: true, message: '请输入目标路径' }]"
      >
        <a-input v-model="form.path" placeholder="请输入" />
      </a-form-item>
    </a-form>
    <section class="tree-box">
      <a-tree
        show-line
        size="mini"
        block-node
        :data="treeData"
        @select="handleClickNode"
      >
        <template #switcher-icon="node, { expanded }">
          <GiSvgIcon
            v-if="node.children && expanded"
            class="switcher-icon"
            name="plus-square"
            :size="16"
          />
          <GiSvgIcon
            v-else-if="node.children && !expanded"
            class="switcher-icon"
            name="minus-square"
            :size="16"
            style="transform: rotate(0deg)"
          />
          <icon-drive-file v-else :size="16" />
        </template>
        <template #icon>
          <GiSvgIcon name="menu-zip" :size="16"></GiSvgIcon>
        </template>
      </a-tree>
    </section>
  </a-modal>
</template>

<script setup lang="ts">
  import type { FormInstance, Modal, TreeInstance } from '@arco-design/web-vue';
  import type { FileItem } from '@/api/system/file';
  import { onMounted, reactive, ref } from 'vue';
  // import GiSvgIcon from '@/components/GiSvgIcon/index.vue';

  interface Props {
    fileInfo: FileItem;
    onClose: () => void;
  }
  const props = withDefaults(defineProps<Props>(), {});

  const visible = ref(false);
  type TForm = { path: string };
  const form: TForm = reactive({ path: '/' });
  const treeData = ref<object[]>([]);

  treeData.value = [
    {
      title: '图片文件夹',
      key: '0-0',
      children: [
        {
          title: '图片文件夹1',
          key: '0-0-0',
          children: [
            { title: '图片文件夹1-1', key: '0-0-0-0' },
            { title: '图片文件夹1-2', key: '0-0-0-1' },
            { title: '图片文件夹1-3', key: '0-0-0-2' },
          ],
        },
        {
          title: '新建文件夹',
          key: '0-0-1',
        },
        {
          title: '视频文件夹',
          key: '0-0-2',
          children: [
            { title: '视频文件夹1', key: '0-0-2-0' },
            { title: '视频文件夹2', key: '0-0-2-1' },
          ],
        },
      ],
    },
    {
      title: '音频文件夹',
      key: '0-1',
    },
    {
      title: '音频文件夹1',
      key: '0-2',
      children: [
        {
          title: '音频文件夹1-1',
          key: '0-2-0',
          children: [
            { title: '音频文件夹1-1-1', key: '0-2-0-0' },
            { title: '音频文件夹1-1-2', key: '0-2-0-1' },
          ],
        },
      ],
    },
  ];

  onMounted(() => {
    visible.value = true;
  });

  const handleClickNode: TreeInstance['onSelect'] = (selectedKeys, data) => {
    form.path = `/${data.selectedNodes[0].title}`;
  };

  const cancel = () => {
    visible.value = false;
    props.onClose();
  };

  // 模拟接口
  const saveApi = (): Promise<boolean> => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(true);
      }, 2000);
    });
  };

  const FormRef = ref<FormInstance | null>(null);
  const save: InstanceType<typeof Modal>['onBeforeOk'] = async () => {
    const flag = await FormRef.value?.validate();
    if (flag) return false;
    return saveApi();
  };
</script>

<style lang="less" scoped>
  .label {
    color: var(--color-text-2);
  }
  .switcher-icon {
    fill: var(--color-text-2);
  }
  :deep(.arco-form-item-label-col > label) {
    white-space: nowrap;
  }
  :deep(.arco-tree-node-switcher-icon) {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .tree-box {
    width: 100%;
    height: 300px;
    padding: 10px 16px;
    box-sizing: border-box;
    border: 1px solid var(--color-border);
    overflow: auto;
  }
</style>
