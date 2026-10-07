# 非遗展品详情与 3D 展示 Implementation Plan

> 历史计划保留原任务清单，不追认未记录的历史测试步骤。2026-10-07 收尾进展见 `2026-10-07-heritage-exhibit-completion.md`，实际验证见 `文档/非遗展示验收记录-2026-10-07.md`。当前无模型时采用明确标注的演示模型，与本历史计划中的空状态方案有所调整。

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将非遗展品详情页从二维 3D 示意升级为可加载真实 GLB/GLTF 模型的交互式展示，并补齐模型数据、上传入口、迁移脚本和使用说明。

**Architecture:** Spring Boot/JPA 保存可空的模型地址，服务层负责 URL 清洗和 GLB/GLTF 文件上传；Thymeleaf 详情页按需初始化 Three.js 查看器，模型缺失或加载失败时保留图片展廊与明确降级状态。模型资源不嵌入数据库，文件保存到现有 uploads 目录，外部 GLTF 资源由调用方保证相对资源可达。

**Tech Stack:** Java 17, Spring Boot 3.3.5, JPA, Thymeleaf, MySQL 8, Three.js CDN modules, GLB/GLTF.

---

### Task 1: Add model data and migration

**Files:**
- Modify: `ich-craft-hub/开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/entity/HeritageWork.java`
- Modify: `ich-craft-hub/开发/数据库/表.sql`
- Create: `ich-craft-hub/开发/数据库/migration/20261005_add_heritage_work_model_url.sql`
- Test: `ich-craft-hub/开发/代码/intangible_heritage/src/test/java/com/zjxy/intangible_heritage/service/HeritageWorkServiceTest.java`

- [ ] **Step 1: Write the failing model-field test**
  Add a test that creates a work with a model URL through the new service overload and asserts `saved.getModelUrl()` equals the trimmed URL. Add a second assertion that a blank model URL is stored as `null`.
- [ ] **Step 2: Run the focused test and verify it fails**
  Run ` .\mvnw.cmd -Dtest=HeritageWorkServiceTest test` from the application directory. Expected result: compilation failure because `modelUrl` and the new service overload do not exist yet.
- [ ] **Step 3: Add the nullable JPA field**
  Add `@Column(name = "model_url", length = 500) private String modelUrl;` and its accessors to `HeritageWork`.
- [ ] **Step 4: Add the schema and migration column**
  Add `model_url VARCHAR(500) DEFAULT NULL COMMENT 'GLB/GLTF模型地址'` to `heritage_work` in `表.sql`, and create a standalone `ALTER TABLE` migration without deleting existing rows.
- [ ] **Step 5: Re-run the focused test**
  Confirm the entity and schema names agree; the service behavior becomes green in Task 2.

### Task 2: Persist model URL and uploaded model files

**Files:**
- Modify: `ich-craft-hub/开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/service/HeritageWorkService.java`
- Modify: `ich-craft-hub/开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/service/HeritageWorkServiceImpl.java`
- Modify: `ich-craft-hub/开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/controller/HeritageWorkController.java`
- Test: `ich-craft-hub/开发/代码/intangible_heritage/src/test/java/com/zjxy/intangible_heritage/service/HeritageWorkServiceTest.java`

- [ ] **Step 1: Add failing upload-path tests**
  Test an uploaded `model.glb` path under `/uploads/heritage/models/` and an unsupported `.fbx` rejection before repository save.
- [ ] **Step 2: Run focused tests and confirm RED**
  Run ` .\mvnw.cmd -Dtest=HeritageWorkServiceTest test`; expected failure is the missing overload or validation behavior.
- [ ] **Step 3: Extend service methods without breaking existing callers**
  Add overloads accepting `String modelUrl` and `MultipartFile modelFile`; keep existing overloads delegating with null model values. Upload files take priority over URL, and blank values become null.
- [ ] **Step 4: Validate model files safely**
  Accept only `.glb` and `.gltf`, sanitize the extension, save under `uploads/heritage/models`, and never interpret user input as a filesystem path.
- [ ] **Step 5: Wire create/edit controller parameters**
  Add optional `modelFile` and `modelUrl` request parameters to create/edit and preserve values when returning the form after an error.
- [ ] **Step 6: Run service tests and verify GREEN**
  Run ` .\mvnw.cmd -Dtest=HeritageWorkServiceTest test`; existing and new URL/upload/validation tests must pass.

### Task 3: Add model fields to the publish/edit form

**Files:**
- Modify: `ich-craft-hub/开发/代码/intangible_heritage/src/main/resources/templates/work/form.html`

- [ ] **Step 1: Add URL and file inputs**
  Add a URL input bound to `work.modelUrl`, a `modelFile` input accepting `.glb` and `.gltf`, and a hint that GLB is preferred and uploaded files take priority.
- [ ] **Step 2: Add edit-state visibility**
  Display the current model URL when editing and explain how a new URL/file replaces it.
- [ ] **Step 3: Verify UTF-8 template rendering**
  Request the form after starting the app as a craftsman and confirm the model fields and Chinese text contain no replacement characters.

### Task 4: Replace the 3D placeholder with a real viewer

**Files:**
- Modify: `ich-craft-hub/开发/代码/intangible_heritage/src/main/resources/templates/work/detail.html`
- Create: `ich-craft-hub/开发/代码/intangible_heritage/src/main/resources/static/models/README.md`

- [ ] **Step 1: Add viewer markup and states**
  Replace the perspective placeholder with a canvas mount, loading progress, empty, error, WebGL-unsupported and ready states. Pass the model URL using an escaped Thymeleaf data attribute.
- [ ] **Step 2: Load pinned Three.js modules on demand**
  Initialize Three.js and `GLTFLoader` only when the 3D tab is activated; blank URLs use the empty state.
- [ ] **Step 3: Implement controls**
  Add scene/camera/lights/renderer/orbit controls/model normalization, drag rotation, wheel/touch zoom, reset camera, fullscreen, resize observation and animation pause.
- [ ] **Step 4: Implement failures and cleanup**
  Show Chinese errors for failed resources or unsupported WebGL and dispose renderer resources, controls, observer and animation frame when leaving the page.
- [ ] **Step 5: Document local model placement**
  Explain `.glb` preference, external `.bin`/texture requirements and upload/URL options in the static models README.

### Task 5: Write feature documentation

**Files:**
- Create: `ich-craft-hub/文档/非遗展品展示功能说明.md`

- [ ] **Step 1: Document capabilities**
  Describe gallery, tab switching, enlarged preview, 3D controls, loading states, accessibility and fallback behavior.
- [ ] **Step 2: Document database and deployment**
  Include migration path, MySQL command, Java 17 startup command, UTF-8 requirement, CDN/network requirement and model format rules.
- [ ] **Step 3: Document troubleshooting**
  Explain empty/404/unsupported/WebGL cases and distinguish a future verified heritage model from a generic test model.

### Task 6: Build and verify end to end

**Files:**
- No source changes unless verification finds a direct defect.

- [ ] **Step 1: Run all tests**
  Run ` .\mvnw.cmd test` with Java 17 and require exit code zero.
- [ ] **Step 2: Build the runnable JAR**
  Run ` .\mvnw.cmd package -DskipTests` and confirm the target JAR exists.
- [ ] **Step 3: Start with UTF-8**
  Run the JAR with `-Dfile.encoding=UTF-8` and the configured MySQL password; verify Spring Boot reaches its started message.
- [ ] **Step 4: Verify HTTP output**
  Request `/work/list` and a known `/work/{id}`; confirm HTTP 200, Chinese text, 3D controls, model-state labels and no replacement characters.
- [ ] **Step 5: Record external dependency**
  If no real non-heritage GLB/GLTF is supplied, report that the viewer is implemented while final visual acceptance still needs the user’s verified model asset.
