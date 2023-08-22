<template>
  <a-card
    class="general-card"
    :title="$t('workplace.announcement')"
    :header-style="{ paddingBottom: '0' }"
    :body-style="{ padding: '15px 20px 13px 20px' }"
  >
    <template #extra>
      <a-link href="/system/announcement">{{
        $t('workplace.viewMore')
      }}</a-link>
    </template>
    <div>
      <div v-for="(item, idx) in list" :key="idx" class="item">
        <a-tag v-if="item.type === 1" color="orangered">活动</a-tag>
        <a-tag v-else-if="item.type === 2" color="cyan">消息</a-tag>
        <a-tag v-else color="blue">通知</a-tag>
        <span class="item-content">
          <a-link @click="toDetail(item.id)">{{ item.title }}</a-link>
        </span>
      </div>
    </div>

    <!-- 详情区域 -->
    <a-modal
      :visible="detailVisible"
      modal-class="detail"
      width="70%"
      :footer="false"
      unmount-on-close
      render-to-body
      @cancel="handleDetailCancel"
    >
      <a-spin
        :loading="detailLoading"
        tip="公告正在赶来..."
        style="width: 100%"
      >
        <template #icon>
          <icon-sync />
        </template>
        <a-typography :style="{ marginTop: '-40px', textAlign: 'center' }">
          <a-typography-title>
            {{ detail.title }}
          </a-typography-title>
          <a-typography-paragraph>
            <div class="meta-data">
              <a-space>
                <span>
                  <icon-user class="icon" />
                  <span class="label">发布人：</span>
                  <span>{{ detail.createUserString }}</span>
                </span>
                <a-divider direction="vertical" />
                <span>
                  <svg-icon icon-class="clock-circle" class="icon" />
                  <span class="label">发布时间：</span>
                  <span>{{
                    detail.effectiveTime
                      ? detail.effectiveTime
                      : detail.createTime
                  }}</span>
                </span>
              </a-space>
            </div>
          </a-typography-paragraph>
        </a-typography>
        <a-divider />
        <v-md-preview :text="detail.content"></v-md-preview>
        <a-divider />
        <div v-if="detail.updateTime" class="update-time-row">
          <span>
            <icon-schedule class="icon" />
            <span>最后更新于：</span>
            <span>{{ detail.updateTime }}</span>
          </span>
        </div>
      </a-spin>
    </a-modal>
  </a-card>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import {
    AnnouncementDashboardRecord,
    listAnnouncement,
  } from '@/api/dashboard';
  import { DataRecord, get } from '@/api/system/announcement';

  const list = ref<AnnouncementDashboardRecord[]>([]);
  const detail = ref<DataRecord>({});
  const detailLoading = ref(false);
  const detailVisible = ref(false);

  /**
   * 查询公告列表
   */
  const getList = () => {
    listAnnouncement().then((res) => {
      list.value = res.data;
    });
  };
  getList();

  /**
   * 查看详情
   *
   * @param id ID
   */
  const toDetail = async (id: string) => {
    if (detailLoading.value) return;
    detailLoading.value = true;
    detailVisible.value = true;
    get(id)
      .then((res) => {
        detail.value = res.data;
      })
      .finally(() => {
        detailLoading.value = false;
      });
  };

  /**
   * 关闭详情
   */
  const handleDetailCancel = () => {
    detailVisible.value = false;
    detail.value = {};
  };
</script>

<style scoped lang="less">
  .item {
    display: flex;
    align-items: center;
    width: 100%;
    height: 24px;
    margin-bottom: 4px;
    .arco-link {
      color: rgb(var(--gray-8));
    }
    .item-content {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-left: 4px;
      color: var(--color-text-2);
      text-decoration: none;
      font-size: 13px;
      cursor: pointer;
    }
  }

  .meta-data {
    font-size: 15px;
    text-align: center;
  }

  .icon {
    margin-right: 3px;
  }

  .update-time-row {
    text-align: right;
  }
</style>
