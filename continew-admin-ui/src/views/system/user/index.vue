<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.user.list']" />
    <a-card class="general-card" :title="$t('menu.system.user.list')">
      <a-row>
        <a-col :xs="9" :sm="6" :md="5" :lg="4" :xl="4" style="margin-right: 10px">
          <a-input-search
            v-model="deptName"
            placeholder="输入部门名称搜索"
            style="margin-bottom: 8px; max-width: 240px"
            allow-clear
          />
          <a-tree
            ref="deptTreeRef"
            :data="deptTree"
            default-expand-all
            show-line
            @select="handleSelectNode"
          />
        </a-col>
        <a-col :xs="15" :sm="18" :md="19" :lg="20" :xl="19">
          <!-- 头部区域 -->
          <div class="header">
            <!-- 搜索栏 -->
            <div v-if="showQuery" class="header-query">
              <a-form ref="queryRef" :model="queryParams" layout="inline">
                <a-form-item field="username" hide-label>
                  <a-input
                    v-model="queryParams.username"
                    placeholder="输入用户名搜索"
                    allow-clear
                    style="width: 150px"
                    @press-enter="handleQuery"
                  />
                </a-form-item>
                <a-form-item field="status" hide-label>
                  <a-select
                    v-model="queryParams.status"
                    :options="DisEnableStatusEnum"
                    placeholder="状态搜索"
                    allow-clear
                    style="width: 150px"
                  />
                </a-form-item>
                <a-form-item field="createTime" hide-label>
                  <date-range-picker v-model="queryParams.createTime" />
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
                    <a-button type="primary" @click="toAdd">
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
                    <a-button
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
            :data="userList"
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
            row-key="userId"
            :bordered="false"
            :stripe="true"
            :loading="loading"
            size="large"
            :scroll="{
              x: '120%',
            }"
            @page-change="handlePageChange"
            @page-size-change="handlePageSizeChange"
            @selection-change="handleSelectionChange"
          >
            <template #columns>
              <a-table-column title="ID" data-index="userId" />
              <a-table-column title="用户名">
                <template #cell="{ record }">
                  <a-link @click="toDetail(record.userId)">{{
                    record.username
                  }}</a-link>
                </template>
              </a-table-column>
              <a-table-column title="昵称" data-index="nickname" :width="120" />
              <a-table-column title="性别">
                <template #cell="{ record }">
                  <span v-if="record.gender === 1">男</span>
                  <span v-else-if="record.gender === 2">女</span>
                  <span v-else>未知</span>
                </template>
              </a-table-column>
              <a-table-column title="头像" align="center">
                <template #cell="{ record }">
                  <a-avatar>
                    <img :src="getAvatar(record.avatar, record.gender)" alt="头像" />
                  </a-avatar>
                </template>
              </a-table-column>
              <a-table-column title="联系方式" :width="175">
                <template #cell="{ record }">
                  {{ record.email }}<br v-if="record.email && record.phone">
                  {{ record.phone }}
                </template>
              </a-table-column>
              <a-table-column title="状态" align="center">
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
              <a-table-column title="描述" data-index="description" ellipsis tooltip />
              <a-table-column title="创建人/创建时间" :width="175">
                <template #cell="{ record }">
                  {{ record.createUserString }}<br>
                  {{ record.createTime }}
                </template>
              </a-table-column>
              <a-table-column
                title="操作"
                align="center"
                fixed="right"
                :width="90"
              >
                <template #cell="{ record }">
                  <a-button
                    v-permission="['admin']"
                    type="text"
                    size="small"
                    title="修改"
                    @click="toUpdate(record.userId)"
                  >
                    <template #icon><icon-edit /></template>
                  </a-button>
                  <a-popconfirm
                    content="确定要删除当前选中的数据吗？"
                    type="warning"
                    @ok="handleDelete([record.userId])"
                  >
                    <a-button
                      v-permission="['admin']"
                      type="text"
                      size="small"
                      title="删除"
                      :disabled="record.disabled"
                    >
                      <template #icon><icon-delete /></template>
                    </a-button>
                  </a-popconfirm>
                  <a-popconfirm
                    content="确定要重置当前用户的密码吗？"
                    type="warning"
                    @ok="handleResetPassword(record.userId)"
                  >
                    <a-button
                      v-permission="['admin']"
                      type="text"
                      size="small"
                      title="重置密码"
                    >
                      <template #icon><svg-icon icon-class="privacy" /></template>
                    </a-button>
                  </a-popconfirm>
                  <a-button
                    v-permission="['admin']"
                    type="text"
                    size="small"
                    title="分配角色"
                    @click="toUpdateRole(record.userId)"
                  >
                    <template #icon><svg-icon icon-class="reference" /></template>
                  </a-button>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-col>
      </a-row>

      <!-- 表单区域 -->
      <a-modal
        :title="title"
        :visible="visible"
        :width="565"
        :mask-closable="false"
        unmount-on-close
        render-to-body
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :label-col-style="{ width: '84px' }"
          size="large"
          layout="inline"
        >
          <a-form-item label="用户名" field="username">
            <a-input
              v-model="form.username"
              placeholder="请输入用户名"
              style="width: 162px"
            />
          </a-form-item>
          <a-form-item label="昵称" field="nickname">
            <a-input
              v-model="form.nickname"
              placeholder="请输入昵称"
              style="width: 162px"
            />
          </a-form-item>
          <a-form-item label="邮箱" field="email">
            <a-input
              v-model="form.email"
              placeholder="请输入邮箱"
              style="width: 162px"
            />
          </a-form-item>
          <a-form-item label="手机号码" field="phone">
            <a-input
              v-model="form.phone"
              placeholder="请输入手机号码"
              style="width: 162px"
            />
          </a-form-item>
          <a-form-item label="性别" field="gender">
            <a-radio-group v-model="form.gender">
              <a-radio :value="1">男</a-radio>
              <a-radio :value="2">女</a-radio>
              <a-radio :value="0" disabled>未知</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="所属部门" field="deptId">
            <a-tree-select
              v-model="form.deptId"
              :data="deptOptions"
              placeholder="请选择所属部门"
              allow-clear
              allow-search
              :filter-tree-node="filterDeptOptions"
              style="width: 416px"
            />
          </a-form-item>
          <a-form-item label="所属岗位" field="postIds">
            <a-select
              v-model="form.postIds"
              :options="postOptions"
              placeholder="请选择所属岗位"
              :loading="postLoading"
              multiple
              allow-clear
              :allow-search="{ retainInputValue: true }"
              style="width: 416px"
            />
          </a-form-item>
          <a-form-item label="所属角色" field="roleIds">
            <a-select
              v-model="form.roleIds"
              :options="roleOptions"
              placeholder="请选择所属角色"
              :loading="roleLoading"
              multiple
              allow-clear
              :allow-search="{ retainInputValue: true }"
              style="width: 416px"
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
              style="width: 416px"
            />
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 表单区域 -->
      <a-modal
        title="分配角色"
        :visible="userRoleVisible"
        :mask-closable="false"
        unmount-on-close
        render-to-body
        @ok="handleUpdateUserRole"
        @cancel="handleCancel"
      >
        <a-form ref="userRoleFormRef" :model="form" :rules="rules" size="large">
          <a-form-item label="所属角色" field="roleIds">
            <a-select
              v-model="form.roleIds"
              :options="roleOptions"
              placeholder="请选择所属角色"
              :loading="roleLoading"
              multiple
              allow-clear
              :allow-search="{ retainInputValue: true }"
              style="width: 416px"
            />
          </a-form-item>
        </a-form>
      </a-modal>

      <!-- 详情区域 -->
      <a-drawer
        title="用户详情"
        :visible="detailVisible"
        :width="580"
        :footer="false"
        unmount-on-close
        render-to-body
        @cancel="handleDetailCancel"
      >
        <a-descriptions :column="2" bordered size="large" layout="vertical">
          <a-descriptions-item label="用户名">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.username }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="昵称">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.nickname }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="性别">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>
              <span v-if="user.gender === 1">男</span>
              <span v-else>女</span>
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>
              <a-tag v-if="user.status === 1" color="green">启用</a-tag>
              <a-tag v-else color="red">禁用</a-tag>
            </span>
          </a-descriptions-item>
          <a-descriptions-item label="邮箱">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.email || '无' }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="手机号码">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.phone || '无' }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="所属部门">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.deptName }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="所属岗位">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.postNames }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="所属角色">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.roleNames }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="最后一次修改密码时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.pwdResetTime }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="创建人">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.createUserString }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.createTime }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="修改人">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.updateUserString }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="修改时间">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.updateTime }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="描述">
            <a-skeleton v-if="detailLoading" :animation="true">
              <a-skeleton-line :rows="1" />
            </a-skeleton>
            <span v-else>{{ user.description }}</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-drawer>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive, watch } from 'vue';
  import { TreeNodeData } from '@arco-design/web-vue';
  import {
    UserRecord,
    UserParam,
    listUser,
    getUser,
    addUser,
    updateUser,
    deleteUser,
    resetPassword,
    updateUserRole,
  } from '@/api/system/user';
  import { listDeptTree, listPostDict, listRoleDict } from '@/api/common';
  import { LabelValueState } from '@/store/modules/dict/types';
  import getAvatar from '@/utils/avatar';

  const { proxy } = getCurrentInstance() as any;
  const { DisEnableStatusEnum } = proxy.useDict('DisEnableStatusEnum');

  const userList = ref<UserRecord[]>([]);
  const user = ref<UserRecord>({
    username: '',
    nickname: '',
    gender: 1,
    email: undefined,
    phone: undefined,
    status: 1,
    pwdResetTime: '',
    createUserString: '',
    createTime: '',
    updateUserString: '',
    updateTime: '',
    description: '',
    roleIds: undefined,
    deptId: undefined,
  });
  const total = ref(0);
  const ids = ref<Array<number>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const detailLoading = ref(false);
  const exportLoading = ref(false);
  const visible = ref(false);
  const userRoleVisible = ref(false);
  const detailVisible = ref(false);
  const deptLoading = ref(false);
  const postLoading = ref(false);
  const roleLoading = ref(false);
  const deptOptions = ref<TreeNodeData[]>([]);
  const postOptions = ref<LabelValueState[]>([]);
  const roleOptions = ref<LabelValueState[]>([]);
  const deptTree = ref<TreeNodeData[]>([]);
  const deptName = ref('');

  const data = reactive({
    // 查询参数
    queryParams: {
      username: undefined,
      status: undefined,
      createTime: undefined,
      deptId: undefined,
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    },
    // 表单数据
    form: {} as UserRecord,
    // 表单验证规则
    rules: {
      username: [{ required: true, message: '请输入用户名' }],
      nickname: [{ required: true, message: '请输入昵称' }],
      roleIds: [{ required: true, message: '请选择所属角色' }],
      deptId: [{ required: true, message: '请选择所属部门' }],
    },
  });
  const { queryParams, form, rules } = toRefs(data);

  /**
   * 查询部门树
   *
   * @param name 名称
   */
  const getDeptTree = (name: string) => {
    listDeptTree({ deptName: name }).then((res) => {
      deptTree.value = res.data;
      setTimeout(() => {
        proxy.$refs.deptTreeRef.expandAll();
      }, 0);
    });
  };
  getDeptTree('');
  watch(deptName, (val) => {
    getDeptTree(val);
  });

  /**
   * 查询列表
   *
   * @param params 查询参数
   */
  const getList = (params: UserParam = { ...queryParams.value }) => {
    loading.value = true;
    listUser(params)
      .then((res) => {
        userList.value = res.data.list;
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
    getDeptOptions();
    getPostOptions();
    getRoleOptions();
    title.value = '新增用户';
    visible.value = true;
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdate = (id: number) => {
    reset();
    getDeptOptions();
    getPostOptions();
    getRoleOptions();
    getUser(id).then((res) => {
      form.value = res.data;
      title.value = '修改用户';
      visible.value = true;
    });
  };

  /**
   * 打开分配角色对话框
   *
   * @param id ID
   */
  const toUpdateRole = (id: number) => {
    reset();
    getRoleOptions();
    getUser(id).then((res) => {
      form.value = res.data;
      userRoleVisible.value = true;
    });
  };

  /**
   * 查询部门列表
   */
  const getDeptOptions = () => {
    deptLoading.value = true;
    listDeptTree({})
      .then((res) => {
        deptOptions.value = res.data;
      })
      .finally(() => {
        deptLoading.value = false;
      });
  };

  /**
   * 查询岗位列表
   */
  const getPostOptions = () => {
    postLoading.value = true;
    listPostDict({})
      .then((res) => {
        postOptions.value = res.data;
      })
      .finally(() => {
        postLoading.value = false;
      });
  };

  /**
   * 查询角色列表
   */
  const getRoleOptions = () => {
    roleLoading.value = true;
    listRoleDict({})
      .then((res) => {
        roleOptions.value = res.data;
      })
      .finally(() => {
        roleLoading.value = false;
      });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    form.value = {
      userId: undefined,
      username: '',
      nickname: '',
      gender: 1,
      email: undefined,
      phone: undefined,
      description: '',
      status: 1,
      deptId: undefined,
      postIds: [] as Array<number>,
      roleIds: [] as Array<number>,
    };
    proxy.$refs.formRef?.resetFields();
  };

  /**
   * 取消
   */
  const handleCancel = () => {
    visible.value = false;
    userRoleVisible.value = false;
    proxy.$refs.formRef?.resetFields();
    proxy.$refs.userRoleFormRef?.resetFields();
  };

  /**
   * 确定
   */
  const handleOk = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        if (form.value.userId !== undefined) {
          updateUser(form.value).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        } else {
          addUser(form.value).then((res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          });
        }
      }
    });
  };

  /**
   * 修改用户角色
   */
  const handleUpdateUserRole = () => {
    proxy.$refs.userRoleFormRef.validate((valid: any) => {
      if (!valid && form.value.userId !== undefined) {
        updateUserRole({ roleIds: form.value.roleIds }, form.value.userId).then(
          (res) => {
            handleCancel();
            getList();
            proxy.$message.success(res.msg);
          }
        );
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
    getUser(id)
      .then((res) => {
        user.value = res.data;
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
    deleteUser(ids).then((res) => {
      proxy.$message.success(res.msg);
      getList();
    });
  };

  /**
   * 重置密码
   *
   * @param id ID
   */
  const handleResetPassword = (id: number) => {
    resetPassword(id).then((res) => {
      proxy.$message.success(res.msg);
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
      .download('/system/user/export', { ...queryParams.value }, '用户数据')
      .finally(() => {
        exportLoading.value = false;
      });
  };

  /**
   * 修改状态
   *
   * @param record 记录信息
   */
  const handleChangeStatus = (record: UserRecord) => {
    const tip = record.status === 1 ? '启用' : '禁用';
    updateUser(record)
      .then(() => {
        proxy.$message.success(`${tip}成功`);
      })
      .catch(() => {
        record.status = record.status === 1 ? 2 : 1;
      });
  };

  /**
   * 过滤部门列表
   *
   * @param searchValue 搜索值
   * @param nodeData 节点值
   */
  const filterDeptOptions = (searchValue: string, nodeData: TreeNodeData) => {
    if (nodeData.title) {
      return (
        nodeData.title.toLowerCase().indexOf(searchValue.toLowerCase()) > -1
      );
    }
    return false;
  };

  /**
   * 根据选中部门查询
   *
   * @param keys 选中节点 key
   */
  const handleSelectNode = (keys: Array<any>) => {
    if (queryParams.value.deptId === keys[0]) {
      queryParams.value.deptId = undefined;
      // 如已选中，再次点击则取消选中
      proxy.$refs.deptTreeRef.selectNode(keys, false);
    } else {
      queryParams.value.deptId = keys.length === 1 ? keys[0] : undefined;
    }
    handleQuery();
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
    name: 'User',
  };
</script>

<style scoped lang="less"></style>
