<template>
  <a-layout-footer class="footer">
    {{ `Copyright © 2022-${new Date().getFullYear()}` }}&nbsp;
    <a
      href="https://blog.charles7c.top/about/me"
      target="_blank"
      rel="noopener"
    >
      Charles7c
    </a>
    <span>&nbsp;⋅&nbsp;</span>
    <a
      href="https://github.com/Charles7c/continew-admin"
      target="_blank"
      rel="noopener"
      >{{ $t('title') }}</a
    >&nbsp; v1.1.2
    <span>&nbsp;⋅&nbsp;</span>
    <a href="https://beian.miit.gov.cn" target="_blank" rel="noopener">
      津ICP备2022005864号-2
    </a>
  </a-layout-footer>
</template>

<script lang="ts" setup></script>

<style lang="less" scoped>
  .footer {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 40px;
    color: var(--color-text-2);
    text-align: center;
  }

  a {
    text-decoration: none;
    color: var(--color-text-2);
  }

  a:hover {
    color: rgb(var(--gray-6));
  }
</style>
