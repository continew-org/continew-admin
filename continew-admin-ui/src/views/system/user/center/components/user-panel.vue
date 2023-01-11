<template>
  <a-card :bordered="false">
    <a-space :size="54">
      <a-upload
        :custom-request="handleUpload"
        list-type="picture-card"
        :file-list="avatarList"
        :show-upload-button="true"
        :show-file-list="false"
        @change="changeAvatar"
      >
        <template #upload-button>
          <a-avatar :size="100" class="info-avatar">
            <template #trigger-icon>
              <icon-camera />
            </template>
            <img v-if="avatarList.length" :src="avatarList[0].url"  :alt="$t('userCenter.panel.avatar')"/>
          </a-avatar>
        </template>
      </a-upload>
      <a-descriptions
        :column="2"
        align="right"
        layout="inline-horizontal"
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
        <a-descriptions-item :label="$t('userCenter.panel.label.registrationDate')">{{ loginStore.registrationDate }}</a-descriptions-item>
      </a-descriptions>
    </a-space>
  </a-card>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import type {
    FileItem,
    RequestOption,
  } from '@arco-design/web-vue/es/upload/interfaces';
  import { useLoginStore } from '@/store';
  import { uploadAvatar } from '@/api/system/user-center';
  import getAvatar from "@/utils/avatar";
  import { Message } from "@arco-design/web-vue";

  const loginStore = useLoginStore();
  const avatar = {
    uid: '-2',
    name: 'avatar.png',
    url: getAvatar(loginStore),
  };
  const avatarList = ref<FileItem[]>([avatar]);

  // 切换头像
  const changeAvatar = (fileItemList: FileItem[], currentFile: FileItem) => {
    avatarList.value = [currentFile];
  };

  // 上传头像
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
      try {
        const res = await uploadAvatar(formData);
        onSuccess(res);
        if (res.success) Message.success(res.msg);
        // 更换头像
        loginStore.avatar = res.data.avatar;
      } catch (error) {
        onError(error);
      }
    })();
    return {
      abort() {
        controller.abort();
      },
    };
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
