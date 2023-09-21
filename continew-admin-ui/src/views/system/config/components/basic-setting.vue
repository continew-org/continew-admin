<template>
  <a-form
    ref="formRef"
    :model="form"
    :rules="rules"
    :label-col-props="{ span: 2 }"
    :wrapper-col-props="{ span: 10 }"
    size="large"
    :disabled="!isEdit"
  >
    <a-list class="list-layout" :bordered="false">
      <a-list-item>
        <a-form-item class="image-item" hide-label field="favicon">
          {{ siteFavicon?.name }}
          <template #extra>
            {{ siteFavicon?.description }}
            <br />
            <a-upload
              :file-list="faviconFile ? [faviconFile] : []"
              :show-file-list="false"
              :custom-request="handleUploadFavicon"
              @change="handleChangeFavicon"
            >
              <template #upload-button>
                <div
                  :class="`arco-upload-list-item${
                    faviconFile && faviconFile.status === 'error'
                      ? ' arco-upload-list-item-error'
                      : ''
                  }`"
                >
                  <div
                    v-if="faviconFile && faviconFile.url"
                    class="arco-upload-list-picture custom-upload-avatar favicon"
                  >
                    <img :src="faviconFile.url" />
                    <div
                      v-if="isEdit"
                      class="arco-upload-list-picture-mask favicon"
                    >
                      <IconEdit />
                    </div>
                  </div>
                  <div v-else class="arco-upload-picture-card favicon">
                    <div class="arco-upload-picture-card-text">
                      <icon-upload />
                    </div>
                  </div>
                </div>
              </template>
            </a-upload>
          </template>
        </a-form-item>
      </a-list-item>
      <a-list-item>
        <a-form-item class="image-item" hide-label field="site_logo">
          {{ siteLogo?.name }}
          <template #extra>
            {{ siteLogo?.description }}
            <br />
            <a-upload
              :file-list="logoFile ? [logoFile] : []"
              :show-file-list="false"
              :custom-request="handleUploadLogo"
              @change="handleChangeLogo"
            >
              <template #upload-button>
                <div
                  :class="`arco-upload-list-item${
                    logoFile && logoFile.status === 'error'
                      ? ' arco-upload-list-item-error'
                      : ''
                  }`"
                >
                  <div
                    v-if="logoFile && logoFile.url"
                    class="arco-upload-list-picture custom-upload-avatar logo"
                  >
                    <img :src="logoFile.url" />
                    <div
                      v-if="isEdit"
                      class="arco-upload-list-picture-mask logo"
                    >
                      <IconEdit />
                    </div>
                  </div>
                  <div v-else class="arco-upload-picture-card logo">
                    <div class="arco-upload-picture-card-text">
                      <icon-upload />
                    </div>
                  </div>
                </div>
              </template>
            </a-upload>
          </template>
        </a-form-item>
      </a-list-item>
      <a-list-item style="padding-top: 20px; border: none">
        <a-form-item
          class="input-item"
          :label="siteTitle?.name"
          field="site_title"
        >
          <a-input v-model="form.site_title" placeholder="请输入网站标题" />
        </a-form-item>
        <a-form-item
          class="input-item"
          :label="siteCopyright?.name"
          field="site_copyright"
        >
          <a-input v-model="form.site_copyright" placeholder="请输入版权信息" />
        </a-form-item>
        <div style="margin: 0 0 0 3px">
          <a-space>
            <a-button
              v-if="!isEdit"
              v-permission="['system:config:update']"
              type="primary"
              @click="toEdit"
            >
              <template #icon><icon-edit /></template>修改
            </a-button>
            <a-button
              v-if="!isEdit"
              v-permission="['system:config:reset']"
              @click="toResetValue"
            >
              <template #icon><icon-redo /></template>恢复默认
            </a-button>
            <a-button
              v-if="isEdit"
              v-permission="['system:config:update']"
              type="primary"
              @click="handleSave"
            >
              <template #icon><icon-save /></template>保存
            </a-button>
            <a-button
              v-if="isEdit"
              v-permission="['system:config:update']"
              @click="reset"
            >
              <template #icon><icon-refresh /></template>重置
            </a-button>
            <a-button
              v-if="isEdit"
              v-permission="['system:config:update']"
              @click="handleCancel"
            >
              <template #icon><icon-redo /></template>取消
            </a-button>
          </a-space>
        </div>
      </a-list-item>
    </a-list>
  </a-form>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, reactive, toRefs } from 'vue';
  import { FileItem, RequestOption } from '@arco-design/web-vue';
  import {
    BasicConfigRecord,
    DataRecord,
    ListParam,
    list,
    resetValue,
  } from '@/api/system/config';

  const { proxy } = getCurrentInstance() as any;
  const dataList = ref<DataRecord[]>([]);
  const isEdit = ref(false);
  const logoFile = ref<FileItem>({ uid: '-1' });
  const faviconFile = ref<FileItem>({ uid: '-2' });
  const siteTitle = ref<DataRecord>();
  const siteCopyright = ref<DataRecord>();
  const siteLogo = ref<DataRecord>();
  const siteFavicon = ref<DataRecord>();

  const data = reactive({
    queryParams: {
      code: ['site_title', 'site_copyright', 'site_logo', 'site_favicon'],
    },
    form: {} as BasicConfigRecord,
    rules: {
      site_title: [{ required: true, message: '请输入系统标题' }],
      site_copyright: [{ required: true, message: '请输入版权信息' }],
    },
  });
  const { queryParams, form, rules } = toRefs(data);

  /**
   * 查询配置
   */
  const getConfig = async (params: ListParam = { ...queryParams.value }) => {
    const res = await list(params);
    dataList.value = res.data;
    siteTitle.value = dataList.value.find(
      (option) => option.code === 'site_title'
    );
    siteCopyright.value = dataList.value.find(
      (option) => option.code === 'site_copyright'
    );
    siteLogo.value = dataList.value.find(
      (option) => option.code === 'site_logo'
    );
    siteFavicon.value = dataList.value.find(
      (option) => option.code === 'site_favicon'
    );
    form.value.site_title = siteTitle.value?.value;
    form.value.site_copyright = siteCopyright.value?.value;
    logoFile.value.url = siteLogo.value?.value;
    faviconFile.value.url = siteFavicon.value?.value;
  };
  getConfig();

  /**
   * 保存
   */
  const handleSave = async () => {
    isEdit.value = false;
  };

  /**
   * 上传 Logo
   *
   * @param options /
   */
  const handleUploadLogo = (options: RequestOption) => {
    console.log(options);
    const controller = new AbortController();
    return {
      abort() {
        controller.abort();
      },
    };
  };

  /**
   * 上传 Favicon
   *
   * @param options /
   */
  const handleUploadFavicon = (options: RequestOption) => {
    console.log(options);
    const controller = new AbortController();
    return {
      abort() {
        controller.abort();
      },
    };
  };

  /**
   * 选择 Logo
   *
   * @param _ /
   * @param currentFile
   */
  const handleChangeLogo = (_: any, currentFile: any) => {
    logoFile.value = {
      ...currentFile,
      // url: URL.createObjectURL(currentFile.file),
    };
  };

  /**
   * 选择 Favicon
   *
   * @param _ /
   * @param currentFile
   */
  const handleChangeFavicon = (_: any, currentFile: any) => {
    faviconFile.value = {
      ...currentFile,
      // url: URL.createObjectURL(currentFile.file),
    };
  };

  /**
   * 恢复默认
   */
  const handleResetValue = async () => {
    await resetValue(queryParams.value);
    proxy.$message.success('恢复成功');
    await getConfig();
  };

  /**
   * 点击恢复默认
   */
  const toResetValue = () => {
    proxy.$modal.warning({
      title: '警告',
      titleAlign: 'start',
      content: '确认恢复基础配置为默认值吗？',
      okText: '确认恢复',
      hideCancel: false,
      onOk: () => {
        handleResetValue();
      },
    });
  };

  /**
   * 开始编辑
   */
  const toEdit = () => {
    isEdit.value = true;
  };

  /**
   * 重置表单
   */
  const reset = () => {
    proxy.$refs.formRef?.resetFields();
  };

  /**
   * 取消
   */
  const handleCancel = () => {
    isEdit.value = false;
    reset();
  };
</script>

<style scoped lang="less">
  .logo {
    width: 48px;
    height: 48px;
    min-width: 48px;
    line-height: 48px;
  }

  .favicon {
    width: 16px;
    height: 16px;
    min-width: 16px;
    line-height: 16px;
  }

  .arco-form .image-item {
    margin-bottom: 0;
  }

  .arco-form .input-item {
    margin-left: -20px;
  }

  :deep(
      .arco-list-medium
        .arco-list-content-wrapper
        .arco-list-content
        > .arco-list-item
    ) {
    padding: 13px;
    border-bottom: 1px solid var(--color-fill-3);
  }
</style>
