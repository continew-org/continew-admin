<template>
  <a-card class="general-card" :title="$t('realTimeMonitor.title.studioInfo')">
    <a-form :model="{}" layout="vertical">
      <a-form-item
        :label="$t('realTimeMonitor.studioInfo.label.studioTitle')"
        required
      >
        <a-input
          :placeholder="`王立群${$t(
            'realTimeMonitor.studioInfo.placeholder.studioTitle'
          )}`"
        />
      </a-form-item>
      <a-form-item
        :label="$t('realTimeMonitor.studioInfo.label.onlineNotification')"
        required
      >
        <a-textarea />
      </a-form-item>
      <a-form-item
        :label="$t('realTimeMonitor.studioInfo.label.studioCategory')"
        required
      >
        <a-input-search />
      </a-form-item>
      <a-form-item
        :label="$t('realTimeMonitor.studioInfo.label.studioCategory')"
        required
      >
        <a-input-search />
      </a-form-item>
    </a-form>
    <a-button type="primary">{{
      $t('realTimeMonitor.studioInfo.btn.fresh')
    }}</a-button>
  </a-card>
</template>

<script lang="ts" setup></script>
