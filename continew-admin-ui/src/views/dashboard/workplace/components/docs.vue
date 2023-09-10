<template>
  <a-card
    class="general-card"
    :title="$t('workplace.docs')"
    :header-style="{ paddingBottom: 0 }"
    :body-style="{ paddingTop: '10px', paddingBottom: '10px' }"
    style="height: 200px"
  >
    <template #extra>
      <a-link href="https://doc.charles7c.top" target="_blank" rel="noopener">{{
        $t('workplace.viewMore')
      }}</a-link>
    </template>
    <a-row>
      <a-col :span="12">
        <a-link
          href="https://doc.charles7c.top/guide/quickstart/intro.html"
          target="_blank"
          rel="noopener"
        >
          {{ $t('workplace.docs.intro') }}
        </a-link>
      </a-col>
      <a-col :span="12">
        <a-link
          href="https://doc.charles7c.top/guide/quickstart/"
          target="_blank"
          rel="noopener"
        >
          {{ $t('workplace.docs.userGuide') }}
        </a-link>
      </a-col>
      <a-col :span="12">
        <a-link
          href="https://doc.charles7c.top/faq.html"
          target="_blank"
          rel="noopener"
        >
          {{ $t('workplace.docs.faq') }}
        </a-link>
      </a-col>
      <a-col :span="12">
        <a-link
          href="https://doc.charles7c.top/other/changelog.html"
          target="_blank"
          rel="noopener"
        >
          {{ $t('workplace.docs.changelog') }}
        </a-link>
      </a-col>
      <a-col :span="12">
        <a-link
          href="https://doc.charles7c.top/require.html"
          target="_blank"
          rel="noopener"
        >
          {{ $t('workplace.docs.require') }}✨
        </a-link>
      </a-col>
      <a-col :span="12">
        <a-link
          href="https://blog.charles7c.top"
          target="_blank"
          rel="noopener"
        >
          {{ $t('workplace.docs.authorSite') }}👋
        </a-link>
      </a-col>
    </a-row>
  </a-card>
</template>

<script setup lang="ts"></script>

<style lang="less" scoped>
  .arco-card-body .arco-link {
    margin: 10px 0;
    color: rgb(var(--gray-8));
  }
</style>
