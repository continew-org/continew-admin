# ContiNew-Admin 中后台管理框架

[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://github.com/Charles7c/continew-admin/blob/dev/LICENSE)
![SNAPSHOT](https://img.shields.io/badge/SNAPSHOT-v0.0.1-%23ff3f59.svg)

### 简介

ContiNew-Admin (incubating) 中后台管理框架，Continue New Admin，持续以最新流行技术栈构建。当前阶段采用的技术栈：Spring Boot、Undertow 等。

### 技术栈

| 名称                                                  | 版本         | 简介                                                         |
| :---------------------------------------------------- | :----------- | :----------------------------------------------------------- |
| [Spring Boot](https://spring.io/projects/spring-boot) | 2.7.6        | 简化新 Spring 应用的初始搭建以及开发过程。                   |
| [Undertow](https://undertow.io/)                      | 2.2.20.Final | 采用 Java 开发的灵活的高性能 Web 服务器，提供包括阻塞和基于 NIO 的非堵塞机制。 |
| [Lombok](https://projectlombok.org/)                  | 1.18.24      | 在 Java 开发过程中用注解的方式，简化了 JavaBean 的编写，避免了冗余和样板式代码，让编写的类更加简洁。 |

### 项目结构

采用按功能拆分模块的开发方式，项目结构如下：

> 项目结构按模块的层次顺序介绍，实际 IDE 中 `continew-admin-common` 模块会因为字母排序原因排在上方
>

```
continew-admin  全局通用项目配置及依赖版本管理
  ├─continew-admin-webapi  API 模块（存放 Controller 层代码，打包部署的模块）
  ├─continew-admin-system  系统管理模块（存放系统管理模块相关功能，例如：部门管理、角色管理、用户管理等）
  ├─continew-admin-common  公共模块（存放公共工具类，公共配置等）
```

### License

- 遵循 [Apache-2.0](https://github.com/Charles7c/continew-admin/blob/dev/LICENSE) 开源许可协议
- Copyright © 2022-present Charles7c