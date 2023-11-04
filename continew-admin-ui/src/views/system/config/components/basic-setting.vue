<template>
  <a-form
    ref="formRef"
    layout="vertical"
    :model="form"
    :rules="rules"
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
              accept="image/*"
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
                    <img :src="getFile(faviconFile.url)" />
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
              accept="image/*"
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
                    <img :src="getFile(logoFile.url)" />
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
      <a-list-item style="padding-top: 13px; border: none">
        <a-form-item
          class="input-item"
          :label="siteTitle?.name"
          field="site_title"
        >
          <a-input
            v-model="form.site_title"
            placeholder="请输入网站标题"
            :max-length="18"
          />
        </a-form-item>
        <a-form-item
          class="input-item"
          :label="siteCopyright?.name"
          field="site_copyright"
          tooltip="支持HTML标签"
        >
          <a-textarea
            v-model="form.site_copyright"
            placeholder="请输入版权信息"
            :auto-size="{
              minRows: 3,
            }"
            show-word-limit
          />
        </a-form-item>
        <div style="margin-top: 20px">
          <a-space>
            <a-button
              v-if="!isEdit"
              v-permission="['system:config:reset']"
              @click="toResetValue"
            >
              <template #icon><icon-undo /></template>恢复默认
            </a-button>
            <a-button
              v-if="!isEdit"
              v-permission="['system:config:update']"
              type="primary"
              @click="toEdit"
            >
              <template #icon><icon-edit /></template>修改
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
              <template #icon><icon-undo /></template>取消
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
    save,
    resetValue,
  } from '@/api/system/config';
  import { upload } from '@/api/common';
  import getFile from '@/utils/file';
  import { useAppStore } from '@/store';

  const { proxy } = getCurrentInstance() as any;
  const dataList = ref<DataRecord[]>([]);
  const isEdit = ref(false);
  const logoFile = ref<FileItem>({ uid: '-1' });
  const faviconFile = ref<FileItem>({ uid: '-2' });
  const siteTitle = ref<DataRecord>();
  const siteCopyright = ref<DataRecord>();
  const siteLogo = ref<DataRecord>();
  const siteFavicon = ref<DataRecord>();
  const appStore = useAppStore();

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
   * 重置表单
   */
  const reset = () => {
    form.value = {
      site_title: siteTitle.value?.value,
      site_copyright: siteCopyright.value?.value,
      site_logo: siteLogo.value?.value,
      site_favicon: siteFavicon.value?.value,
    };
    logoFile.value.url = siteLogo.value?.value;
    faviconFile.value.url = siteFavicon.value?.value;
  };

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
    reset();
  };
  getConfig();

  /**
   * 取消
   */
  const handleCancel = () => {
    isEdit.value = false;
  };

  /**
   * 保存
   */
  const handleSave = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        const optionList: DataRecord[] = Object.entries(form.value).map(
          (item) => {
            return {
              code: item[0],
              value: item[1],
            };
          }
        );
        save(optionList).then((res) => {
          appStore.save(form.value);
          handleCancel();
          proxy.$message.success(res.msg);
        });
      }
    });
  };

  /**
   * 上传 Logo
   *
   * @param options /
   */
  const handleUploadLogo = (options: RequestOption) => {
    const controller = new AbortController();
    (async function requestWrap() {
      const {
        onProgress,
        onError,
        onSuccess,
        fileItem,
        name = 'file',
      } = options;
      onProgress(20);
      const formData = new FormData();
      formData.append(name as string, fileItem.file as Blob);
      upload(formData)
        .then((res) => {
          onSuccess(res);
          form.value.site_logo = res.data;
          proxy.$message.success(res.msg);
        })
        .catch((error) => {
          onError(error);
        });
    })();
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
    const controller = new AbortController();
    (async function requestWrap() {
      const {
        onProgress,
        onError,
        onSuccess,
        fileItem,
        name = 'file',
      } = options;
      onProgress(20);
      const formData = new FormData();
      formData.append(name as string, fileItem.file as Blob);
      upload(formData)
        .then((res) => {
          onSuccess(res);
          form.value.site_favicon = res.data;
          proxy.$message.success(res.msg);
        })
        .catch((error) => {
          onError(error);
        });
    })();
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
    };
  };

  /**
   * 恢复默认
   */
  const handleResetValue = async () => {
    await resetValue(queryParams.value);
    proxy.$message.success('恢复成功');
    await getConfig();
    appStore.save(form.value);
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
</script>

<style scoped lang="less">
  .logo {
    width: 33px;
    height: 33px;
    min-width: 33px;
    line-height: 33px;
  }

  .favicon {
    width: 16px;
    height: 16px;
    min-width: 16px;
    line-height: 16px;
  }

  .arco-form .image-item,
  .input-item {
    margin-bottom: 0;
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

  :deep(.arco-form-item-wrapper-col) {
    width: 50%;
  }
</style>
