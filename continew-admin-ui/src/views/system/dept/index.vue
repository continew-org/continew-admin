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
                  style="width: 150px"
                  @press-enter="toQuery"
                />
              </a-form-item>
              <a-form-item field="status" hide-label>
                <a-select
                  v-model="queryFormData.status"
                  :options="statusOptions"
                  placeholder="状态搜索"
                  allow-clear
                  style="width: 150px"
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

          <!-- 工具栏 -->
          <a-row style="margin-bottom: 16px">
            <a-col :span="12">
              <a-space>
                <a-button type="primary" @click="toCreate">
                  <template #icon>
                    <icon-plus />
                  </template>
                  {{ $t('searchTable.operation.create') }}
                </a-button>
                <a-button
                  type="primary"
                  status="success"
                  disabled
                  title="尚未开发"
                >
                  <template #icon>
                    <icon-edit />
                  </template>
                  {{ $t('searchTable.operation.update') }}
                </a-button>
                <a-button
                  type="primary"
                  status="danger"
                  disabled
                  title="尚未开发"
                >
                  <template #icon>
                    <icon-delete />
                  </template>
                  {{ $t('searchTable.operation.delete') }}
                </a-button>
              </a-space>
            </a-col>
            <a-col
              :span="12"
              style="display: flex; align-items: center; justify-content: end"
            >
              <a-button
                type="primary"
                status="warning"
                disabled
                title="尚未开发"
              >
                <template #icon>
                  <icon-download />
                </template>
                {{ $t('searchTable.operation.export') }}
              </a-button>
              <a-tooltip :content="$t('searchTable.actions.refresh')">
                <div class="action-icon" @click="toQuery">
                  <icon-refresh size="18" />
                </div>
              </a-tooltip>
              <a-dropdown @select="handleSelectDensity">
                <a-tooltip :content="$t('searchTable.actions.density')">
                  <div class="action-icon"><icon-line-height size="18" /></div>
                </a-tooltip>
                <template #content>
                  <a-doption
                    v-for="item in densityList"
                    :key="item.value"
                    :value="item.value"
                    :class="{ active: item.value === size }"
                  >
                    <span>{{ item.name }}</span>
                  </a-doption>
                </template>
              </a-dropdown>
              <a-tooltip :content="$t('searchTable.actions.columnSetting')">
                <a-popover
                  trigger="click"
                  position="bl"
                  @popup-visible-change="popupVisibleChange"
                >
                  <div class="action-icon"><icon-settings size="18" /></div>
                  <template #content>
                    <div id="tableSetting">
                      <div
                        v-for="(item, index) in showColumns"
                        :key="item.dataIndex"
                        class="setting"
                      >
                        <div style="margin-right: 4px; cursor: move">
                          <icon-drag-arrow />
                        </div>
                        <div>
                          <a-checkbox
                            v-model="item.checked"
                            @change="handleChange($event, item as TableColumnData, index)"
                          >
                          </a-checkbox>
                        </div>
                        <div class="title">
                          {{ item.title === '#' ? '序列号' : item.title }}
                        </div>
                      </div>
                    </div>
                  </template>
                </a-popover>
              </a-tooltip>
            </a-col>
          </a-row>

          <!-- 表格渲染 -->
          <a-table
            ref="tableRef"
            :columns="cloneColumns as TableColumnData[]"
            :data="renderData"
            :pagination="false"
            :default-expand-all-rows="true"
            :hide-expand-button-on-empty="true"
            row-key="deptId"
            :bordered="false"
            :stripe="true"
            :loading="loading"
            :size="size"
          >
            <template #status="{ record }">
              <a-switch
                v-model="record.status"
                :checked-value="1"
                :unchecked-value="2"
                @change="handleChangeStatus(record, record.status)"
              />
            </template>
            <template #operations="{ record }">
              <a-button
                v-permission="['admin']"
                type="text"
                size="small"
                disabled
                title="尚未开发"
              >
                <template #icon>
                  <icon-edit />
                </template>
                修改
              </a-button>
              <a-popconfirm content="确定删除吗，如果存在下级部门则一并删除，此操作不能撤销！" type="error" @ok="handleDelete(record)">
                <a-button
                  v-permission="['admin']"
                  type="text"
                  size="small"
                >
                  <template #icon>
                    <icon-delete />
                  </template>
                  删除
                </a-button>
              </a-popconfirm>
            </template>
          </a-table>

          <!-- 窗口 -->
          <a-modal
            title="新增部门"
            :width="570"
            :visible="visible"
            :mask-closable="false"
            unmount-on-close
            @ok="handleOk"
            @cancel="handleCancel"
          >
            <a-form ref="formRef" :model="formData" :rules="rules">
              <a-form-item
                field="parentId"
                :validate-trigger="['change', 'blur']"
                label="上级部门"
              >
                <a-tree-select
                  v-model="formData.parentId"
                  :data="treeData"
                  :allow-search="true"
                  :allow-clear="true"
                  :filter-tree-node="filterDept"
                  :fallback-option="false"
                  placeholder="请选择上级部门"
                />
              </a-form-item>
              <a-form-item
                field="deptName"
                :validate-trigger="['change', 'blur']"
                label="部门名称"
              >
                <a-input
                  v-model="formData.deptName"
                  placeholder="请输入部门名称"
                  size="large"
                  allow-clear
                >
                </a-input>
              </a-form-item>
              <a-form-item
                field="deptSort"
                :validate-trigger="['change', 'blur']"
                label="部门排序"
              >
                <a-input-number
                  v-model="formData.deptSort"
                  :min="1"
                  placeholder="请输入部门排序"
                  mode="button"
                  size="large"
                >
                </a-input-number>
              </a-form-item>
              <a-form-item
                field="description"
                :validate-trigger="['change', 'blur']"
                label="描述"
              >
                <a-textarea
                  v-model="formData.description"
                  placeholder="请输入描述"
                  size="large"
                  :max-length="200"
                  show-word-limit
                  :auto-size="{
                    minRows:3,
                  }"
                >
                </a-textarea >
              </a-form-item>
            </a-form>
          </a-modal>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, nextTick, ref, watch } from 'vue';
  import useLoading from '@/hooks/loading';
  import { FieldRule, Message, TableInstance, TreeNodeData } from '@arco-design/web-vue';
  import {
    getDeptList,
    DeptRecord,
    DeptParams,
    createDept,
    updateDeptStatus,
    deleteDept,
  } from '@/api/system/dept';
  import getDeptTree from '@/api/common';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { FormInstance } from '@arco-design/web-vue/es/form';
  import { SelectOptionData } from '@arco-design/web-vue/es/select/interface';
  import cloneDeep from 'lodash/cloneDeep';
  import Sortable from 'sortablejs';
  import { useI18n } from 'vue-i18n';

  type SizeProps = 'mini' | 'small' | 'medium' | 'large';
  type Column = TableColumnData & { checked?: true };
  const cloneColumns = ref<Column[]>([]);
  const showColumns = ref<Column[]>([]);
  const size = ref<SizeProps>('large');
  const { t } = useI18n();
  const densityList = computed(() => [
    {
      name: t('searchTable.size.mini'),
      value: 'mini',
    },
    {
      name: t('searchTable.size.small'),
      value: 'small',
    },
    {
      name: t('searchTable.size.medium'),
      value: 'medium',
    },
    {
      name: t('searchTable.size.large'),
      value: 'large',
    },
  ]);
  const { loading, setLoading } = useLoading(true);
  const tableRef = ref<TableInstance>();
  const queryFormRef = ref<FormInstance>();
  const queryFormData = ref({
    deptName: undefined,
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
    {
      title: '操作',
      slotName: 'operations',
      align: 'center',
    },
  ]);

  // 分页查询列表
  const fetchData = async (
    params: DeptParams = {
      ...queryFormData.value
    }
  ) => {
    setLoading(true);
    try {
      const { data } = await getDeptList(params);
      renderData.value = data;
    } finally {
      setLoading(false);
    }
    setTimeout(() => {
      tableRef.value?.expandAll();
    }, 0);
  };
  fetchData();

  // 改变状态
  const handleChangeStatus = async (record: DeptRecord, val: number) => {
    if (record.deptId) {
      const res = await updateDeptStatus([record.deptId], { status: val });
      if (res.success) {
        Message.success(res.msg);
      } else {
        record.status = (record.status === 1) ? 2 : 1;
      }
    }
  };

  // 删除
  const handleDelete = async (record: DeptRecord) => {
    if (record.deptId) {
      const res = await deleteDept([record.deptId]);
      if (res.success) {
        Message.success(res.msg);
        await fetchData();
      }
    }
  };

  const handleSelectDensity = (
    val: string | number | Record<string, any> | undefined,
    e: Event
  ) => {
    size.value = val as SizeProps;
  };

  const handleChange = (
    checked: boolean | (string | boolean | number)[],
    column: Column,
    index: number
  ) => {
    if (!checked) {
      cloneColumns.value = showColumns.value.filter(
        (item) => item.dataIndex !== column.dataIndex
      );
    } else {
      cloneColumns.value.splice(index, 0, column);
    }
  };

  const exchangeArray = <T extends Array<any>>(
    array: T,
    beforeIdx: number,
    newIdx: number,
    isDeep = false
  ): T => {
    const newArray = isDeep ? cloneDeep(array) : array;
    if (beforeIdx > -1 && newIdx > -1) {
      // 先替换后面的，然后拿到替换的结果替换前面的
      newArray.splice(
        beforeIdx,
        1,
        newArray.splice(newIdx, 1, newArray[beforeIdx]).pop()
      );
    }
    return newArray;
  };

  const popupVisibleChange = (val: boolean) => {
    if (val) {
      nextTick(() => {
        const el = document.getElementById('tableSetting') as HTMLElement;
        const sortable = new Sortable(el, {
          onEnd(e: any) {
            const { oldIndex, newIndex } = e;
            exchangeArray(cloneColumns.value, oldIndex, newIndex);
            exchangeArray(showColumns.value, oldIndex, newIndex);
          },
        });
      });
    }
  };

  watch(
    () => columns.value,
    (val) => {
      cloneColumns.value = cloneDeep(val);
      cloneColumns.value.forEach((item, index) => {
        item.checked = true;
      });
      showColumns.value = cloneDeep(cloneColumns.value);
    },
    { deep: true, immediate: true }
  );

  const visible = ref(false);
  const type = ref('新增');
  const treeData = ref<TreeNodeData[]>();
  const formRef = ref<FormInstance>();
  const formData = ref<DeptRecord>({
    deptId: undefined,
    deptName: '',
    parentId: undefined,
    deptSort: 999,
    description: '',
    status: undefined,
    updateUserString: '',
    updateTime: '',
    children: [],
  });
  const rules = computed((): Record<string, FieldRule[]> => {
    return {
      deptName: [
        { required: true, message: '请输入部门名称' }
      ],
      deptSort: [
        { required: true, message: '请输入部门排序' }
      ],
    };
  });
  // 创建
  const toCreate = async () => {
    visible.value = true;
    const { data } = await getDeptTree({});
    treeData.value = data;
  };
  const filterDept = (searchValue: string, nodeData: TreeNodeData) => {
    if (nodeData.title) {
      return nodeData.title.toLowerCase().indexOf(searchValue.toLowerCase()) > -1;
    }
    return false;
  };
  const handleOk = async () => {
    const errors = await formRef.value?.validate();
    if (errors) return false;
    const res = await createDept({
      parentId: formData.value.parentId || 0,
      deptName: formData.value.deptName,
      deptSort: formData.value.deptSort,
      description: formData.value.description,
    });
    if (!res.success) return false;
    Message.success(res.msg);
    handleCancel();
    await fetchData();
    return true;
  };
  const handleCancel = () => {
    visible.value = false;
    formRef.value?.resetFields();
  };
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

  .action-icon {
    margin-left: 12px;
    cursor: pointer;
  }
  .active {
    color: #0960bd;
    background-color: #e3f4fc;
  }
  .setting {
    display: flex;
    align-items: center;
    width: 200px;
    .title {
      margin-left: 12px;
      cursor: pointer;
    }
  }
</style>
