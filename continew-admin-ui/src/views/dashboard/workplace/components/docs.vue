<template>
  <a-card
    class="general-card"
    :title="$t('workplace.docs')"
    :header-style="{ paddingBottom: 0 }"
    :body-style="{ paddingTop: 0 }"
    style="height: 166px"
  >
    <template #extra>
      <a-link href="https://doc.charles7c.top" target="_blank" rel="noopener">{{ $t('workplace.viewMore') }}</a-link>
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
    </a-row>
  </a-card>
</template>

<style lang="less" scoped>
  .arco-card-body .arco-link {
    margin: 10px 0;
    color: rgb(var(--gray-8));
  }
</style>
