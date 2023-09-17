<template>
  <div class="app-container">
    <Breadcrumb :items="['menu.system', 'menu.system.menu.list']" />
    <a-card class="general-card" :title="$t('menu.system.menu.list')">
      <!-- 头部区域 -->
      <div class="header">
        <!-- 搜索栏 -->
        <div v-if="showQuery" class="header-query">
          <a-form ref="queryRef" :model="queryParams" layout="inline">
            <a-form-item field="title" hide-label>
              <a-input
                v-model="queryParams.title"
                placeholder="输入菜单标题搜索"
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
                  v-permission="['system:menu:add']"
                  type="primary"
                  @click="toAdd"
                >
                  <template #icon><icon-plus /></template>新增
                </a-button>
                <a-button
                  v-permission="['system:menu:update']"
                  type="primary"
                  status="success"
                  :disabled="single"
                  :title="single ? '请选择一条要修改的数据' : ''"
                  @click="toUpdate(ids[0])"
                >
                  <template #icon><icon-edit /></template>修改
                </a-button>
                <a-button
                  v-permission="['system:menu:delete']"
                  type="primary"
                  status="danger"
                  :disabled="multiple"
                  :title="multiple ? '请选择要删除的数据' : ''"
                  @click="handleBatchDelete"
                >
                  <template #icon><icon-delete /></template>删除
                </a-button>
                <a-button
                  v-permission="['system:menu:export']"
                  :loading="exportLoading"
                  type="primary"
                  status="warning"
                  @click="handleExport"
                >
                  <template #icon><icon-download /></template>导出
                </a-button>
                <a-button type="secondary" @click="handleExpandAll">
                  <template #icon><icon-swap :rotate="90" /></template>展开/折叠
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
          <a-table-column title="菜单标题">
            <template #cell="{ record }">
              <svg-icon :icon-class="record.icon ? record.icon : ''" />
              {{ record.title }}
            </template>
          </a-table-column>
          <a-table-column title="排序" align="center" data-index="sort" />
          <a-table-column title="权限标识" data-index="permission" />
          <a-table-column title="组件路径" data-index="component" />
          <a-table-column title="状态" align="center">
            <template #cell="{ record }">
              <a-switch
                v-model="record.status"
                :checked-value="1"
                :unchecked-value="2"
                :disabled="!checkPermission(['system:menu:update'])"
                @change="handleChangeStatus(record)"
              />
            </template>
          </a-table-column>
          <a-table-column title="外链" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.isExternal" color="arcoblue">是</a-tag>
              <a-tag v-else color="red">否</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="缓存" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.isCache" color="arcoblue">是</a-tag>
              <a-tag v-else color="red">否</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="隐藏" align="center">
            <template #cell="{ record }">
              <a-tag v-if="record.isHidden" color="arcoblue">是</a-tag>
              <a-tag v-else color="red">否</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" data-index="createTime" />
          <a-table-column
            v-if="checkPermission(['system:menu:update', 'system:menu:delete'])"
            title="操作"
            align="center"
          >
            <template #cell="{ record }">
              <a-button
                v-permission="['system:menu:update']"
                type="text"
                size="small"
                title="修改"
                @click="toUpdate(record.id)"
              >
                <template #icon><icon-edit /></template>修改
              </a-button>
              <a-popconfirm
                content="确定要删除当前选中的数据吗？如果存在下级菜单则一并删除，此操作不能撤销！"
                type="warning"
                @ok="handleDelete([record.id])"
              >
                <a-button
                  v-permission="['system:menu:delete']"
                  type="text"
                  size="small"
                  title="删除"
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
        :width="625"
        :mask-closable="false"
        :esc-to-close="false"
        unmount-on-close
        render-to-body
        @ok="handleOk"
        @cancel="handleCancel"
      >
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          layout="inline"
          :label-col-style="{ width: '85px' }"
          size="large"
        >
          <a-form-item label="菜单类型" field="type" lab>
            <a-radio-group v-model="form.type" type="button">
              <a-radio :value="1" style="width: 57px">目录</a-radio>
              <a-radio :value="2" style="width: 57px">菜单</a-radio>
              <a-radio :value="3" style="width: 57px">按钮</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item v-if="form.type !== 3" label="菜单图标" field="icon">
            <a-popover
              v-model:popup-visible="showChooseIcon"
              position="bottom"
              trigger="click"
            >
              <a-input
                v-model="form.icon"
                placeholder="点击选择菜单图标"
                readonly
                allow-clear
                style="width: 473px"
              >
                <template #prefix>
                  <svg-icon
                    v-if="form.icon"
                    :icon-class="form.icon"
                    style="height: 32px; width: 16px"
                  />
                  <icon-search v-else />
                </template>
              </a-input>
              <template #content>
                <icon-select ref="iconSelectRef" @selected="selected" />
              </template>
            </a-popover>
          </a-form-item>
          <a-form-item label="菜单标题" field="title">
            <a-input
              v-model="form.title"
              placeholder="请输入菜单标题"
              style="width: 182px"
            />
          </a-form-item>
          <a-form-item label="菜单排序" field="sort">
            <a-input-number
              v-model="form.sort"
              placeholder="请输入菜单排序"
              :min="1"
              mode="button"
              style="width: 182px"
            />
          </a-form-item>
          <a-form-item
            v-if="form.type !== 1"
            label="权限标识"
            field="permission"
          >
            <a-input
              v-model="form.permission"
              placeholder="请输入权限标识"
              style="width: 182px"
            />
          </a-form-item>
          <a-form-item v-if="form.type !== 3" label="路由地址" field="path">
            <a-input
              v-model="form.path"
              placeholder="请输入路由地址"
              style="width: 473px"
            />
          </a-form-item>
          <a-form-item
            v-if="!form.isExternal && form.type === 2"
            label="组件名称"
            field="name"
          >
            <a-input
              v-model="form.name"
              placeholder="请输入组件名称"
              style="width: 182px"
            />
          </a-form-item>
          <a-form-item
            v-if="!form.isExternal && form.type === 2"
            label="组件路径"
            field="component"
          >
            <a-input
              v-model="form.component"
              placeholder="请输入组件路径"
              style="width: 182px"
            />
          </a-form-item>
          <br />
          <a-form-item
            v-if="form.type !== 3"
            label="是否外链"
            field="isExternal"
          >
            <a-radio-group v-model="form.isExternal" type="button">
              <a-radio :value="true">是</a-radio>
              <a-radio :value="false">否</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item v-if="form.type === 2" label="是否缓存" field="isCache">
            <a-radio-group v-model="form.isCache" type="button">
              <a-radio :value="true">是</a-radio>
              <a-radio :value="false">否</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item v-if="form.type !== 3" label="是否隐藏" field="isHidden">
            <a-radio-group v-model="form.isHidden" type="button">
              <a-radio :value="true">是</a-radio>
              <a-radio :value="false">否</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="上级菜单" field="parentId">
            <a-tree-select
              v-model="form.parentId"
              :data="treeData"
              placeholder="请选择上级菜单"
              allow-clear
              allow-search
              :filter-tree-node="filterMenuTree"
              :fallback-option="false"
              style="width: 473px"
            />
          </a-form-item>
        </a-form>
      </a-modal>
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
  } from '@/api/system/menu';
  import { listMenuTree } from '@/api/common';
  import checkPermission from '@/utils/permission';

  const { proxy } = getCurrentInstance() as any;
  const { dis_enable_status_enum } = proxy.useDict('dis_enable_status_enum');

  const dataList = ref<DataRecord[]>([]);
  const ids = ref<Array<number>>([]);
  const title = ref('');
  const single = ref(true);
  const multiple = ref(true);
  const showQuery = ref(true);
  const loading = ref(false);
  const exportLoading = ref(false);
  const expandAll = ref(false);
  const visible = ref(false);
  const showChooseIcon = ref(false);
  const treeData = ref<TreeNodeData[]>();

  const data = reactive({
    // 查询参数
    queryParams: {
      title: undefined,
      status: undefined,
      sort: ['parentId,asc', 'sort,asc', 'createTime,desc'],
    },
    // 表单数据
    form: {} as DataRecord,
    // 表单验证规则
    rules: {
      title: [{ required: true, message: '请输入菜单标题' }],
      path: [{ required: true, message: '请输入路由地址' }],
      name: [{ required: true, message: '请输入组件名称' }],
      component: [{ required: true, message: '请输入组件路径' }],
      permission: [{ required: true, message: '请输入权限标识' }],
      sort: [{ required: true, message: '请输入菜单排序' }],
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
    listMenuTree({}).then((res) => {
      treeData.value = res.data;
    });
    title.value = '新增菜单';
    visible.value = true;
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdate = (id: number) => {
    reset();
    listMenuTree({}).then((res) => {
      treeData.value = res.data;
    });

    get(id).then((res) => {
      form.value = res.data;
      title.value = '修改菜单';
      visible.value = true;
    });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    form.value = {
      id: undefined,
      title: '',
      parentId: undefined,
      type: 1,
      path: undefined,
      name: undefined,
      component: undefined,
      icon: undefined,
      isExternal: false,
      isCache: false,
      isHidden: false,
      permission: undefined,
      sort: 999,
      status: 1,
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
   * 批量删除
   */
  const handleBatchDelete = () => {
    if (ids.value.length === 0) {
      proxy.$message.info('请选择要删除的数据');
    } else {
      proxy.$modal.warning({
        title: '警告',
        titleAlign: 'start',
        content:
          '确定要删除当前选中的数据吗？如果存在下级菜单则一并删除，此操作不能撤销！',
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
      .download('/system/menu/export', { ...queryParams.value }, '菜单数据')
      .finally(() => {
        exportLoading.value = false;
      });
  };

  /**
   * 展开/折叠
   */
  const handleExpandAll = () => {
    expandAll.value = !expandAll.value;
    proxy.$refs.tableRef.expandAll(expandAll.value);
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
   * 过滤菜单树
   *
   * @param searchValue 搜索值
   * @param nodeData 节点值
   */
  const filterMenuTree = (searchValue: string, nodeData: TreeNodeData) => {
    if (nodeData.title) {
      return (
        nodeData.title.toLowerCase().indexOf(searchValue.toLowerCase()) > -1
      );
    }
    return false;
  };

  /**
   * 展示下拉图标
   */
  const showSelectIcon = () => {
    proxy.$refs.iconSelectRef.reset();
    showChooseIcon.value = true;
  };

  /**
   * 选择图标
   *
   * @param name 图标名称
   */
  const selected = (name: string) => {
    form.value.icon = name;
    showChooseIcon.value = false;
  };

  const hideSelectIcon = () => {
    showChooseIcon.value = false;
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
    // eslint-disable-next-line vue/no-reserved-component-names
    name: 'Menu',
  };
</script>

<style scoped lang="less"></style>
