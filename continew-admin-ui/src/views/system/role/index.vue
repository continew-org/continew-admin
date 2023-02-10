<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.role.list']" />
    <a-card class="general-card" :title="$t('menu.system.role.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div v-if="showQuery" class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="roleName" hide-label>
              <a-input
                v-model="queryParams.roleName"
                placeholder="输入角色名称搜索"
                allow-clear
                style="width: 150px"
                @press-enter="handleQuery"
              />
            </a-form-item>
            <a-form-item field="status" hide-label>
              <a-select
                v-model="queryParams.status"
                :options="statusOptions"
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
                <a-button type="primary" @click="toCreate">
                  <template #icon><icon-plus /></template>新增
                </a-button>
                <a-button
                  type="primary"
                  status="success"
                  :disabled="single"
                  :title="single ? '请选择一条要修改的数据' : ''"
                  @click="toUpdate(ids[0])"
                >
                  <template #icon><icon-edit /></template>修改
                </a-button>
                <a-button
                  type="primary"
                  status="danger"
                  :disabled="multiple"
                  :title="multiple ? '请选择要删除的数据' : ''"
                  @click="handleBatchDelete"
                >
                  <template #icon><icon-delete /></template>删除
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
        :data="roleList"
        :row-selection="{
          type: 'checkbox',
          showCheckedAll: true,
          onlyCurrent: false,
        }"
        :pagination="{
          showTotal: true,
          showPageSize: true,
          total: total,
          current: queryParams.page,
        }"
        row-key="roleId"
        :bordered="false"
        :stripe="true"
        :loading="loading"
        size="large"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        @selection-change="handleSelectionChange"
      >
        <template #columns>
          <a-table-column title="ID" data-index="roleId" />
          <a-table-column title="角色名称" data-index="roleName">
            <template #cell="{ record }">
              <a-link @click="toDetail(record.roleId)">{{
                record.roleName
              }}</a-link>
            </template>
          </a-table-column>
          <a-table-column title="角色编码" data-index="roleCode" />
          <a-table-column title="数据权限">
            <template #cell="{ record }">
              <span v-if="record.dataScope === 1">全部数据权限</span>
              <span v-else-if="record.dataScope === 2">本部门及以下数据权限</span>
              <span v-else-if="record.dataScope === 3">本部门数据权限</span>
              <span v-else-if="record.dataScope === 4">仅本人数据权限</span>
              <span v-else>自定义数据权限</span>
            </template>
          </a-table-column>
          <a-table-column
            title="角色排序"
            align="center"
            data-index="roleSort"
          />
          <a-table-column title="状态" align="center" data-index="status">
            <template #cell="{ record }">
              <a-switch
                v-model="record.status"
                :checked-value="1"
                :unchecked-value="2"
                :disabled="record.disabled"
                @change="handleChangeStatus(record)"
              />
            </template>
          </a-table-column>
          <a-table-column title="描述" data-index="description" />
          <a-table-column title="创建人" data-index="createUserString" />
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column title="操作" align="center">
            <template #cell="{ record }">
              <a-button
                v-permission="['admin']"
                type="text"
                size="small"
                title="修改"
                :disabled="record.disabled"
                @click="toUpdate(record.roleId)"
              >
                <template #icon><icon-edit /></template>修改
              </a-button>
              <a-popconfirm
                content="确定要删除当前选中的数据吗？"
                type="warning"
                @ok="handleDelete([record.roleId])"
              >
                <a-button
                  v-permission="['admin']"
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
        :width="570"
        :mask-closable="false"
        unmount-on-close
        render-to-body
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form ref="formRef" :model="form" :rules="rules">
          <a-form-item label="角色名称" field="roleName">
            <a-input
              v-model="form.roleName"
              placeholder="请输入角色名称"
              size="large"
            />
          </a-form-item>
          <a-form-item label="角色编码" field="roleCode">
            <a-input
              v-model="form.roleCode"
              placeholder="请输入角色编码"
              size="large"
            />
          </a-form-item>
          <a-form-item label="数据权限" field="dataScope">
            <a-select
              v-model="form.dataScope"
              :options="dataScopeOptions"
              placeholder="请选择数据权限"
              size="large"
            />
          </a-form-item>
          <a-form-item
            v-if="form.dataScope === 5"
            label="数据范围"
            field="dataScopeDeptIds"
          >
            <a-tree-select
              v-model="form.dataScopeDeptIds"
              :data="treeData"
              placeholder="请选择数据范围"
              allow-search
              allow-clear
              tree-checkable
              tree-check-strictly
              :filter-tree-node="filterDeptTree"
              :fallback-option="false"
              size="large"
            />
          </a-form-item>
          <a-form-item label="角色排序" field="roleSort">
            <a-input-number
              v-model="form.roleSort"
              placeholder="请输入角色排序"
              :min="1"
              mode="button"
              size="large"
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
              size="large"
            />
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 详情区域 -->
      <a-drawer
        title="部门详情"
        :visible="detailVisible"
        :width="570"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleDetailCancel"
      >
        <a-descriptions title="基础信息" :column="2" bordered size="large">
          <a-descriptions-item label="角色名称">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ role.roleName }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="角色编码">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ role.roleCode }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>
              <a-tag v-if="role.status === 1" color="green">
                <span class="circle pass"></span>启用
              </a-tag>
              <a-tag v-else color="red">
                <span class="circle fail"></span>禁用
              </a-tag>
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="数据权限">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>
              <span v-if="role.dataScope === 1">全部数据权限</span>
              <span v-else-if="role.dataScope === 2">本部门及以下数据权限</span>
              <span v-else-if="role.dataScope === 3">本部门数据权限</span>
              <span v-else-if="role.dataScope === 4">仅本人数据权限</span>
              <span v-else>自定义数据权限</span>
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="创建人">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ role.createUserString }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ role.createTime }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="修改人">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ role.updateUserString }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="修改时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ role.updateTime }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="描述">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ role.description }}</span>
          </a-descriptions-item>
        </a-descriptions>
        <a-descriptions
          v-if="role.dataScope === 5"
          title="数据权限"
          :column="2"
          bordered
          size="large"
          style="margin-top: 25px"
        >
          <a-descriptions-item label="数据范围">
            <a-tree-select
              v-model="role.dataScopeDeptIds"
              :data="treeData"
              tree-checkable
              disabled
            />
          </a-descriptions-item>
        </a-descriptions>
      </a-drawer>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import { SelectOptionData, TreeNodeData } from '@arco-design/web-vue';
  import {
    RoleRecord,
    RoleParam,
    listRole,
    getRole,
    createRole,
    updateRole,
    deleteRole,
  } from '@/api/system/role';
  import listDeptTree from '@/api/common';

  const { proxy } = getCurrentInstance() as any;

  const roleList = ref<RoleRecord[]>([]);
  const role = ref<RoleRecord>({
    roleName: '',
    roleCode: '',
    dataScope: 1,
    description: '',
    roleSort: 0,
    status: 1,
    createUserString: '',
    createTime: '',
    updateUserString: '',
    updateTime: '',
  });
  const total = ref(0);
  const ids = ref<Array<number>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const detailLoading = ref(false);
  const visible = ref(false);
  const detailVisible = ref(false);
  const statusOptions = ref<SelectOptionData[]>([
    { label: '启用', value: 1 },
    { label: '禁用', value: 2 },
  ]);
  const dataScopeOptions = ref<SelectOptionData[]>([
    { label: '全部数据权限', value: 1 },
    { label: '本部门及以下数据权限', value: 2 },
    { label: '本部门数据权限', value: 3 },
    { label: '仅本人数据权限', value: 4 },
    { label: '自定义数据权限', value: 5 },
  ]);
  const treeData = ref<TreeNodeData[]>();

  const data = reactive({
    // 查询参数
    queryParams: {
      roleName: undefined,
      status: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    },
    // 表单数据
    form: {} as RoleRecord,
    // 表单验证规则
    rules: {
      roleName: [{ required: true, message: '请输入角色名称' }],
      dataScope: [{ required: true, message: '请选择数据权限' }],
      dataScopeDeptIds: [{ required: true, message: '请选择数据范围' }],
      roleSort: [{ required: true, message: '请输入角色排序' }],
    },
  });
  const { queryParams, form, rules } = toRefs(data);

  /**
   * 查询列表
   *
   * @param params 查询参数
   */
  const getList = (params: RoleParam = { ...queryParams.value }) => {
    loading.value = true;
    listRole(params)
      .then((res) => {
        roleList.value = res.data.list;
        total.value = res.data.total;
      })
      .finally(() => {
        loading.value = false;
      });
  };
  getList();

  /**
   * 打开新增对话框
   */
  const toCreate = () => {
    reset();
    listDeptTree({ status: 1 }).then((res) => {
      treeData.value = res.data;
    });
    title.value = '新增角色';
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
    getRole(id).then((res) => {
      form.value = res.data;
      title.value = '修改角色';
      visible.value = true;
    });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    form.value = {
      roleId: undefined,
      roleName: '',
      roleCode: undefined,
      dataScope: 4,
      dataScopeDeptIds: undefined,
      description: '',
      roleSort: 999,
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
        if (form.value.roleId !== undefined) {
          updateRole(form.value).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        } else {
          createRole(form.value).then((res) => {
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
    getRole(id)
      .then((res) => {
        role.value = res.data;
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
    deleteRole(ids).then((res) => {
      proxy.$message.success(res.msg);
      getList();
    });
  };

  /**
   * 多选
   *
   * @param rowKeys ID 列表
   */
  const handleSelectionChange = (rowKeys: Array<any>) => {
    ids.value = rowKeys;
    single.value = rowKeys.length !== 1;
    multiple.value = !rowKeys.length;
  };

  /**
   * 修改状态
   *
   * @param record 记录信息
   */
  const handleChangeStatus = (record: RoleRecord) => {
    const tip = record.status === 1 ? '启用' : '禁用';
    updateRole(record)
      .then(() => {
        proxy.$message.success(`${tip}成功`);
      })
      .catch(() => {
        record.status = record.status === 1 ? 2 : 1;
      });
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

  /**
   * 切换页码
   *
   * @param current 页码
   */
  const handlePageChange = (current: number) => {
    queryParams.value.page = current;
    getList();
  };

  /**
   * 切换每页条数
   *
   * @param pageSize 每页条数
   */
  const handlePageSizeChange = (pageSize: number) => {
    queryParams.value.size = pageSize;
    getList();
  };
</script>

<script lang="ts">
  export default {
    name: 'Role',
  };
</script>

<style scoped lang="less"></style>
