<template>
  <div class="chat-list">
    <ChatItem v-for="item in renderList" :key="item.id" :item-data="item" />
    <a-result v-if="!renderList.length" status="404" />
  </div>
</template>

<script lang="ts" setup>
  import { PropType } from 'vue';
  import { ChatRecord } from '@/api/message';
  import ChatItem from './chat-item.vue';

  defineProps({
    renderList: {
      type: Array as PropType<ChatRecord[]>,
      default() {
        return [];
      },
    },
  });
</script>

<style scoped lang="less">
  .chat-item {
    padding: 8px;
    font-size: 12px;
    line-height: 20px;
    border-radius: 2px;

    &-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    &-actions {
      display: flex;
      opacity: 0;

      &-item {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 20px;
        height: 20px;
        margin-right: 4px;
        color: var(--color-text-3);
        font-size: 14px;
        border-radius: 50%;
        cursor: pointer;

        &:hover {
          background-color: rgb(var(--gray-3));
        }

        &:last-child {
          margin-right: 0;
        }
      }
    }

    &-collected {
      .message-item-actions-collect {
        color: rgb(var(--gold-6));
      }
    }

    &:hover {
      background-color: rgb(var(--gray-2));

      .message-item-actions {
        opacity: 1;
      }
    }
  }
</style>
