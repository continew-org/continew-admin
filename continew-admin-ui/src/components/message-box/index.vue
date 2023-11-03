<template>
  <a-spin style="display: block" :loading="loading">
    <a-tabs v-model:activeKey="messageType" type="rounded" destroy-on-hide>
      <a-tab-pane :key="1">
        <template #title>
          <span>
            {{ $t('messageBox.tab.title.message.system')
            }}{{ formatUnreadCount(messageType) }}
          </span>
        </template>
        <a-result v-if="!messageList.length" status="404">
          <template #subtitle> {{ $t('messageBox.noContent') }} </template>
        </a-result>
        <List :render-list="messageList" @item-click="handleItemClick" />
      </a-tab-pane>
    </a-tabs>
  </a-spin>
</template>

<script lang="ts" setup>
  import { reactive, ref, toRefs } from 'vue';
  import {
    DataRecord,
    MessageUnreadRes,
    list,
    read,
    countUnread,
    ListParam,
  } from '@/api/system/message';
  import useLoading from '@/hooks/loading';
  import List from './list.vue';

  const { loading, setLoading } = useLoading();
  const messageType = ref(1);
  const unreadCount = ref<MessageUnreadRes>();
  const messageList = ref<DataRecord[]>([]);
  const data = reactive({
    // 查询参数
    queryParams: {
      type: messageType.value,
      isRead: false,
      page: 1,
      size: 3,
      sort: ['createTime,desc'],
    },
  });
  const { queryParams } = toRefs(data);

  /**
   * 查询未读消息数量
   */
  async function getUnreadCount() {
    const res = await countUnread(true);
    unreadCount.value = res.data;
  }

  /**
   * 查询未读消息列表
   */
  async function getList(params: ListParam = { ...queryParams.value }) {
    await getUnreadCount();
    setLoading(true);
    try {
      await list(params).then((res) => {
        messageList.value = res.data.list;
      });
    } finally {
      setLoading(false);
    }
  }

  /**
   * 将消息设置为已读
   *
   * @param items 消息列表
   */
  async function readMessage(items: DataRecord[]) {
    const ids = items.map((item) => item.id);
    await read(ids);
    await getList();
    await getUnreadCount();
  }

  /**
   * 每个类型的未读消息数量
   *
   * @param type 消息类型
   */
  const formatUnreadCount = (type: number) => {
    const count = unreadCount.value?.details.find(
      (item) => item.type === type
    )?.count;
    return count && count !== 0 ? `(${count})` : '';
  };

  /**
   * 点击消息事件
   *
   * @param items 消息
   */
  const handleItemClick = (items: DataRecord[]) => {
    if (messageList.value.length) readMessage([...items]);
  };
  getList();
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
