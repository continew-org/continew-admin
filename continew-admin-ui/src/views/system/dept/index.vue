<template>
  <div class="container">
    <Breadcrumb :items="['menu.system', 'menu.system.dept.list']" />
    <a-card class="general-card">
      <a-row :gutter="20">
        <a-col>
          <!-- 工具栏 -->
          <div class="head-container">
            <a-form ref="queryFormRef" :model="queryFormData" layout="inline">
              <a-form-item field="deptName" hide-label>
                <a-input
                  v-model="queryFormData.deptName"
                  placeholder="输入部门名称搜索"
                  allow-clear
                  style="width: 150px;"
                  @press-enter="toQuery"
                />
              </a-form-item>
              <a-form-item field="status" hide-label>
                <a-select
                  v-model="queryFormData.status"
                  :options="statusOptions"
                  placeholder="状态搜索"
                  allow-clear
                  style="width: 150px;"
                />
              </a-form-item>
              <a-button type="primary" @click="toQuery">
                <template #icon>
                  <icon-search />
                </template>
                查询
              </a-button>
              <a-button @click="resetQuery">
                <template #icon>
                  <icon-refresh />
                </template>
                重置
              </a-button>
            </a-form>
          </div>

          <!-- 表格渲染 -->
          <a-table
            :columns="columns"
            :data="renderData"
            :pagination="false"
            :default-expand-all-rows="true"
            :hide-expand-button-on-empty="true"
            ref="tableRef"
            row-key="deptId"
            :bordered="false"
            :stripe="true"
            :loading="loading"
            size="large"
          >
            <template #status="{ record }">
              <a-switch v-model="record.status" :checked-value="1" :unchecked-value="2" />
            </template>
          </a-table>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { Message, TableInstance } from '@arco-design/web-vue';
  import { getDeptList, DeptRecord, DeptParams } from '@/api/system/dept';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { SelectOptionData } from '@arco-design/web-vue/es/select/interface';

  const { loading, setLoading } = useLoading(true);
  const tableRef = ref<TableInstance>();
  const queryFormRef = ref<FormInstance>();
  const queryFormData = ref({
    deptName: '',
    status: undefined,
  });
  const statusOptions = computed<SelectOptionData[]>(() => [
    {
      label: '启用',
      value: 1,
    },
    {
      label: '禁用',
      value: 2,
    },
  ]);

  // 查询
  const toQuery = () => {
    fetchData({
      ...queryFormData.value,
    } as unknown as DeptParams);
  };

  // 重置
  const resetQuery = async () => {
    await queryFormRef.value?.resetFields();
    await fetchData();
  };

  const renderData = ref<DeptRecord[]>([]);
  const columns = computed<TableColumnData[]>(() => [
    {
      title: '部门名称',
      dataIndex: 'deptName',
    },
    {
      title: '部门排序',
      dataIndex: 'deptSort',
      align: 'center',
    },
    {
      title: '状态',
      dataIndex: 'status',
      slotName: 'status',
      align: 'center',
    },
    {
      title: '描述',
      dataIndex: 'description',
    },
    {
      title: '修改人',
      dataIndex: 'updateUserString',
    },
    {
      title: '修改时间',
      dataIndex: 'updateTime',
    },
  ]);

  // 分页查询列表
  const fetchData = async (
    params: DeptParams = { page: 1, size: 10, sort: ['parentId,asc', 'deptSort,asc', 'createTime,desc'] }
  ) => {
    setLoading(true);
    try {
      const { data } = await getDeptList(params);
      renderData.value = data;
    } finally {
      setLoading(false);
    }
    setTimeout(function() {
      tableRef.value?.expandAll();
    }, 0);
  };
  fetchData();
</script>

<script lang="ts">
  export default {
    name: 'Dept',
  };
</script>

<style scoped lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }

  .general-card {
    padding-top: 25px;
  }

  .head-container {
    margin-bottom: 15px;
  }

  :deep(.arco-table-th) {
    &:last-child {
      .arco-table-th-item-title {
        margin-left: 16px;
      }
    }
  }
</style>
