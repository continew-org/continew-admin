<template>
  <div>
    <a-card
      :title="$t('menu.system.file.list')"
      :bordered="false"
      :body-style="{ padding: 0 }"
    >
      <a-menu :default-open-keys="['0']" :selected-keys="[selectedKey]">
        <a-sub-menu key="0">
          <template #icon>
            <icon-apps></icon-apps>
          </template>
          <template #title>文件类型</template>
          <a-menu-item
            v-for="item in fileTypeList"
            :key="item.value.toString()"
            @click="onClickItem(item)"
          >
            <template #icon>
              <svg-icon
                :icon-class="item.menuIcon"
                style="height: 28px; width: 28px"
              ></svg-icon>
            </template>
            <span>{{ item.name }}</span>
          </a-menu-item>
        </a-sub-menu>
      </a-menu>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { fileTypeList, type fileTypeListItem } from '@/constant/file';

  const route = useRoute();
  const router = useRouter();

  const selectedKey = ref('0');

  // 监听路由变化
  watch(
    () => route.query,
    () => {
      if (route.query.type) {
        selectedKey.value = route.query.type as string;
      }
    },
    {
      immediate: true,
    },
  );

  // 点击事件
  const onClickItem = (item: fileTypeListItem) => {
    router.push({ name: 'File', query: { type: item.value } });
  };
</script>

<style lang="less" scoped>
  :deep(.arco-card) {
    .arco-card-header {
      border-bottom-style: dashed;
      margin: 0 16px;
      padding-left: 0;
      padding-right: 0;
    }
  }
</style>
