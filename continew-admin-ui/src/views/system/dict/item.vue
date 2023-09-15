<template>
  <div class="app-container">
    <!-- 头部区域 -->
    <div style="margin-bottom: 18px">
      <a-row>
        <a-col :span="18" />
        <a-col :span="6" style="display: flex; justify-content: end">
          <a-button
            v-permission="['system:dictItem:add']"
            type="primary"
            :disabled="!dictId"
            :title="!dictId ? '请先点击左侧字典' : ''"
            @click="toAdd"
          >
            <template #icon><icon-plus /></template>新增
          </a-button>
        </a-col>
      </a-row>
    </div>

    <!-- 列表区域 -->
    <a-table
      ref="tableRef"
      row-key="id"
      :data="dictId ? dataList : []"
      :loading="loading"
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
    >
      <template #columns>
        <a-table-column title="字典标签" align="center">
          <template #cell="{ record }">
            <a-tag v-if="record.color === 'primary'" color="arcoblue">{{
              record.label
            }}</a-tag>
            <a-tag v-else-if="record.color === 'success'" color="green">{{
              record.label
            }}</a-tag>
            <a-tag
              v-else-if="record.color === 'warning'"
              color="orangered"
              >{{ record.label }}</a-tag
            >
            <a-tag v-else-if="record.color === 'error'" color="red">{{
              record.label
            }}</a-tag>
            <a-tag v-else-if="record.color === 'default'" color="gray">{{
              record.label
            }}</a-tag>
            <span v-else-if="!record.color">{{ record.label }}</span>
            <a-tag v-else :color="record.color">{{ record.label }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="字典值" align="center" data-index="value" />
        <a-table-column title="排序" align="center" data-index="sort" />
        <a-table-column title="描述" data-index="description" />
        <a-table-column
          v-if="
            checkPermission([
              'system:dictItem:update',
              'system:dictItem:delete',
            ])
          "
          title="操作"
          align="center"
        >
          <template #cell="{ record }">
            <a-button
              v-permission="['system:dictItem:update']"
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
                v-permission="['system:dictItem:delete']"
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
        <a-form-item label="字典标签" field="label">
          <a-input v-model="form.label" placeholder="请输入字典标签" />
        </a-form-item>
        <a-form-item label="字典值" field="value">
          <a-input v-model="form.value" placeholder="请输入字典值" />
        </a-form-item>
        <a-form-item label="背景颜色" field="color">
          <a-auto-complete
            v-model="form.color"
            :data="colors"
            placeholder="请选择或输入背景颜色"
            allow-clear
          >
            <template #option="{ data }">
              <a-tag v-if="data.value === 'primary'" color="arcoblue">{{
                data.value
              }}</a-tag>
              <a-tag v-else-if="data.value === 'success'" color="green">{{
                data.value
              }}</a-tag>
              <a-tag v-else-if="data.value === 'warning'" color="orangered">{{
                data.value
              }}</a-tag>
              <a-tag v-else-if="data.value === 'error'" color="red">{{
                data.value
              }}</a-tag>
              <a-tag v-else color="gray">{{ data.value }}</a-tag>
            </template>
          </a-auto-complete>
        </a-form-item>
        <a-form-item label="排序" field="sort">
          <a-input-number
            v-model="form.sort"
            placeholder="请输入排序"
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
  </div>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, toRefs, reactive, computed } from 'vue';
  import {
    DataRecord,
    ListParam,
    list,
    get,
    add,
    update,
    del,
  } from '@/api/system/dict-item';
  import checkPermission from '@/utils/permission';

  const props = defineProps({
    dictId: {
      type: Number,
      required: true,
    },
  });
  const { proxy } = getCurrentInstance() as any;
  const dataList = ref<DataRecord[]>([]);
  const colors = ref(['primary', 'success', 'warning', 'error', 'default']);
  const total = ref(0);
  const title = ref('');
  const loading = ref(false);
  const visible = ref(false);
  const dictId = computed(() => props.dictId);

  const data = reactive({
    // 查询参数
    queryParams: {
      page: 1,
      size: 10,
      sort: ['createTime,desc'],
    } as ListParam,
    // 表单数据
    form: {} as DataRecord,
    // 表单验证规则
    rules: {
      label: [{ required: true, message: '字典标签不能为空' }],
      value: [{ required: true, message: '字典值不能为空' }],
    },
  });
  const { queryParams, form, rules } = toRefs(data);

  /**
   * 查询列表
   *
   * @param dictId 字典 ID
   */
  const getList = (dictId: number) => {
    queryParams.value.dictId = dictId;
    loading.value = true;
    list(queryParams.value)
      .then((res) => {
        dataList.value = res.data.list;
        total.value = res.data.total;
      })
      .finally(() => {
        loading.value = false;
      });
  };
  defineExpose({
    getList,
  });

  /**
   * 打开新增对话框
   */
  const toAdd = () => {
    reset();
    title.value = '新增字典项';
    visible.value = true;
  };

  /**
   * 打开修改对话框
   *
   * @param id ID
   */
  const toUpdate = (id: number) => {
    reset();
    get(id).then((res) => {
      form.value = res.data;
      title.value = '修改字典项';
      visible.value = true;
    });
  };

  /**
   * 重置表单
   */
  const reset = () => {
    form.value = {
      id: undefined,
      label: '',
      value: '',
      color: '',
      sort: 999,
      description: '',
      dictId: dictId.value,
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
            getList(dictId.value);
            proxy.$message.success(res.msg);
          });
        } else {
          add(form.value).then((res) => {
            handleCancel();
            getList(dictId.value);
            proxy.$message.success(res.msg);
          });
        }
      }
    });
  };

  /**
   * 删除
   *
   * @param ids ID 列表
   */
  const handleDelete = (ids: Array<number>) => {
    del(ids).then((res) => {
      proxy.$message.success(res.msg);
      getList(dictId.value);
    });
  };

  /**
   * 查询
   */
  const handleQuery = () => {
    getList(dictId.value);
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
    getList(dictId.value);
  };

  /**
   * 切换每页条数
   *
   * @param pageSize 每页条数
   */
  const handlePageSizeChange = (pageSize: number) => {
    queryParams.value.size = pageSize;
    getList(dictId.value);
  };
</script>

<script lang="ts">
  export default {
    name: 'DictItem',
  };
</script>

<style scoped lang="less"></style>
