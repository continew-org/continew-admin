<template>
  <a-card :bordered="false">
    <a-space :size="54">
      <a-upload
        :file-list="avatarList"
        :show-file-list="false"
        list-type="picture-card"
        :show-upload-button="true"
        :custom-request="handleUpload"
        @change="handleAvatarChange"
      >
        <template #upload-button>
          <a-avatar :size="100" class="info-avatar">
            <template #trigger-icon><icon-camera /></template>
            <img v-if="avatarList.length" :src="avatarList[0].url" :alt="$t('userCenter.panel.avatar')" />
          </a-avatar>
        </template>
      </a-upload>

      <a-descriptions
        :column="2"
        :label-style="{
          width: '140px',
          fontWeight: 'normal',
          color: 'rgb(var(--gray-8))',
        }"
        :value-style="{
          width: '200px',
          paddingLeft: '8px',
          textAlign: 'left',
        }"
        align="right"
        layout="inline-horizontal"
      >
        <a-descriptions-item :label="$t('userCenter.panel.label.nickname')">{{ loginStore.nickname }}</a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.gender')">
          <div v-if="loginStore.gender === 1">
            {{ $t('userCenter.panel.male') }}
            <icon-man style="color: #19BBF1" />
          </div>
          <div v-else-if="loginStore.gender === 2">
            {{ $t('userCenter.panel.female') }}
            <icon-woman style="color: #FA7FA9" />
          </div>
          <div v-else>{{ $t('userCenter.panel.unknown') }}</div>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.phone')">{{ loginStore.phone }}</a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.email')">{{ loginStore.email }}</a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.deptName')">{{ loginStore.deptName }}</a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.registrationDate')">{{ loginStore.registrationDate }}</a-descriptions-item>
      </a-descriptions>
    </a-space>
  </a-card>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref } from 'vue';
  import { FileItem, RequestOption } from '@arco-design/web-vue';
  import { uploadAvatar } from '@/api/system/user-center';
  import { useLoginStore } from '@/store';
  import getAvatar from '@/utils/avatar';

  const { proxy } = getCurrentInstance() as any;

  const loginStore = useLoginStore();
  const avatar = {
    uid: '-2',
    name: 'avatar.png',
    url: getAvatar(loginStore),
  };
  const avatarList = ref<FileItem[]>([avatar]);

  /**
   * 上传头像
   *
   * @param options 选项
   */
  const handleUpload = (options: RequestOption) => {
    const controller = new AbortController();
    (async function requestWrap() {
      const {
        onProgress,
        onError,
        onSuccess,
        fileItem,
        name = 'avatarFile',
      } = options;
      onProgress(20);
      const formData = new FormData();
      formData.append(name as string, fileItem.file as Blob);
      uploadAvatar(formData).then((res) => {
        onSuccess(res);
        loginStore.avatar = res.data.avatar;
        proxy.$message.success(res.msg);
      }).catch((error) => {
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
   * 切换头像
   *
   * @param fileItemList 文件列表
   * @param currentFile 当前文件
   */
  const handleAvatarChange = (fileItemList: FileItem[], currentFile: FileItem) => {
    avatarList.value = [currentFile];
  };
</script>

<style scoped lang="less">
  .arco-card {
    padding: 14px 0 4px 4px;
    border-radius: 4px;
  }

  :deep(.arco-avatar-trigger-icon-button) {
    width: 32px;
    height: 32px;
    line-height: 32px;
    background-color: #e8f3ff;
    .arco-icon-camera {
      margin-top: 8px;
      color: rgb(var(--arcoblue-6));
      font-size: 14px;
    }
  }
</style>
