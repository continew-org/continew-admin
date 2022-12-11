# ContiNew-Admin 中后台管理框架

[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://github.com/Charles7c/continew-admin/blob/dev/LICENSE)
![SNAPSHOT](https://img.shields.io/badge/SNAPSHOT-v0.0.1-%23ff3f59.svg)

### 简介

ContiNew-Admin (incubating) 中后台管理框架，Continue New Admin，持续以最新流行技术栈构建。当前阶段采用的技术栈：Spring Boot、Undertow、Redis、Redisson、Hutool 等。

### 技术栈

| 名称                                                         | 版本           | 简介                                                         |
| :----------------------------------------------------------- | :------------- | :----------------------------------------------------------- |
| [Spring Boot](https://spring.io/projects/spring-boot)        | 2.7.6          | 简化新 Spring 应用的初始搭建以及开发过程。                   |
| [Undertow](https://undertow.io/)                             | 2.2.20.Final   | 采用 Java 开发的灵活的高性能 Web 服务器，提供包括阻塞和基于 NIO 的非堵塞机制。 |
| [Redis](https://redis.io/)                                   | 6.2.7          | 高性能的 key-value 数据库。                                  |
| [Redisson](https://github.com/redisson/redisson/wiki/Redisson%E9%A1%B9%E7%9B%AE%E4%BB%8B%E7%BB%8D) | 3.18.1         | 不仅仅是一个 Redis Java 客户端，同其他 Redis Java 客户端有着很大的区别，相比之下其他客户端提供的功能还仅仅停留在作为数据库驱动层面上，比如仅针对 Redis 提供连接方式，发送命令和处理返回结果等。而 Redisson 充分的利用了 Redis 键值数据库提供的一系列优势，基于 Java 实用工具包中常用接口，为使用者提供了一系列具有分布式特性的常用工具类。使得原本作为协调单机多线程并发程序的工具包获得了协调分布式多机多线程并发系统的能力，大大降低了设计和研发大规模分布式系统的难度。同时结合各富特色的分布式服务，更进一步简化了分布式环境中程序相互之间的协作。 |
| Easy Captcha                                                 | 1.6.2          | Java 图形验证码，支持 gif、中文、算术等类型，可用于 Java Web、JavaSE 等项目。 |
| [Knife4j](https://doc.xiaominfo.com/)                        | 4.0.0-SNAPSHOT | 前身是 swagger-bootstrap-ui，集 Swagger2 和 OpenAPI3 为一体的增强解决方案。本项目使用的是 [knife4j-openapi3-spring-boot-starter](https://gitee.com/xiaoym/swagger-bootstrap-ui-demo/tree/master/knife4j-springdoc-openapi-demo) 基于 OpenAPI3 规范，在 Spring Boot < 3.0.0-M1 的单体架构下可以直接引用此 starter，该模块包含了 UI 部分，底层基于 springdoc-openapi 项目。 |
| [Hutool](https://www.hutool.cn/)                             | 5.8.10         | 小而全的 Java 工具类库，通过静态方法封装，降低相关 API 的学习成本，提高工作效率，使 Java 拥有函数式语言般的优雅，让 Java 语言也可以“甜甜的”。 |
| [Lombok](https://projectlombok.org/)                         | 1.18.24        | 在 Java 开发过程中用注解的方式，简化了 JavaBean 的编写，避免了冗余和样板式代码，让编写的类更加简洁。 |

### 项目结构

采用按功能拆分模块的开发方式，项目目录结构如下：

> 下方项目目录结构是按照模块的层次顺序进行介绍的，实际 IDE 中 `continew-admin-common` 模块会因为字母排序原因排在上方。
>

```
continew-admin  全局通用项目配置及依赖版本管理
  ├─continew-admin-webapi  API 模块（存放 Controller 层代码，打包部署的模块）
  │  ├─src
  │  │  ├─main
  │  │  │  ├─java       工程源文件代码目录
  │  │  │  │  └─top
  │  │  │  │    └─charles7c
  │  │  │  │      └─cnadmin
  │  │  │  │        └─webapi
  │  │  │  │          └─controller  
  │  │  │  │            └─auth    认证相关 API
  │  │  │  │      └─ContinewAdminApplication.java  启动入口
  │  │  │  ├─resources  工程配置目录
  ├─continew-admin-system  系统管理模块（存放系统管理模块相关功能，例如：部门管理、角色管理、用户管理等）
  │  ├─src
  │  │  ├─main
  │  │  │  ├─java       工程源文件代码目录
  │  │  │  │  └─top
  │  │  │  │    └─charles7c
  │  │  │  │      └─cnadmin
  │  │  │  │        └─auth    认证相关业务及配置
  │  │  │  │          └─config       认证相关配置
  │  │  │  │            └─properties   认证相关配置属性
  │  │  │  │          └─model        认证相关模型
  │  │  │  │            └─vo           认证相关 VO（View Object）
  ├─continew-admin-common  公共模块（存放公共工具类，公共配置等）
  │  ├─src
  │  │  ├─main
  │  │  │  ├─java       工程源文件代码目录
  │  │  │  │  └─top
  │  │  │  │    └─charles7c
  │  │  │  │      └─cnadmin
  │  │  │  │        └─common
  │  │  │  │          └─config       公共配置
  │  │  │  │            └─jackson      Jackson 配置
  │  │  │  │            └─properties   公共配置属性
  │  │  │  │          └─model        公共模型
  │  │  │  │            └─vo           公共 VO（View Object）
  │  │  │  │          └─util         公共工具类
```

### License

- 遵循 [Apache-2.0](https://github.com/Charles7c/continew-admin/blob/dev/LICENSE) 开源许可协议
- Copyright © 2022-present Charles7c