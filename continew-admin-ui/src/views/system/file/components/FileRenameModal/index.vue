<template>
  <a-modal
    v-model:visible="visible"
    title="重命名"
    width="90%"
    modal-animation-name="el-fade"
    :modal-style="{ maxWidth: '500px' }"
    @close="cancel"
    @before-ok="save"
  >
    <a-row justify="center" align="center">
      <a-form
        ref="FormRef"
        :model="form"
        auto-label-width
        :style="{ width: '80%' }"
      >
        <a-form-item
          field="name"
          label="文件名称"
          :rules="[{ required: true, message: '请输入文件名称' }]"
        >
          <a-input v-model="form.name" placeholder="请输入" allow-clear />
        </a-form-item>
      </a-form>
    </a-row>
  </a-modal>
</template>

<script setup lang="ts">
  import type { FormInstance, Modal } from '@arco-design/web-vue';
  import type { FileItem } from '@/api/system/file';
  import { onMounted, reactive, ref } from 'vue';

  interface Props {
    fileInfo: FileItem;
    onClose: () => void;
  }
  const props = withDefaults(defineProps<Props>(), {});

  const visible = ref(false);
  type Form = { name: string };
  const form: Form = reactive({
    name: '',
  });

  onMounted(() => {
    form.name = props.fileInfo?.name || '';
    visible.value = true;
  });

  const cancel = () => {
    visible.value = false;
    props.onClose();
  };

  // 模拟接口
  const saveApi = (): Promise<boolean> => {
    return new Promise((resolve) => {
        setTimeout(() => {
          resolve(true);
        }, 2000)
      }
    );
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
  :deep(.arco-form-item) {
    margin-bottom: 0;
  }
  :deep(.arco-form-item-label-col > label) {
    white-space: nowrap;
  }
</style>
