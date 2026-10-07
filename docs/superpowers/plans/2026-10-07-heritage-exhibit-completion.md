# 非遗展示收尾 Implementation Plan

**Goal:** 补齐已确认的分类与展示体验，记录真实模型及部署验收边界。

**Architecture:** 沿用 Spring MVC/Thymeleaf/JPA 和 Three.js，服务层维护分类查询、权限及校验。

**Tech Stack:** Java 17、Spring Boot 3.3.5、MySQL 8、JUnit/Mockito、浏览器 JavaScript。

## 分类
- [x] 执行 HeritageWorkServiceTest 基线：10 项通过。
- [x] 添加分类保存、保留、查询与校验失败测试。
- [x] 修改 HeritageWorkService、HeritageWorkServiceImpl、HeritageWorkRepository，提供分类重载与查询。
- [x] 修改 HeritageWorkController、work/form.html、work/list.html，增加选择、回显和筛选；补 MVC 测试。
- [x] 执行 Maven 定向测试。

## 真实模型
- [ ] 核实资源来源、许可证和非遗关联；具备条件才接入，否则明确记录待办。

当前无可核实授权及非遗关联的真实 GLB，此项保持待办；现有测试文件和程序生成模型不作为真实资源验收依据。

## 展示体验
- [x] 为动画暂停、图片错误恢复、中文进度、模型移除添加失败测试。
- [x] 修改 work/detail.html 生命周期、错误提示、进度；接通 removeModel 参数及冲突校验，不删除文件。
- [x] 执行新增回归测试。

## 验证与文档
- [x] 用 information_schema 检查及条件 DDL 替换不兼容的迁移语法。
- [x] 执行完整 Maven 测试、打包及尽可能充分的隔离环境浏览器验收。
- [x] 同步功能、设计、模型说明及本计划，记录未验证项，检查差异，不提交。

最终复验：26 项 Java 测试、4 项 Chromium 回归通过，Maven package 退出码为 0。浏览器渲染使用计数桩，真实模型和实际 GPU 验收尚未完成。详见 `文档/非遗展示验收记录-2026-10-07.md`。
