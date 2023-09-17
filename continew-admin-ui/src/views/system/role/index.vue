<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.role.list']" />
    <a-card class="general-card" :title="$t('menu.system.role.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div v-if="showQuery" class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="name" hide-label>
              <a-input
                v-model="queryParams.name"
                placeholder="输入角色名称搜索"
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
                  v-permission="['system:role:add']"
                  type="primary"
                  @click="toAdd"
                >
                  <template #icon><icon-plus /></template>新增
                </a-button>
                <a-button
                  v-permission="['system:role:update']"
                  type="primary"
                  status="success"
                  :disabled="single"
                  :title="single ? '请选择一条要修改的数据' : ''"
                  @click="toUpdate(ids[0])"
                >
                  <template #icon><icon-edit /></template>修改
                </a-button>
                <a-button
                  v-permission="['system:role:delete']"
                  type="primary"
                  status="danger"
                  :disabled="multiple"
                  :title="multiple ? '请选择要删除的数据' : ''"
                  @click="handleBatchDelete"
                >
                  <template #icon><icon-delete /></template>删除
                </a-button>
                <a-button
                  v-permission="['system:role:export']"
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
        :pagination="{
          showTotal: true,
          showPageSize: true,
          total: total,
          current: queryParams.page,
        }"
        :bordered="false"
        column-resizable
        stripe
        size="large"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
        @selection-change="handleSelectionChange"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" />
          <a-table-column title="角色名称" data-index="name" :width="130">
            <template #cell="{ record }">
              <a-link @click="toDetail(record.id)">{{ record.name }}</a-link>
            </template>
          </a-table-column>
          <a-table-column title="角色编码" data-index="code" />
          <a-table-column title="数据权限" :width="130">
            <template #cell="{ record }">
              <dict-tag :value="record.dataScope" :dict="data_scope_enum" />
            </template>
          </a-table-column>
          <a-table-column title="角色排序" align="center" data-index="sort" />
          <a-table-column title="状态" align="center" data-index="status">
            <template #cell="{ record }">
              <a-switch
                v-model="record.status"
                :checked-value="1"
                :unchecked-value="2"
                :disabled="
                  record.disabled || !checkPermission(['system:role:update'])
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
          <a-table-column title="描述" data-index="description" tooltip />
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column
            v-if="checkPermission(['system:role:update', 'system:role:delete'])"
            title="操作"
            align="center"
          >
            <template #cell="{ record }">
              <a-button
                v-permission="['system:role:update']"
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
                  v-permission="['system:role:delete']"
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
      <a-drawer
        :title="title"
        :visible="visible"
        :width="580"
        :mask-closable="false"
        :esc-to-close="false"
        unmount-on-close
        render-to-body
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form ref="formRef" :model="form" :rules="rules" size="large">
          <a-alert
            v-if="!form.disabled"
            type="warning"
            style="margin-bottom: 15px"
          >
            变更角色编码、功能权限或数据权限后，关联在线用户会自动下线！
          </a-alert>
          <fieldset>
            <legend>基础信息</legend>
            <a-form-item label="角色名称" field="name">
              <a-input v-model="form.name" placeholder="请输入角色名称" />
            </a-form-item>
            <a-form-item
              label="角色编码"
              field="code"
              :disabled="form.disabled"
            >
              <a-input v-model="form.code" placeholder="请输入角色编码" />
            </a-form-item>
            <a-form-item label="角色排序" field="sort">
              <a-input-number
                v-model="form.sort"
                placeholder="请输入角色排序"
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
          </fieldset>
          <fieldset>
            <legend>功能权限</legend>
            <a-form-item label="功能权限" :disabled="form.disabled">
              <a-space style="margin-top: 2px">
                <a-checkbox
                  v-model="menuExpandAll"
                  @change="handleExpandAll('menu')"
                  >展开/折叠</a-checkbox
                >
                <a-checkbox
                  v-model="menuCheckAll"
                  @change="handleCheckAll('menu')"
                  >全选/全不选</a-checkbox
                >
                <a-checkbox v-model="menuCheckStrictly">父子联动</a-checkbox>
              </a-space>
              <template #extra>
                <a-spin v-if="menuLoading" />
                <a-tree
                  v-if="!menuLoading"
                  ref="menuRef"
                  :data="menuOptions"
                  :default-checked-keys="form.menuIds"
                  :check-strictly="!menuCheckStrictly"
                  :default-expand-all="menuExpandAll"
                  checkable
                />
              </template>
            </a-form-item>
          </fieldset>
          <fieldset>
            <legend>数据权限</legend>
            <a-form-item
              label="数据权限"
              field="dataScope"
              :disabled="form.disabled"
            >
              <a-select
                v-model="form.dataScope"
                :options="data_scope_enum"
                placeholder="请选择数据权限"
              />
            </a-form-item>
            <a-form-item
              v-if="form.dataScope === 5"
              label="权限范围"
              :disabled="form.disabled"
            >
              <a-space style="margin-top: 2px">
                <a-checkbox
                  v-model="deptExpandAll"
                  @change="handleExpandAll('dept')"
                  >展开/折叠</a-checkbox
                >
                <a-checkbox
                  v-model="deptCheckAll"
                  @change="handleCheckAll('dept')"
                  >全选/全不选</a-checkbox
                >
                <a-checkbox v-model="deptCheckStrictly">父子联动</a-checkbox>
              </a-space>
              <template #extra>
                <a-spin v-if="deptLoading" />
                <a-tree
                  v-if="!deptLoading"
                  ref="deptRef"
                  :data="deptOptions"
                  :default-checked-keys="form.deptIds"
                  :check-strictly="!deptCheckStrictly"
                  :default-expand-all="deptExpandAll"
                  checkable
                />
              </template>
            </a-form-item>
          </fieldset>
        </a-form>
      </a-drawer>

      <!-- 详情区域 -->
      <a-drawer
        title="角色详情"
        :visible="detailVisible"
        :width="580"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleDetailCancel"
      >
        <a-card title="基础信息" :bordered="false">
          <a-descriptions :column="2" bordered size="large">
            <a-descriptions-item label="角色名称">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>{{ dataDetail.name }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="角色编码">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>{{ dataDetail.code }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>
                <a-tag v-if="dataDetail.status === 1" color="green">启用</a-tag>
                <a-tag v-else color="red">禁用</a-tag>
              </span>
            </a-descriptions-item>
            <a-descriptions-item label="数据权限">
              <a-skeleton v-if="detailLoading" :animation="true">
                <a-skeleton-line :rows="1" />
              </a-skeleton>
              <span v-else>
                <dict-tag
                  :value="dataDetail.dataScope"
                  :dict="data_scope_enum"
                />
              </span>
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
        </a-card>
        <a-card :loading="menuLoading" title="功能权限" :bordered="false">
          <a-tree
            :data="menuOptions"
            :checked-keys="dataDetail.menuIds"
            :default-expand-all="false"
            check-strictly
            checkable
          />
        </a-card>
        <a-card
          v-if="dataDetail.dataScope === 5"
          :loading="deptLoading"
          title="数据权限"
          :bordered="false"
        >
          <a-tree
            :data="deptOptions"
            :checked-keys="dataDetail.deptIds"
            default-expand-all
            check-strictly
            checkable
          />
        </a-card>
      </a-drawer>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive } from 'vue';
  import { TreeNodeData } from '@arco-design/web-vue';
  import {
    DataRecord,
    ListParam,
    list,
    get,
    add,
    update,
    del,
  } from '@/api/system/role';
  import { listMenuTree, listDeptTree } from '@/api/common';
  import checkPermission from '@/utils/permission';

  const { proxy } = getCurrentInstance() as any;
  const { data_scope_enum, dis_enable_status_enum } = proxy.useDict(
    'data_scope_enum',
    'dis_enable_status_enum'
  );

  const dataList = ref<DataRecord[]>([]);
  const dataDetail = ref<DataRecord>({
    name: '',
    code: '',
    status: 1,
    dataScope: 1,
    createUserString: '',
    createTime: '',
    updateUserString: '',
    updateTime: '',
    description: '',
    menuIds: undefined,
    deptIds: undefined,
  });
  const total = ref(0);
  const ids = ref<Array<string>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const detailLoading = ref(false);
  const exportLoading = ref(false);
  const visible = ref(false);
  const detailVisible = ref(false);
  const menuLoading = ref(false);
  const deptLoading = ref(false);
  const menuOptions = ref<TreeNodeData[]>([]);
  const deptOptions = ref<TreeNodeData[]>([]);
  const menuExpandAll = ref(false);
  const deptExpandAll = ref(true);
  const menuCheckAll = ref(false);
  const deptCheckAll = ref(false);
  const menuCheckStrictly = ref(true);
  const deptCheckStrictly = ref(true);

  const data = reactive({
    // 查询参数
    queryParams: {
      name: undefined,
      status: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    },
    // 表单数据
    form: {} as DataRecord,
    // 表单验证规则
    rules: {
      name: [
        { required: true, message: '请输入角色名称' },
        {
          match: /^[\u4e00-\u9fa5a-zA-Z0-9_-]{1,20}$/,
          message:
            '长度为 1 到 20 位，可以包含中文、字母、数字、下划线，短横线',
        },
      ],
      code: [
        { required: true, message: '请输入角色编码' },
        {
          match: /^[a-zA-Z][a-zA-Z0-9_]{1,15}$/,
          message: '长度为 2 到 16 位，可以包含字母、数字，下划线，以字母开头',
        },
      ],
      dataScope: [{ required: true, message: '请选择数据权限' }],
      sort: [{ required: true, message: '请输入角色排序' }],
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
        dataList.value = res.data.list;
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
  const toAdd = () => {
    reset();
    getMenuTree();
    title.value = '新增角色';
    visible.value = true;
    getDeptTree();
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdate = (id: string) => {
    reset();
    menuCheckStrictly.value = false;
    deptCheckStrictly.value = false;
    getMenuTree();
    getDeptTree();
    get(id).then((res) => {
      form.value = res.data;
      title.value = '修改角色';
      visible.value = true;
    });
  };

  /**
   * 查询菜单树
   */
  const getMenuTree = () => {
    if (menuOptions.value.length <= 0) {
      menuLoading.value = true;
    }
    listMenuTree({})
      .then((res) => {
        menuOptions.value = res.data;
      })
      .finally(() => {
        menuLoading.value = false;
      });
  };

  /**
   * 查询部门树
   */
  const getDeptTree = () => {
    if (deptOptions.value.length <= 0) {
      deptLoading.value = true;
    }
    listDeptTree({})
      .then((res) => {
        deptOptions.value = res.data;
      })
      .finally(() => {
        deptLoading.value = false;
      });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    menuExpandAll.value = false;
    menuCheckAll.value = false;
    menuCheckStrictly.value = true;
    deptExpandAll.value = true;
    deptCheckAll.value = false;
    deptCheckStrictly.value = true;
    proxy.$refs.menuRef?.expandAll(menuExpandAll.value);
    proxy.$refs.deptRef?.expandAll(deptExpandAll.value);
    form.value = {
      id: undefined,
      name: '',
      code: undefined,
      dataScope: 4,
      description: '',
      sort: 999,
      status: 1,
      menuIds: [],
      deptIds: [],
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
   * 获取所有选中的菜单
   */
  const getMenuAllCheckedKeys = () => {
    // 获取目前被选中的菜单
    const checkedNodes = proxy.$refs.menuRef.getCheckedNodes();
    const checkedKeys = checkedNodes.map((item: TreeNodeData) => item.key);

    // 获取半选中的菜单
    const halfCheckedNodes = proxy.$refs.menuRef.getHalfCheckedNodes();
    const halfCheckedKeys = halfCheckedNodes.map(
      (item: TreeNodeData) => item.key
    );
    // eslint-disable-next-line prefer-spread
    checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
    return checkedKeys;
  };

  /**
   * 获取所有选中的部门
   */
  const getDeptAllCheckedKeys = () => {
    if (!proxy.$refs.deptRef) {
      return [];
    }
    // 获取目前被选中的部门
    const checkedNodes = proxy.$refs.deptRef.getCheckedNodes();
    const checkedKeys = checkedNodes.map((item: TreeNodeData) => item.key);

    // 获取半选中的部门
    const halfCheckedNodes = proxy.$refs.deptRef.getHalfCheckedNodes();
    const halfCheckedKeys = halfCheckedNodes.map(
      (item: TreeNodeData) => item.key
    );
    // eslint-disable-next-line prefer-spread
    checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
    return checkedKeys;
  };

  /**
   * 确定
   */
  const handleOk = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        if (form.value.id !== undefined) {
          form.value.menuIds = getMenuAllCheckedKeys();
          form.value.deptIds = getDeptAllCheckedKeys();
          update(form.value, form.value.id).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        } else {
          form.value.menuIds = getMenuAllCheckedKeys();
          form.value.deptIds = getDeptAllCheckedKeys();
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
  const toDetail = async (id: string) => {
    if (detailLoading.value) return;
    getMenuTree();
    getDeptTree();
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
  const handleDelete = (ids: Array<string>) => {
    del(ids).then((res) => {
      proxy.$message.success(res.msg);
      getList();
    });
  };

  /**
   * 已选择的数据行发生改变时触发
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
      .download('/system/role/export', { ...queryParams.value }, '角色数据')
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
   * 展开/折叠
   *
   * @param type 类型（菜单/部门）
   */
  const handleExpandAll = (type: string) => {
    if (type === 'menu') {
      proxy.$refs.menuRef.expandAll(menuExpandAll.value);
    } else if (type === 'dept') {
      proxy.$refs.deptRef.expandAll(deptExpandAll.value);
    }
  };

  /**
   * 全选/全不选
   *
   * @param type 类型（菜单/部门）
   */
  const handleCheckAll = (type: string) => {
    if (type === 'menu') {
      proxy.$refs.menuRef.checkAll(menuCheckAll.value);
    } else if (type === 'dept') {
      proxy.$refs.deptRef.checkAll(deptCheckAll.value);
    }
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
