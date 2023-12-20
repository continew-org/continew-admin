<template>
  <div class="file-detail">
    <a-row :gutter="[14, 14]" align="stretch" class="wrap">
      <a-col :xs="24" :sm="24" :md="16" :lg="18" :xl="19" :xxl="19">
        <div class="left">
          <a-row justify="space-between">
            <a-button @click="back"><icon-left /></a-button>
          </a-row>
          <div class="view-box">
<!--            <PreImage></PreImage>-->
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="24" :md="8" :lg="6" :xl="5" :xxl="5">
        <div class="right">
          <a-descriptions
            title="文件详情"
            :column="1"
            size="mini"
            table-layout="fixed"
            layout="inline-horizontal"
          >
            <a-descriptions-item label="名称：">头像01</a-descriptions-item>
            <a-descriptions-item label="类型：">jpg</a-descriptions-item>
            <a-descriptions-item label="路径：">/</a-descriptions-item>
            <a-descriptions-item label="文件大小：">256K</a-descriptions-item>
            <a-descriptions-item label="创建人：">admin</a-descriptions-item>
            <a-descriptions-item label="创建时间："
              >2022-05-18 15:25:08</a-descriptions-item
            >
            <a-descriptions-item label="文件ID："
              >1511952522629615617</a-descriptions-item
            >
            <a-descriptions-item label="标签：">
              <a-space wrap>
                <a-tag size="small" color="red">头像</a-tag>
                <a-tag size="small" color="orangered">图片</a-tag>
                <a-tag size="small" color="purple">JPG</a-tag>
                <a-tag size="small" color="blue">壁纸</a-tag>
                <a-tag size="small" color="orange">4K</a-tag>
                <a-tag size="small" color="green">风景</a-tag>
              </a-space>
            </a-descriptions-item>
            <a-descriptions-item label="说明："
              >基于 v-viewer vue3
              实现自定义按钮的图片预览，其他功能可自行扩展</a-descriptions-item
            >
          </a-descriptions>

          <a-row justify="end" style="margin-top: 30px">
            <a-space>
              <a-button>
                <template #icon><icon-download :size="16" /></template>
              </a-button>
              <a-button>
                <template #icon><icon-share-alt :size="16" /></template>
              </a-button>
              <a-button>
                <template #icon><icon-drag-arrow :size="16" /></template>
              </a-button>
              <a-button>
                <template #icon><icon-delete :size="16" /></template>
              </a-button>
            </a-space>
          </a-row>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router';
  // import PreImage from './PreImage.vue';

  defineOptions({ name: 'FileDetail' });
  const router = useRouter();

  const back = () => {
    router.back();
  };
</script>

<style lang="less" scoped>
  :deep(.arco-descriptions) {
    .arco-descriptions-title {
      margin-bottom: 12px;
    }
    .arco-descriptions-item {
      display: flex;
      margin-bottom: 8px;
      .arco-descriptions-item-label-inline {
        white-space: nowrap;
        font-size: 12px;
      }
      .arco-descriptions-item-value-inline {
        font-size: 12px;
      }
    }
  }
  .file-detail {
    flex: 1;
    padding: 16px;
    padding-bottom: 0;
    box-sizing: border-box;
    overflow: hidden;
    overflow-y: auto;
    .wrap {
      height: 100%;
    }
    .left,
    .right {
      background-color: var(--color-bg-1);
      padding: 16px;
      box-sizing: border-box;
      overflow: hidden;
      box-sizing: border-box;
    }
    .left {
      height: 100%;
      display: flex;
      flex-direction: column;
      .view-box {
        min-height: 400px;
        flex: 1;
        overflow: hidden;
        margin-top: $margin;
      }
    }

    .right {
      // width: 300px;
      // height: fit-content;
      // margin: $margin;
      // margin-left: 0;
    }
  }
</style>
