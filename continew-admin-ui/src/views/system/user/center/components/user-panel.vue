<template>
  <a-card :bordered="false">
    <a-space :size="54">
      <a-upload
        :file-list="avatarList"
        accept="image/*"
        :show-file-list="false"
        list-type="picture-card"
        :show-upload-button="true"
        :on-before-upload="handleBeforeUpload"
      >
        <template #upload-button>
          <a-avatar :size="100" class="info-avatar">
            <template #trigger-icon><icon-camera /></template>
            <img
              v-if="avatarList.length"
              :src="avatarList[0].url"
              :alt="$t('userCenter.panel.avatar')"
            />
          </a-avatar>
        </template>
      </a-upload>
      <a-modal
        :title="$t('userCenter.panel.avatar.upload')"
        :visible="visible"
        :width="400"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleCancel"
      >
        <a-row>
          <a-col :span="14" style="width: 200px; height: 200px">
            <vue-cropper
              ref="cropperRef"
              :img="options.img"
              :info="true"
              :auto-crop="options.autoCrop"
              :auto-crop-width="options.autoCropWidth"
              :auto-crop-height="options.autoCropHeight"
              :fixed-box="options.fixedBox"
              :fixed="options.fixed"
              :full="options.full"
              :center-box="options.centerBox"
              :can-move="options.canMove"
              :output-type="options.outputType"
              :output-size="options.outputSize"
              @real-time="handleRealTime"
            />
          </a-col>
          <a-col :span="10" style="display: flex; justify-content: center">
            <div :style="previewStyle">
              <div :style="previews.div">
                <img :src="previews.url" :style="previews.img" alt="" />
              </div>
            </div>
          </a-col>
        </a-row>
        <div style="text-align: center; padding-top: 30px">
          <a-space>
            <a-button type="primary" @click="handleUpload">确定</a-button>
            <a-button @click="handleCancel">取消</a-button>
          </a-space>
        </div>
      </a-modal>

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
        <a-descriptions-item :label="$t('userCenter.panel.label.nickname')">{{
          userStore.nickname
        }}</a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.gender')">
          <div v-if="userStore.gender === 1">
            {{ $t('userCenter.panel.male') }}
            <icon-man style="color: #19bbf1" />
          </div>
          <div v-else-if="userStore.gender === 2">
            {{ $t('userCenter.panel.female') }}
            <icon-woman style="color: #fa7fa9" />
          </div>
          <div v-else>{{ $t('userCenter.panel.unknown') }}</div>
        </a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.phone')">{{
          userStore.phone || '暂无'
        }}</a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.email')">{{
          userStore.email || '暂无'
        }}</a-descriptions-item>
        <a-descriptions-item :label="$t('userCenter.panel.label.deptName')">{{
          userStore.deptName
        }}</a-descriptions-item>
        <a-descriptions-item
          :label="$t('userCenter.panel.label.registrationDate')"
          >{{ userStore.registrationDate }}</a-descriptions-item
        >
      </a-descriptions>
    </a-space>
  </a-card>
</template>

<script lang="ts" setup>
  import { reactive, ref, getCurrentInstance } from 'vue';
  import { FileItem } from '@arco-design/web-vue';
  import { uploadAvatar, cropperOptions } from '@/api/system/user-center';
  import { useUserStore } from '@/store';
  import getAvatar from '@/utils/avatar';
  import { VueCropper } from 'vue-cropper';
  import 'vue-cropper/dist/index.css';

  const { proxy } = getCurrentInstance() as any;
  const userStore = useUserStore();
  const cropperRef = ref();
  const visible = ref(false);
  const previews: any = ref({});
  const previewStyle: any = ref({});
  const fileRef = ref(reactive({ name: 'avatar.png' }));
  const avatar = {
    uid: '-2',
    name: 'avatar.png',
    url: getAvatar(userStore.avatar, userStore.gender),
  };
  const avatarList = ref<FileItem[]>([avatar]);

  const options: cropperOptions = reactive({
    img: '',
    autoCrop: true,
    autoCropWidth: 160,
    autoCropHeight: 160,
    fixedBox: true,
    fixed: true,
    full: false,
    centerBox: true,
    canMove: true,
    outputSize: 1,
    outputType: 'png',
  });

  /**
   * 上传前弹出裁剪框
   *
   * @param file 头像
   */
  const handleBeforeUpload = (file: File): boolean => {
    fileRef.value = file;
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      options.img = reader.result;
    };
    visible.value = true;
    return false;
  };

  /**
   * 关闭裁剪框
   */
  const handleCancel = () => {
    fileRef.value = { name: '' };
    options.img = '';
    visible.value = false;
  };

  /**
   * 上传头像
   */
  const handleUpload = () => {
    cropperRef.value.getCropBlob((data: string | Blob) => {
      const formData = new FormData();
      formData.append('avatarFile', data, fileRef.value?.name);
      uploadAvatar(formData).then((res) => {
        userStore.avatar = res.data.avatar;
        avatarList.value[0].url = getAvatar(res.data.avatar, undefined);
        proxy.$message.success(res.msg);
        handleCancel();
      });
    });
  };

  /**
   * 实时预览
   *
   * @param data data 预览图像
   */
  const handleRealTime = (data: any) => {
    previewStyle.value = {
      width: `${data.w}px`,
      height: `${data.h}px`,
      overflow: 'hidden',
      margin: '0',
      zoom: 100 / data.h,
      borderRadius: '50%',
    };
    previews.value = data;
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
