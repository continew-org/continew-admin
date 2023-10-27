<template>
  <a-list-item-meta>
    <template #avatar>
      <a-typography-paragraph>
        {{ $t('userCenter.securitySettings.phone.label') }}
      </a-typography-paragraph>
    </template>
    <template #description>
      <div class="tip">
        {{ $t('userCenter.securitySettings.phone.tip') }}
      </div>
      <div class="content">
        <a-typography-paragraph v-if="userStore.phone">
          {{ userStore.phone }}
        </a-typography-paragraph>
        <a-typography-paragraph v-else class="tip">
          {{ $t('userCenter.securitySettings.phone.content') }}
        </a-typography-paragraph>
      </div>
      <div class="operation">
        <a-link
          :title="$t('userCenter.securitySettings.button.update')"
          @click="toUpdate"
        >
          {{ $t('userCenter.securitySettings.button.update') }}
        </a-link>
      </div>
    </template>
  </a-list-item-meta>

  <a-modal
    :title="$t('userCenter.securitySettings.updatePhone.modal.title')"
    :visible="visible"
    :mask-closable="false"
    :esc-to-close="false"
    @ok="handleUpdate"
    @cancel="handleCancel"
  >
    <a-form ref="formRef" :model="form" :rules="rules" size="large">
      <a-form-item
        :label="
          $t('userCenter.securitySettings.updatePhone.form.label.newPhone')
        "
        field="newPhone"
      >
        <a-input
          v-model="form.newPhone"
          :placeholder="
            $t(
              'userCenter.securitySettings.updatePhone.form.placeholder.newPhone'
            )
          "
          allow-clear
        />
      </a-form-item>
      <a-form-item
        :label="
          $t('userCenter.securitySettings.updatePhone.form.label.captcha')
        "
        field="captcha"
      >
        <a-input
          v-model="form.captcha"
          :placeholder="
            $t('userCenter.securitySettings.form.placeholder.captcha')
          "
          :max-length="4"
          allow-clear
          style="width: 80%"
        />
        <a-button
          :loading="captchaLoading"
          type="primary"
          :disabled="captchaDisable"
          class="captcha-btn"
          @click="handleSendCaptcha"
        >
          {{ captchaBtnName }}
        </a-button>
      </a-form-item>
      <a-form-item
        :label="
          $t(
            'userCenter.securitySettings.updatePhone.form.label.currentPassword'
          )
        "
        field="currentPassword"
      >
        <a-input-password
          v-model="form.currentPassword"
          :placeholder="
            $t(
              'userCenter.securitySettings.updatePhone.form.placeholder.currentPassword'
            )
          "
          :max-length="32"
          allow-clear
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts" setup>
  import { getCurrentInstance, ref, reactive, computed } from 'vue';
  import { FieldRule } from '@arco-design/web-vue';
  import { getSmsCaptcha } from '@/api/common/captcha';
  import { UserPhoneUpdateReq, updatePhone } from '@/api/system/user-center';
  import { useI18n } from 'vue-i18n';
  import { useUserStore } from '@/store';
  import { encryptByRsa } from '@/utils/encrypt';

  const { proxy } = getCurrentInstance() as any;
  const { t } = useI18n();
  const userStore = useUserStore();
  const captchaTime = ref(60);
  const captchaTimer = ref();
  const captchaLoading = ref(false);
  const captchaDisable = ref(true);
  const visible = ref(false);
  const captchaBtnNameKey = ref('userCenter.securitySettings.captcha.get');
  const captchaBtnName = computed(() => t(captchaBtnNameKey.value));

  // 表单数据
  const form = reactive<UserPhoneUpdateReq>({
    newPhone: '',
    captcha: '',
    currentPassword: '',
  });
  // 表单验证规则
  const rules = computed((): Record<string, FieldRule[]> => {
    return {
      newPhone: [
        {
          required: true,
          message: t(
            'userCenter.securitySettings.updatePhone.form.error.required.newPhone'
          ),
        },
        {
          match: /^1[3-9]\d{9}$/,
          message: t(
            'userCenter.securitySettings.updatePhone.form.error.match.newPhone'
          ),
        },
      ],
      captcha: [
        {
          required: true,
          message: t('userCenter.securitySettings.form.error.required.captcha'),
        },
      ],
      currentPassword: [
        {
          required: true,
          message: t(
            'userCenter.securitySettings.updatePhone.form.error.required.currentPassword'
          ),
        },
      ],
    };
  });

  /**
   * 重置验证码
   */
  const resetCaptcha = () => {
    window.clearInterval(captchaTimer.value);
    captchaTime.value = 60;
    captchaBtnNameKey.value = 'userCenter.securitySettings.captcha.get';
    captchaDisable.value = false;
  };

  /**
   * 发送验证码
   */
  const handleSendCaptcha = () => {
    if (captchaLoading.value) return;
    proxy.$refs.formRef.validateField('newPhone', (valid: any) => {
      if (!valid) {
        captchaLoading.value = true;
        captchaBtnNameKey.value = 'userCenter.securitySettings.captcha.ing';
        getSmsCaptcha(form.newPhone)
          .then((res) => {
            captchaLoading.value = false;
            captchaDisable.value = true;
            captchaBtnNameKey.value = `${t(
              'userCenter.securitySettings.captcha.get'
            )}(${(captchaTime.value -= 1)}s)`;
            captchaTimer.value = window.setInterval(() => {
              captchaTime.value -= 1;
              captchaBtnNameKey.value = `${t(
                'userCenter.securitySettings.captcha.get'
              )}(${captchaTime.value}s)`;
              if (captchaTime.value <= 0) {
                resetCaptcha();
              }
            }, 1000);
            proxy.$message.success(res.msg);
          })
          .catch(() => {
            resetCaptcha();
            captchaLoading.value = false;
          });
      }
    });
  };

  /**
   * 取消
   */
  const handleCancel = () => {
    visible.value = false;
    proxy.$refs.formRef.resetFields();
    resetCaptcha();
  };

  /**
   * 修改
   */
  const handleUpdate = () => {
    proxy.$refs.formRef.validate((valid: any) => {
      if (!valid) {
        updatePhone({
          newPhone: form.newPhone,
          captcha: form.captcha,
          currentPassword: encryptByRsa(form.currentPassword) || '',
        }).then((res) => {
          handleCancel();
          userStore.getInfo();
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

<style scoped lang="less">
  .captcha-btn {
    margin-left: 5px;
  }
</style>
