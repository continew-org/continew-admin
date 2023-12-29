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
          <a-input v-model="form.name" placeholder="请输入文件名称" allow-clear />
        </a-form-item>
      </a-form>
    </a-row>
  </a-modal>
</template>

<script setup lang="ts">
  import { getCurrentInstance, onMounted, reactive, ref } from 'vue';
  import type { FormInstance, Modal } from '@arco-design/web-vue';
  import { FileItem, update } from '@/api/system/file';

  interface Props {
    fileInfo: FileItem;
    onClose: () => void;
  }
  const props = withDefaults(defineProps<Props>(), {});
  const { proxy } = getCurrentInstance() as any;
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

  const FormRef = ref<FormInstance | null>(null);
  const save: InstanceType<typeof Modal>['onBeforeOk'] = async () => {
    const flag = await FormRef.value?.validate();
    if (flag) return false;
    const res = await update({ name: form.name }, props.fileInfo.id);
    // eslint-disable-next-line vue/no-mutating-props
    props.fileInfo.name = form.name;
    proxy.$message.success(res.msg);
    return true;
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
