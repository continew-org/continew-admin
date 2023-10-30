<template>
  <a-spin style="display: block" :loading="loading">
    <a-tabs v-model:activeKey="messageType" type="rounded" destroy-on-hide>
      <a-tab-pane v-for="item in message_type" :key="item.value">
        <template #title>
          <span> {{ item.label }}{{ formatUnreadLength(item.value) }} </span>
        </template>
        <a-result v-if="!renderList.length" status="404">
          <template #subtitle> {{ $t('messageBox.noContent') }} </template>
        </a-result>
        <List
          :render-list="renderList"
          :unread-count="unreadCount"
          @item-click="handleItemClick"
        />
      </a-tab-pane>
    </a-tabs>
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, getCurrentInstance, reactive, ref, toRefs } from 'vue';
  import {
    MessageListType,
    MessageRecord,
    list,
    read,
  } from '@/api/system/message';
  import useLoading from '@/hooks/loading';
  import List from './list.vue';

  const { proxy } = getCurrentInstance() as any;
  const { message_type } = proxy.useDict('message_type');
  const { loading, setLoading } = useLoading(true);
  const messageType = ref('1');

  const messageData = reactive<{
    renderList: MessageRecord[];
    messageList: MessageRecord[];
  }>({
    renderList: [],
    messageList: [],
  });
  toRefs(messageData);

  /**
   * 查询列表
   */
  async function fetchSourceData() {
    setLoading(true);
    try {
      list({ sort: ['createTime,desc'] }).then((res) => {
        messageData.messageList = res.data;
      });
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  }

  /**
   * 将消息设置为已读
   *
   * @param data 消息列表
   */
  async function readMessage(data: MessageListType) {
    const ids = data.map((item) => item.id);
    await read(ids);
    fetchSourceData();
  }

  /**
   * 每个消息类型下的消息列表
   */
  const renderList = computed(() => {
    return messageData.messageList.filter(
      (item) => item.type === messageType.value && !item.readStatus
    ).splice(0,3);
  });

  /**
   * 未读消息数量
   */
  const unreadCount = computed(() => {
    return renderList.value.filter((item) => !item.readStatus).length;
  });

  /**
   * 未读消息列表
   *
   * @param type 消息类型
   */
  const getUnreadList = (type: string) => {
    return messageData.messageList.filter(
      (item) => item.type === type && !item.readStatus
    );
  };

  /**
   * 每个类型的未读消息数量
   *
   * @param type 消息类型
   */
  const formatUnreadLength = (type: string) => {
    const unreadList = getUnreadList(type);
    return unreadList.length ? `(${unreadList.length})` : ``;
  };

  /**
   * 点击消息事件
   *
   * @param items 消息
   */
  const handleItemClick = (items: MessageListType) => {
    if (renderList.value.length) readMessage([...items]);
  };

  /**
   * 清空消息
   */
  const emptyList = () => {
    read([]).then((res) => {
      messageData.messageList = [];
    });
  };
  fetchSourceData();
</script>

<style scoped lang="less">
  :deep(.arco-popover-popup-content) {
    padding: 0;
  }

  :deep(.arco-list-item-meta) {
    align-items: flex-start;
  }
  :deep(.arco-tabs-nav) {
    padding: 14px 0 12px 16px;
    border-bottom: 1px solid var(--color-neutral-3);
  }
  :deep(.arco-tabs-content) {
    padding-top: 0;
    .arco-result-subtitle {
      color: rgb(var(--gray-6));
    }
  }
</style>
