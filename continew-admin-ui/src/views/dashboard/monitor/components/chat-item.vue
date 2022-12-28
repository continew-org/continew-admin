<template>
  <div :class="['chat-item', itemData.isCollect ? 'chat-item-collected' : '']">
    <a-space :size="4" direction="vertical" fill>
      <a-typography-text type="warning">
        {{ itemData.username }}
      </a-typography-text>
      <a-typography-text>{{ itemData.content }}</a-typography-text>
      <div class="chat-item-footer">
        <div class="chat-item-time">
          <a-typography-text type="secondary">
            {{ itemData.time }}
          </a-typography-text>
        </div>
        <div class="chat-item-actions">
          <div class="chat-item-actions-item">
            <icon-command />
          </div>
          <div class="chat-item-actions-item chat-item-actions-collect">
            <icon-star />
          </div>
        </div>
      </div>
    </a-space>
  </div>
</template>

<script lang="ts" setup>
  import { PropType } from 'vue';
  import { ChatRecord } from '@/api/message';

  defineProps({
    itemData: {
      type: Object as PropType<ChatRecord>,
      default() {
        return {};
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
      .chat-item-actions-collect {
        color: rgb(var(--gold-6));
      }
    }

    &:hover {
      background-color: rgb(var(--gray-2));

      .chat-item-actions {
        opacity: 1;
      }
    }
  }
</style>
