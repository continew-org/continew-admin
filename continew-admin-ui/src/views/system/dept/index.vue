<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.dept.list']" />
    <a-card class="general-card" :title="$t('menu.system.dept.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div v-if="showQuery" class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="name" hide-label>
              <a-input
                v-model="queryParams.name"
                placeholder="输入部门名称搜索"
                allow-clear
                style="width: 150px"
                @press-enter="handleQuery"
              />
            </a-form-item>
            <a-form-item field="status" hide-label>
              <a-select
                v-model="queryParams.status"
                :options="dis_enable_status_enum"
                placeholder="状态搜索"
                allow-clear
                style="width: 150px"
              />
            </a-form-item>
            <a-form-item hide-label>
              <a-space>
                <a-button type="primary" @click="handleQuery">
                  <template #icon><icon-search /></template>查询
                </a-button>
                <a-button @click="resetQuery">
                  <template #icon><icon-refresh /></template>重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
        <!-- 操作栏 -->
        <div class="header-operation">
          <a-row>
            <a-col :span="12">
              <a-space>
                <a-button
                  v-permission="['system:dept:add']"
                  type="primary"
                  @click="toAdd"
                >
                  <template #icon><icon-plus /></template>新增
                </a-button>
                <a-button
                  v-permission="['system:dept:update']"
                  type="primary"
                  status="success"
                  :disabled="single"
                  :title="single ? '请选择一条要修改的数据' : ''"
                  @click="toUpdate(ids[0])"
                >
                  <template #icon><icon-edit /></template>修改
                </a-button>
                <a-button
                  v-permission="['system:dept:delete']"
                  type="primary"
                  status="danger"
                  :disabled="multiple"
                  :title="multiple ? '请选择要删除的数据' : ''"
                  @click="handleBatchDelete"
                >
                  <template #icon><icon-delete /></template>删除
                </a-button>
                <a-button
                  v-permission="['system:dept:export']"
                  :loading="exportLoading"
                  type="primary"
                  status="warning"
                  @click="handleExport"
                >
                  <template #icon><icon-download /></template>导出
                </a-button>
              </a-space>
            </a-col>
            <a-col :span="12">
              <right-toolbar
                v-model:show-query="showQuery"
                @refresh="getList"
              />
            </a-col>
          </a-row>
        </div>
      </div>

      <!-- 列表区域 -->
      <a-table
        ref="tableRef"
        row-key="id"
        :data="dataList"
        :loading="loading"
        :row-selection="{
          type: 'checkbox',
          showCheckedAll: true,
          onlyCurrent: false,
        }"
        :pagination="false"
        :bordered="false"
        :default-expand-all-rows="true"
        :hide-expand-button-on-empty="true"
        column-resizable
        stripe
        size="large"
        @select="handleSelect"
        @selection-change="handleSelectionChange"
      >
        <template #columns>
          <a-table-column title="部门名称">
            <template #cell="{ record }">
              <a-link @click="toDetail(record.id)">{{ record.name }}</a-link>
            </template>
          </a-table-column>
          <a-table-column title="部门排序" align="center" data-index="sort" />
          <a-table-column title="状态" align="center" data-index="status">
            <template #cell="{ record }">
              <a-switch
                v-model="record.status"
                :checked-value="1"
                :unchecked-value="2"
                :disabled="
                  record.disabled || !checkPermission(['system:dept:update'])
                "
                @change="handleChangeStatus(record)"
              />
            </template>
          </a-table-column>
          <a-table-column title="类型" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.type === 1" color="red">系统内置</a-tag>
              <a-tag v-else color="arcoblue">自定义</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="描述" data-index="description" />
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column
            v-if="checkPermission(['system:dept:update', 'system:dept:delete'])"
            title="操作"
            align="center"
          >
            <template #cell="{ record }">
              <a-button
                v-permission="['system:dept:update']"
                type="text"
                size="small"
                title="修改"
                @click="toUpdate(record.id)"
              >
                <template #icon><icon-edit /></template>修改
              </a-button>
              <a-popconfirm
                content="确定要删除当前选中的数据吗？"
                type="warning"
                @ok="handleDelete([record.id])"
              >
                <a-button
                  v-permission="['system:dept:delete']"
                  type="text"
                  size="small"
                  title="删除"
                  :disabled="record.disabled"
                >
                  <template #icon><icon-delete /></template>删除
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>

      <!-- 表单区域 -->
      <a-modal
        :title="title"
        :visible="visible"
        :mask-closable="false"
        :esc-to-close="false"
        unmount-on-close
        render-to-body
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form ref="formRef" :model="form" :rules="rules" size="large">
          <a-form-item
            label="上级部门"
            field="parentId"
            :disabled="form.disabled"
          >
            <a-tree-select
              v-model="form.parentId"
              :data="treeData"
              placeholder="请选择上级部门"
              allow-clear
              allow-search
              :filter-tree-node="filterDeptTree"
              :fallback-option="false"
            />
          </a-form-item>
          <a-form-item label="部门名称" field="name">
            <a-input v-model="form.name" placeholder="请输入部门名称" />
          </a-form-item>
          <a-form-item label="部门排序" field="sort">
            <a-input-number
              v-model="form.sort"
              placeholder="请输入部门排序"
              :min="1"
              mode="button"
            />
          </a-form-item>
          <a-form-item label="描述" field="description">
            <a-textarea
              v-model="form.description"
              :max-length="200"
              placeholder="请输入描述"
              :auto-size="{
                minRows: 3,
              }"
              show-word-limit
            />
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 详情区域 -->
      <a-drawer
        title="部门详情"
        :visible="detailVisible"
        :width="580"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleDetailCancel"
      >
        <a-descriptions :column="2" bordered size="large">
          <a-descriptions-item label="部门名称">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.name }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="上级部门">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.parentName || '无' }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>
              <dict-tag
                :value="dataDetail.status"
                :dict="dis_enable_status_enum"
              />
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="部门排序">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.sort }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="创建人">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.createUserString }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.createTime }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="修改人">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.updateUserString }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="修改时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.updateTime }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="描述">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ dataDetail.description }}</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-drawer>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import { TreeNodeData, TableData } from '@arco-design/web-vue';
  import {
    DataRecord,
    ListParam,
    list,
    get,
    add,
    update,
    del,
  } from '@/api/system/dept';
  import { listDeptTree } from '@/api/common';
  import checkPermission from '@/utils/permission';

  const { proxy } = getCurrentInstance() as any;
  const { dis_enable_status_enum } = proxy.useDict('dis_enable_status_enum');

  const dataList = ref<DataRecord[]>([]);
  const dataDetail = ref<DataRecord>({
    name: '',
    sort: 0,
    description: '',
    status: 1,
    createUserString: '',
    createTime: '',
    updateUserString: '',
    updateTime: '',
    parentName: '',
  });
  const ids = ref<Array<number>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const detailLoading = ref(false);
  const exportLoading = ref(false);
  const visible = ref(false);
  const detailVisible = ref(false);
  const treeData = ref<TreeNodeData[]>();

  const data = reactive({
    // 查询参数
    queryParams: {
      name: undefined,
      status: undefined,
      sort: ['parentId,asc', 'sort,asc', 'createTime,desc'],
    },
    // 表单数据
    form: {} as DataRecord,
    // 表单验证规则
    rules: {
      parentId: [{ required: true, message: '请选择上级部门' }],
      name: [{ required: true, message: '请输入部门名称' }],
      sort: [{ required: true, message: '请输入部门排序' }],
    },
  });
  const { queryParams, form, rules } = toRefs(data);

  /**
   * 查询列表
   *
   * @param params 查询参数
   */
  const getList = (params: ListParam = { ...queryParams.value }) => {
    loading.value = true;
    list(params)
      .then((res) => {
        dataList.value = res.data;
        setTimeout(() => {
          proxy.$refs.tableRef.expandAll();
        }, 0);
      })
      .finally(() => {
        loading.value = false;
      });
  };
  getList();

  /**
   * 打开新增对话框
   */
  const toAdd = () => {
    reset();
    listDeptTree({}).then((res) => {
      treeData.value = res.data;
    });
    title.value = '新增部门';
    visible.value = true;
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdate = (id: number) => {
    reset();
    listDeptTree({}).then((res) => {
      treeData.value = res.data;
    });

    get(id).then((res) => {
      form.value = res.data;
      title.value = '修改部门';
      visible.value = true;
    });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    form.value = {
      id: undefined,
      name: '',
      parentId: undefined,
      description: '',
      sort: 999,
      status: 1,
      disabled: false,
    };
    proxy.$refs.formRef?.resetFields();
  };

  /**
   * 取消
   */
  const handleCancel = () => {
    visible.value = false;
    proxy.$refs.formRef.resetFields();
  };

  /**
   * 确定
   */
  const handleOk = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        if (form.value.id !== undefined) {
          update(form.value, form.value.id).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        } else {
          add(form.value).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        }
      }
    });
  };

  /**
   * 查看详情
   *
   * @param id ID
   */
  const toDetail = async (id: number) => {
    if (detailLoading.value) return;
    detailLoading.value = true;
    detailVisible.value = true;
    get(id)
      .then((res) => {
        dataDetail.value = res.data;
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
  };

  /**
   * 批量删除
   */
  const handleBatchDelete = () => {
    if (ids.value.length === 0) {
      proxy.$message.info('请选择要删除的数据');
    } else {
      proxy.$modal.warning({
        title: '警告',
        titleAlign: 'start',
        content: '确定要删除当前选中的数据吗？',
        hideCancel: false,
        onOk: () => {
          handleDelete(ids.value);
        },
      });
    }
  };

  /**
   * 删除
   *
   * @param ids ID 列表
   */
  const handleDelete = (ids: Array<number>) => {
    del(ids).then((res) => {
      proxy.$message.success(res.msg);
      getList();
    });
  };

  /**
   * 点击行选择器
   */
  const handleSelect = (rowKeys: any, rowKey: any, record: TableData) => {
    if (rowKeys.find((key: any) => key === rowKey)) {
      if (record.children) {
        record.children.forEach((r) => {
          proxy.$refs.tableRef.select(r.id);
          rowKeys.push(r.id);
          if (r.children) {
            handleSelect(rowKeys, rowKey, r);
          }
        });
      }
    } else if (record.children) {
      record.children.forEach((r) => {
        rowKeys.splice(
          rowKeys.findIndex((key: number | undefined) => key === r.id),
          1
        );
        proxy.$refs.tableRef.select(r.id, false);
        if (r.children) {
          handleSelect(rowKeys, rowKey, r);
        }
      });
    }
  };

  /**
   * 已选择的数据行发生改变
   *
   * @param rowKeys ID 列表
   */
  const handleSelectionChange = (rowKeys: Array<any>) => {
    ids.value = rowKeys;
    single.value = rowKeys.length !== 1;
    multiple.value = !rowKeys.length;
  };

  /**
   * 导出
   */
  const handleExport = () => {
    if (exportLoading.value) return;
    exportLoading.value = true;
    proxy
      .download('/system/dept/export', { ...queryParams.value }, '部门数据')
      .finally(() => {
        exportLoading.value = false;
      });
  };

  /**
   * 修改状态
   *
   * @param record 记录信息
   */
  const handleChangeStatus = (record: DataRecord) => {
    if (record.id) {
      const tip = record.status === 1 ? '启用' : '禁用';
      update(record, record.id)
        .then(() => {
          proxy.$message.success(`${tip}成功`);
        })
        .catch(() => {
          record.status = record.status === 1 ? 2 : 1;
        });
    }
  };

  /**
   * 过滤部门树
   *
   * @param searchValue 搜索值
   * @param nodeData 节点值
   */
  const filterDeptTree = (searchValue: string, nodeData: TreeNodeData) => {
    if (nodeData.title) {
      return (
        nodeData.title.toLowerCase().indexOf(searchValue.toLowerCase()) > -1
      );
    }
    return false;
  };

  /**
   * 查询
   */
  const handleQuery = () => {
    getList();
  };

  /**
   * 重置
   */
  const resetQuery = () => {
    proxy.$refs.queryRef.resetFields();
    handleQuery();
  };
</script>

<script lang="ts">
  export default {
    name: 'Dept',
  };
</script>

<style scoped lang="less"></style>
