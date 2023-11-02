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

      <div class="main">
        <a-modal
          :visible="cropperVisible"
          width="40%"
          :footer="false"
          unmount-on-close
          render-to-body
          @cancel="handleCropperCancel"
        >
          <a-row>
            <a-col :span="14">
              <div style="width: 370px; height: 370px">
                <!-- 头像裁剪框 -->
                <vue-cropper
                  ref="cropper"
                  :info="true"
                  :img="options.img"
                  :full="options.full"
                  :fixed="options.fixed"
                  :fixed-box="options.fixedBox"
                  :can-move="options.canMove"
                  :center-box="options.centerBox"
                  :auto-crop="options.autoCrop"
                  :auto-crop-width="options.autoCropWidth"
                  :auto-crop-height="options.autoCropHeight"
                  :output-type="options.outputType"
                  :output-size="options.outputSize"
                  @realTime="realTime"
                />
              </div>
            </a-col>
            <a-col :span="6">
              <!-- 实时预览 -->
              <div :style="previewStyle">
                <div :style="previews.div">
                  <img :src="previews.url" :style="previews.img" alt="" />
                </div>
              </div>
            </a-col>
          </a-row>
          <br />
          <a-space>
            <a-button type="primary" @click="handleUpload">提交</a-button>
            <a-button type="outline" @click="handleCropperCancel"
              >取消</a-button
            >
          </a-space>
        </a-modal>
      </div>

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
  import getAvatar from '@/utils/avatar';
  import { useUserStore } from '@/store';
  import { VueCropper } from 'vue-cropper';
  import 'vue-cropper/dist/index.css';

  const fileRef = ref(reactive({ name: 'avatar.png' }));
  const previews: any = ref({});
  const previewStyle: any = ref({});
  const cropperVisible = ref(false);
  const cropper = ref();
  const { proxy } = getCurrentInstance() as any;
  const userStore = useUserStore();

  const avatar = {
    uid: '-2',
    name: 'avatar.png',
    url: getAvatar(userStore.avatar, userStore.gender),
  };
  const avatarList = ref<FileItem[]>([avatar]);

  const options: cropperOptions = reactive({
    autoCrop: true,
    autoCropWidth: 200,
    autoCropHeight: 200,
    canMove: true,
    centerBox: true,
    full: false,
    fixed: false,
    fixedBox: false,
    img: '',
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
    cropperVisible.value = true;
    return false;
  };

  /**
   * 关闭裁剪框
   */
  const handleCropperCancel = () => {
    fileRef.value = { name: '' };
    options.img = '';
    cropperVisible.value = false;
  };

  /**
   * 上传头像
   */
  const handleUpload = () => {
    cropper.value.getCropBlob((data: string | Blob) => {
      const formData = new FormData();
      formData.append('avatarFile', data, fileRef.value?.name);
      uploadAvatar(formData).then((res) => {
        userStore.avatar = res.data.avatar;
        avatarList.value[0].url = getAvatar(res.data.avatar, undefined);
        proxy.$message.success(res.msg);
        handleCropperCancel();
      });
    });
  };

  /**
   * 实时预览
   * @param data data
   */
  const realTime = (data: any) => {
    previewStyle.value = {
      width: `${data.w}px`,
      height: `${data.h}px`,
      overflow: 'hidden',
      margin: '0',
      zoom: 0.8,
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

  .main {
    position: relative;
  }
</style>
