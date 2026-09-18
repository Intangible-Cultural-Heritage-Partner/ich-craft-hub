# Heritage Work Module Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the complete member B heritage-work module in the existing Spring Boot + JPA + Thymeleaf application.

**Architecture:** Add a `HeritageWork` JPA entity, repository, service, and controller. Keep server-rendered Thymeleaf pages and the existing session-based `loginUser` authentication model. Store uploaded images under `uploads/heritage`, expose them through a resource handler, and fall back to submitted image URLs when no file is uploaded.

**Tech Stack:** Java 17, Spring Boot 3.3.5, Spring MVC, Spring Data JPA, Thymeleaf, MySQL, JUnit 5, Mockito.

---

## File Map

- Create `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/entity/HeritageWork.java` for the `heritage_work` persistence model.
- Create `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/repository/HeritageWorkRepository.java` for ordered public and owner queries.
- Create `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/service/HeritageWorkService.java` and `HeritageWorkServiceImpl.java` for validation, ownership, and image-path handling.
- Create `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/controller/HeritageWorkController.java` for public routes, craftsman routes, session checks, and multipart form binding.
- Modify `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/controller/IndexController.java` only if existing placeholder work routes conflict with the new controller.
- Create or modify `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/config/WebConfig.java` to expose `/uploads/**`.
- Create `开发/代码/intangible_heritage/src/main/resources/templates/work/form.html` for create/edit forms.
- Replace or modify `开发/代码/intangible_heritage/src/main/resources/templates/work/list.html` for database-backed cards and craftsman actions.
- Create `开发/代码/intangible_heritage/src/main/resources/templates/work/detail.html` for detail content and module links.
- Modify `开发/代码/intangible_heritage/src/main/resources/templates/user/userCenter.html` to add the current user's work link and work list.
- Modify `开发/代码/intangible_heritage/src/main/resources/templates/fragments/nav.html` to make the work module reachable from shared navigation.
- Modify `开发/数据库/表.sql` to include the `heritage_work` table definition matching the entity.
- Create focused tests under `开发/代码/intangible_heritage/src/test/java/com/zjxy/intangible_heritage/service/HeritageWorkServiceTest.java`.

### Task 1: Add failing service tests

**Files:**
- Create: `开发/代码/intangible_heritage/src/test/java/com/zjxy/intangible_heritage/service/HeritageWorkServiceTest.java`

- [ ] **Step 1: Test craftsman-only creation.** Mock `HeritageWorkRepository`, construct a craftsman `User` with role `CRAFTSMAN`, call the service create method with a title, and assert the saved entity carries that user and a default pending audit status. Add a second test with role `USER` and assert `IllegalStateException`.
- [ ] **Step 2: Test ownership enforcement.** Stub a work owned by user 10, call the service update method as user 11, and assert `IllegalStateException`; call as user 10 and assert repository save receives updated title and content.
- [ ] **Step 3: Test URL fallback and uploaded-file priority.** Use a non-empty `MockMultipartFile` and an empty file to assert the service stores a generated `/uploads/heritage/...` path for the non-empty file and the submitted URL for the empty file.
- [ ] **Step 4: Run the focused test before implementation.** Run `./mvnw.cmd -Dtest=HeritageWorkServiceTest test`; it must fail because the service classes do not exist yet.

### Task 2: Implement the persistence model

**Files:**
- Create: `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/entity/HeritageWork.java`
- Create: `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/repository/HeritageWorkRepository.java`

- [ ] **Step 1: Add entity fields.** Map `id`, `craftsman_id`, `title`, `cover_img`, `image_list`, `description`, `craft_background`, `audit_status`, `audit_remark`, and `create_time` with JPA annotations and JavaBean accessors. Map the owner as `@ManyToOne(fetch = FetchType.LAZY)` to the existing `User` entity.
- [ ] **Step 2: Add repository queries.** Define `findAllByOrderByCreateTimeDesc()`, `findByCraftsmanIdOrderByCreateTimeDesc(Long craftsmanId)`, and `Optional<HeritageWork> findByIdAndCraftsmanId(Long id, Long craftsmanId)`.
- [ ] **Step 3: Run compilation.** Run `./mvnw.cmd -DskipTests compile`; it must compile the new entity and repository.

### Task 3: Implement image and business services

**Files:**
- Create: `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/service/HeritageWorkService.java`
- Create: `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/service/HeritageWorkServiceImpl.java`

- [ ] **Step 1: Define service contract.** Expose list, find-by-id, list-by-owner, create, and update operations. Create/update accept the authenticated `User`, form fields, `MultipartFile` cover/image files, and URL fallbacks.
- [ ] **Step 2: Enforce role and ownership.** Accept only role `CRAFTSMAN` for create/update; require the loaded work owner ID to equal the authenticated user ID before update. Reject blank titles with `IllegalArgumentException`.
- [ ] **Step 3: Normalize image inputs.** Save non-empty files using UUID-prefixed safe names under `uploads/heritage`; otherwise keep a trimmed URL. Split image URL text on commas or line breaks, trim blanks, and persist the normalized comma-separated value.
- [ ] **Step 4: Run service tests.** Run `./mvnw.cmd -Dtest=HeritageWorkServiceTest test`; expected result is all focused tests passing.

### Task 4: Add upload resource mapping and controller routes

**Files:**
- Create: `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/config/WebConfig.java`
- Create: `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/controller/HeritageWorkController.java`
- Modify: `开发/代码/intangible_heritage/src/main/java/com/zjxy/intangible_heritage/controller/IndexController.java`

- [ ] **Step 1: Map uploaded files.** Register a `ResourceHandler` for `/uploads/**` pointing to the normalized application working-directory `uploads/` path.
- [ ] **Step 2: Add public routes.** Implement `GET /work/list` with all works and `GET /work/{id}` with one work; return a not-found redirect/message when no entity exists.
- [ ] **Step 3: Add craftsman routes.** Implement `GET/POST /work/create`, `GET/POST /work/{id}/edit`, and `GET /work/mine`; resolve `loginUser` from session and redirect non-craftsmen with a clear message. Ensure create/edit templates receive a reusable empty form object and validation messages.
- [ ] **Step 4: Remove route collision.** Delete the old placeholder `/work/list` method from `IndexController` if the new controller owns that mapping; leave unrelated placeholder routes unchanged.
- [ ] **Step 5: Run context tests.** Run `./mvnw.cmd test`; expected result is the Spring context starting without duplicate route mappings.

### Task 5: Build Thymeleaf pages and navigation

**Files:**
- Create: `开发/代码/intangible_heritage/src/main/resources/templates/work/form.html`
- Create: `开发/代码/intangible_heritage/src/main/resources/templates/work/detail.html`
- Modify: `开发/代码/intangible_heritage/src/main/resources/templates/work/list.html`
- Modify: `开发/代码/intangible_heritage/src/main/resources/templates/user/userCenter.html`
- Modify: `开发/代码/intangible_heritage/src/main/resources/templates/fragments/nav.html`

- [ ] **Step 1: Implement form.** Add multipart form fields for title, cover file/URL, multiple image files/URLs, description, and craft background; show errors and use the same template for create/edit.
- [ ] **Step 2: Implement list.** Render title, cover, craftsman nickname, summary, creation time, detail link, and craftsman-only create/mine/edit actions. Show a useful empty state.
- [ ] **Step 3: Implement detail.** Render cover, all gallery images, description, craft background, author information, and links to `/tutorial/list` and `/userWork/share`.
- [ ] **Step 4: Extend personal center.** Add a “我的非遗展品” link and render the current craftsman’s works when the controller supplies them; keep ordinary-user content usable.
- [ ] **Step 5: Extend shared navigation.** Add a stable “非遗展品” link and role-aware create/mine links without breaking existing login/logout links.

### Task 6: Update SQL and verify end-to-end behavior

**Files:**
- Modify: `开发/数据库/表.sql`

- [ ] **Step 1: Add matching DDL.** Add `DROP TABLE IF EXISTS heritage_work` and a `CREATE TABLE` with the entity fields, `craftsman_id` foreign key to `user.id`, default audit status, and `utf8mb4` encoding. Place it after `user` and before dependent content tables.
- [ ] **Step 2: Run formatting and tests.** Run `./mvnw.cmd test` and inspect the complete output for failures; run `git diff --check` for whitespace errors.
- [ ] **Step 3: Review requirement coverage.** Confirm routes, local upload priority, URL fallback, owner-only edit, list/detail pages, personal-center access, and tutorial/user-work links against the design acceptance criteria.
- [ ] **Step 4: Report actual verification.** Summarize changed files and exact test/build results, including any pre-existing environment limitation such as unavailable MySQL.
