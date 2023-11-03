<template>
  <a-list :bordered="false">
    <a-list-item
      v-for="item in renderList"
      :key="item.id"
      action-layout="vertical"
      :style="{
        opacity: item.isRead ? 0.5 : 1,
      }"
    >
      <div class="item-wrap" @click="onItemClick(item)">
        <a-list-item-meta>
          <template #title>
            <a-space :size="4">
              <span>{{ item.title }}</span>
            </a-space>
          </template>
          <template #description>
            <div>
              <a-typography-paragraph
                :ellipsis="{
                  rows: 1,
                }"
                >{{ item.content }}</a-typography-paragraph
              >
              <a-typography-text class="time-text">
                {{ item.createTime }}
              </a-typography-text>
            </div>
          </template>
        </a-list-item-meta>
      </div>
    </a-list-item>
    <template #footer>
      <a-space
        fill
        :size="0"
        :class="{ 'add-border-top': renderList.length < showMax }"
      >
        <div class="footer-wrap">
          <a-link @click="handleReadAll">{{ $t('messageBox.allRead') }}</a-link>
        </div>
        <div class="footer-wrap">
          <a-link @click="handleViewMore">{{
            $t('messageBox.viewMore')
          }}</a-link>
        </div>
      </a-space>
    </template>
    <div
      v-if="renderList.length && renderList.length < 3"
      :style="{ height: (showMax - renderList.length) * 86 + 'px' }"
    ></div>
  </a-list>
</template>

<script lang="ts" setup>
  import { PropType } from 'vue';
  import { useRouter } from 'vue-router';
  import { DataRecord } from '@/api/system/message';

  const router = useRouter();
  const props = defineProps({
    renderList: {
      type: Array as PropType<DataRecord[]>,
      required: true,
    },
  });
  const emit = defineEmits(['itemClick']);

  /**
   * 全部已读
   */
  const handleReadAll = () => {
    emit('itemClick', [...props.renderList]);
  };

  /**
   * 查看更多
   */
  const handleViewMore = () => {
    router.push({
      name: 'Message',
    });
  };

  /**
   * 点击消息
   *
   * @param item 消息
   */
  const onItemClick = (item: DataRecord) => {
    if (!item.isRead) {
      emit('itemClick', [item]);
    }
  };
  const showMax = 3;
</script>

<style scoped lang="less">
  :deep(.arco-list) {
    .arco-list-item {
      min-height: 86px;
      border-bottom: 1px solid rgb(var(--gray-3));
    }
    .arco-list-item-extra {
      position: absolute;
      right: 20px;
    }
    .arco-list-item-meta-content {
      flex: 1;
    }
    .item-wrap {
      cursor: pointer;
    }
    .time-text {
      font-size: 12px;
      color: rgb(var(--gray-6));
    }
    .arco-empty {
      display: none;
    }
    .arco-list-footer {
      padding: 0;
      height: 50px;
      line-height: 50px;
      border-top: none;
      .arco-space-item {
        width: 100%;
        border-right: 1px solid rgb(var(--gray-3));
        &:last-child {
          border-right: none;
        }
      }
      .add-border-top {
        border-top: 1px solid rgb(var(--gray-3));
      }
    }
    .footer-wrap {
      text-align: center;
      .arco-link {
        margin-left: 10px;
      }
    }
    .arco-typography {
      margin-bottom: 0;
    }
    .add-border {
      border-top: 1px solid rgb(var(--gray-3));
    }
  }
</style>
