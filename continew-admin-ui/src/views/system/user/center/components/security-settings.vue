<template>
  <a-modal v-model:visible="updatePasswordVisible" :title="$t('userCenter.SecuritySettings.form.password.modal.title')" @cancel="handleCancelUpdatePassword" @before-ok="handleBeforeOkUpdatePassword">
    <a-form
      ref="formPasswordRef"
      :model="formPasswordData"
    >
      <a-form-item
        field="oldPassword"
        :rules="{ required: true, message: $t('userCenter.SecuritySettings.form.password.oldPassword.placeholder') }"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.SecuritySettings.form.password.oldPassword.label')"
      >
        <a-input-password
          v-model="formPasswordData.oldPassword"
          :placeholder="$t('userCenter.SecuritySettings.form.password.oldPassword.placeholder')"
          size="large"
          allow-clear
          max-length="50"
        >
        </a-input-password>
      </a-form-item>
      <a-form-item
        field="newPassword"
        :rules="[
          {
            required: true,
            message: $t('userCenter.SecuritySettings.form.password.error.newPassword.required'),
          },
          {
            match: /^(?=.*\d)(?=.*[a-z]).{6,32}$/,
            message: $t('userCenter.SecuritySettings.form.password.newPassword.placeholder')
          }
        ]"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.SecuritySettings.form.password.newPassword.label')"
      >
        <a-input-password
          v-model="formPasswordData.newPassword"
          :placeholder="$t('userCenter.SecuritySettings.form.password.newPassword.placeholder')"
          size="large"
          allow-clear
          max-length="50"
        >
        </a-input-password>
      </a-form-item>
      <a-form-item
        field="rePassword"
        :rules="[
          {
            required: true,
            message: $t('userCenter.SecuritySettings.form.password.rePassword.placeholder'),
          },
          {
            validator: (value, callback) => {
              if (value !== formPasswordData.newPassword) {
                callback($t('userCenter.SecuritySettings.form.password.error.rePassword.notequal'))
              } else {
                callback()
              }
            }
          }
        ]"
        :validate-trigger="['change', 'blur']"
        :label="$t('userCenter.SecuritySettings.form.password.rePassword.label')"
      >
        <a-input-password
          v-model="formPasswordData.rePassword"
          :placeholder="$t('userCenter.SecuritySettings.form.password.rePassword.placeholder')"
          size="large"
          allow-clear
          max-length="50"
        >
        </a-input-password>
      </a-form-item>
    </a-form>
  </a-modal>
  <a-list :bordered="false">
    <a-list-item>
      <a-list-item-meta>
        <template #avatar>
          <a-typography-paragraph>
            {{ $t('userCenter.SecuritySettings.label.password') }}
          </a-typography-paragraph>
        </template>
        <template #description>
          <div class="content">
            <a-typography-paragraph v-if="loginStore.pwdResetTime">
              已设置
            </a-typography-paragraph>
            <a-typography-paragraph v-else class="tip">
              {{ $t('userCenter.SecuritySettings.placeholder.password') }}
            </a-typography-paragraph>
          </div>
          <div class="operation">
            <a-link @click="handleClickUpdatePassword">
              {{ $t('userCenter.SecuritySettings.button.update') }}
            </a-link>
          </div>
        </template>
      </a-list-item-meta>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta>
        <template #avatar>
          <a-typography-paragraph>
            {{ $t('userCenter.SecuritySettings.form.label.phone') }}
          </a-typography-paragraph>
        </template>
        <template #description>
          <div class="content">
            <a-typography-paragraph v-if="loginStore.phone">
              已绑定：{{ loginStore.phone }}
            </a-typography-paragraph>
            <a-typography-paragraph v-else class="tip">
              {{ $t('userCenter.SecuritySettings.placeholder.phone') }}
            </a-typography-paragraph>
          </div>
          <div class="operation">
            <a-link>
              {{ $t('userCenter.SecuritySettings.button.update') }}
            </a-link>
          </div>
        </template>
      </a-list-item-meta>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta>
        <template #avatar>
          <a-typography-paragraph>
            {{ $t('userCenter.SecuritySettings.form.label.email') }}
          </a-typography-paragraph>
        </template>
        <template #description>
          <div class="content">
            <a-typography-paragraph v-if="loginStore.email">
              已绑定：{{ loginStore.email }}
            </a-typography-paragraph>
            <a-typography-paragraph v-else class="tip">
              {{ $t('userCenter.SecuritySettings.placeholder.email') }}
            </a-typography-paragraph>
          </div>
          <div class="operation">
            <a-link>
              {{ $t('userCenter.SecuritySettings.button.update') }}
            </a-link>
          </div>
        </template>
      </a-list-item-meta>
    </a-list-item>
  </a-list>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  import { useLoginStore } from '@/store';
  import { FormInstance } from "@arco-design/web-vue/es/form";
  import useLoading from "@/hooks/loading";
  import { updatePassword } from "@/api/system/user-center";
  import { Message } from "@arco-design/web-vue";
  import { encryptByRsa } from "@/utils/encrypt";

  const { loading, setLoading } = useLoading();
  const loginStore = useLoginStore();
  const formPasswordRef = ref<FormInstance>();
  const updatePasswordVisible = ref(false);
  const formPasswordData = reactive({
    oldPassword: '',
    newPassword: '',
    rePassword: '',
  });

  const handleClickUpdatePassword = () => {
    updatePasswordVisible.value = true;
  };

  const handleBeforeOkUpdatePassword = async () => {
    const errors = await formPasswordRef.value?.validate();
    if (loading.value) return false;
    if (errors) return false;
    setLoading(true);
    try {
      const res = await updatePassword({
        oldPassword: encryptByRsa(formPasswordData.oldPassword) || '',
        newPassword: encryptByRsa(formPasswordData.newPassword) || '',
      });
      if (res.success) Message.success(res.msg);
    } finally {
      setLoading(false);
    }
    return true;
  };

  const handleCancelUpdatePassword = () => {
    updatePasswordVisible.value = false;
    formPasswordRef.value?.resetFields()
  };
</script>

<style scoped lang="less">
  :deep(.arco-list-item) {
    border-bottom: none !important;
    .arco-typography {
      margin-bottom: 20px;
    }
    .arco-list-item-meta-avatar {
      margin-bottom: 1px;
    }
    .arco-list-item-meta {
      padding: 0;
    }
  }
  :deep(.arco-list-item-meta-content) {
    flex: 1;
    border-bottom: 1px solid var(--color-neutral-3);

    .arco-list-item-meta-description {
      display: flex;
      flex-flow: row;
      justify-content: space-between;

      .tip {
        color: rgb(var(--gray-6));
      }
      .operation {
        margin-right: 6px;
      }
    }
  }
</style>
