<template>
  <a-list-item-meta>
    <template #avatar>
      <a-typography-paragraph>
        {{ $t('userCenter.securitySettings.updatePwd.label.password') }}
      </a-typography-paragraph>
    </template>
    <template #description>
      <div class="content">
        <a-typography-paragraph v-if="loginStore.pwdResetTime">
          {{ $t('userCenter.securitySettings.updatePwd.placeholder.success.password') }}
        </a-typography-paragraph>
        <a-typography-paragraph v-else class="tip">
          {{ $t('userCenter.securitySettings.updatePwd.placeholder.error.password') }}
        </a-typography-paragraph>
      </div>
      <div class="operation">
        <a-link :title="$t('userCenter.securitySettings.button.update')" @click="toUpdate">
          {{ $t('userCenter.securitySettings.button.update') }}
        </a-link>
      </div>
    </template>
  </a-list-item-meta>

  <a-modal
    :title="$t('userCenter.securitySettings.updatePwd.modal.title')"
    :visible="visible"
    :mask-closable="false"
    @ok="handleUpdate"
    @cancel="handleCancel"
  >
    <a-form ref="formRef" :model="form" :rules="rules" size="large">
      <a-form-item :label="$t('userCenter.securitySettings.updatePwd.form.label.oldPassword')" field="oldPassword">
        <a-input-password
          v-model="form.oldPassword"
          :placeholder="$t('userCenter.securitySettings.updatePwd.form.placeholder.oldPassword')"
          max-length="32"
          allow-clear
        />
      </a-form-item>
      <a-form-item :label="$t('userCenter.securitySettings.updatePwd.form.label.newPassword')" field="newPassword">
        <a-input-password
          v-model="form.newPassword"
          :placeholder="$t('userCenter.securitySettings.updatePwd.form.placeholder.newPassword')"
          max-length="32"
          allow-clear
        />
      </a-form-item>
      <a-form-item :label="$t('userCenter.securitySettings.updatePwd.form.label.rePassword')" field="rePassword">
        <a-input-password
          v-model="form.rePassword"
          :placeholder="$t('userCenter.securitySettings.updatePwd.form.placeholder.rePassword')"
          max-length="32"
          allow-clear
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, reactive, computed } from 'vue';
  import { FieldRule } from '@arco-design/web-vue';
  import { updatePassword } from '@/api/system/user-center';
  import { useI18n } from 'vue-i18n';
  import { useLoginStore } from '@/store';
  import { encryptByRsa } from '@/utils/encrypt';

  const { proxy } = getCurrentInstance() as any;

  const { t } = useI18n();
  const loginStore = useLoginStore();
  const visible = ref(false);

  // 表单数据
  const form = reactive({
    oldPassword: '',
    newPassword: '',
    rePassword: '',
  });
  // 表单验证规则
  const rules = computed((): Record<string, FieldRule[]> => {
    return {
      oldPassword: [{ required: true, message: t('userCenter.securitySettings.updatePwd.form.error.required.oldPassword') }],
      newPassword: [
        { required: true, message: t('userCenter.securitySettings.updatePwd.form.error.required.newPassword') },
        { match: /^(?=.*\d)(?=.*[a-z]).{6,32}$/, message: t('userCenter.securitySettings.updatePwd.form.error.match.newPassword') },
        {
          validator: (value, callback) => {
            if (value === form.oldPassword) {
              callback(t('userCenter.securitySettings.updatePwd.form.error.validator.newPassword'))
            } else {
              callback()
            }
          }
        }
      ],
      rePassword: [
        { required: true, message: t('userCenter.securitySettings.updatePwd.form.error.required.rePassword') },
        {
          validator: (value, callback) => {
            if (value !== form.newPassword) {
              callback(t('userCenter.securitySettings.updatePwd.form.error.validator.rePassword'))
            } else {
              callback()
            }
          }
        }
      ],
    };
  });

  /**
   * 取消
   */
  const handleCancel = () => {
    visible.value = false;
    proxy.$refs.formRef.resetFields();
  };

  /**
   * 修改
   */
  const handleUpdate = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        updatePassword({
          oldPassword: encryptByRsa(form.oldPassword) || '',
          newPassword: encryptByRsa(form.newPassword) || '',
        }).then((res) => {
          handleCancel();
          proxy.$message.success(res.msg);
        });
      }
    });
  };

  /**
   * 打开修改对话框
   */
  const toUpdate = () => {
    visible.value = true;
  };
</script>

<style scoped lang="less"></style>
