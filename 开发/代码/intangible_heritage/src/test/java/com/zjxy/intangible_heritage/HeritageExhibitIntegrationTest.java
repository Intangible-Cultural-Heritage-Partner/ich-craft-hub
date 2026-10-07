package com.zjxy.intangible_heritage;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.repository.HeritageWorkRepository;
import com.zjxy.intangible_heritage.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
@EnabledIfEnvironmentVariable(named = "HERITAGE_MYSQL_INTEGRATION", matches = "true")
class HeritageExhibitIntegrationTest {
    private static final String SCHEMA = "ich_exhibit_test_" + UUID.randomUUID().toString().replace("-", "");
    private static final Properties CONFIG = new Properties();
    private static boolean created;

    @Autowired private MockMvc mvc;
    @Autowired private UserRepository users;
    @Autowired private HeritageWorkRepository works;

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) throws Exception {
        try (var reader = new InputStreamReader(HeritageExhibitIntegrationTest.class.getResourceAsStream(
                "/application.properties"), StandardCharsets.UTF_8)) {
            CONFIG.load(reader);
        }
        try (var connection = connect(""); var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE `" + SCHEMA + "` CHARACTER SET utf8mb4");
            created = true;
        }
        registry.add("spring.datasource.url", () -> databaseUrl(SCHEMA));
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-only");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    private static String databaseUrl(String schema) {
        return "jdbc:mysql://localhost:3306/" + schema + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    }

    private static Connection connect(String schema) throws Exception {
        return DriverManager.getConnection(databaseUrl(schema), CONFIG.getProperty("spring.datasource.username"),
                CONFIG.getProperty("spring.datasource.password"));
    }

    @AfterAll
    static void removeIsolatedDatabase() throws Exception {
        if (created && SCHEMA.matches("ich_exhibit_test_[a-f0-9]{32}")) {
            try (var connection = connect(""); var statement = connection.createStatement()) {
                statement.execute("DROP DATABASE `" + SCHEMA + "`");
            }
        }
    }

    @Test
    @Transactional
    void publishAuditFilterEditAndRemoveModel() throws Exception {
        User craftsman = saveUser("integration_craftsman", "1");
        User admin = saveUser("integration_admin", "2");
        User other = saveUser("integration_other", "1");
        mvc.perform(get("/work/create").sessionAttr("loginUser", craftsman))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"category\"")));
        String redirect = mvc.perform(multipart("/work/create").sessionAttr("loginUser", craftsman)
                        .param("title", "分类验收木雕").param("category", "木雕")
                        .param("description", "隔离数据库中的流程验证资料")
                        .param("skillBackground", "木雕技艺背景")
                        .param("modelUrl", "/models/verification-only.glb"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        long id = Long.parseLong(redirect.substring(redirect.lastIndexOf('/') + 1));
        assertEquals(0, works.findById(id).orElseThrow().getAuditStatus());
        mvc.perform(get("/work/list").param("category", "木雕"))
                .andExpect(model().attribute("works", org.hamcrest.Matchers.empty()));
        mvc.perform(get(redirect)).andExpect(status().is3xxRedirection());
        mvc.perform(post("/work/" + id + "/edit").param("title", "Unauthorized")
                        .sessionAttr("loginUser", other)).andExpect(status().is3xxRedirection());
        assertEquals("分类验收木雕", works.findById(id).orElseThrow().getTitle());
        mvc.perform(post("/audit/doAudit").sessionAttr("loginUser", admin)
                        .param("type", "1").param("id", Long.toString(id)).param("status", "1"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/work/list").param("category", "木雕"))
                .andExpect(model().attribute("works", org.hamcrest.Matchers.hasSize(1)));
        mvc.perform(get("/work/list").param("category", "苏绣"))
                .andExpect(model().attribute("works", org.hamcrest.Matchers.empty()));
        mvc.perform(get(redirect)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("木雕技艺背景")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-model-url=\"/models/verification-only.glb\"")));
        mvc.perform(get("/work/" + id + "/edit").sessionAttr("loginUser", craftsman))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"removeModel\"")));
        mvc.perform(multipart("/work/" + id + "/edit").sessionAttr("loginUser", craftsman)
                        .param("title", "修改后的展品").param("category", "苏绣").param("removeModel", "true")
                        .param("modelUrl", "/models/verification-only.glb"))
                .andExpect(status().is3xxRedirection());
        HeritageWork changed = works.findById(id).orElseThrow();
        assertEquals("苏绣", changed.getCategory());
        assertNull(changed.getModelUrl());
        assertEquals(0, changed.getAuditStatus());
    }

    @Test
    void migrationIsDatabaseScopedAndRepeatable() throws Exception {
        String script = Files.readString(Path.of("../../数据库/migration/20261005_add_heritage_work_model_url.sql"));
        assertFalse(script.toUpperCase().contains("USE INTANGIBLE_HERITAGE_PLATFORM"), "迁移不能切换到业务数据库");
        String migrationSchema = SCHEMA + "_migration";
        try (var connection = connect(""); var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE `" + migrationSchema + "` CHARACTER SET utf8mb4");
        }
        try (var connection = connect(migrationSchema); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE heritage_work (id BIGINT PRIMARY KEY, title VARCHAR(100))");
            statement.execute("INSERT INTO heritage_work VALUES (1, 'preserved')");
            for (int repeat = 0; repeat < 2; repeat++) {
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) statement.execute(sql);
                }
            }
            try (var result = statement.executeQuery("SELECT title, model_url FROM heritage_work WHERE id = 1")) {
                assertTrue(result.next());
                assertEquals("preserved", result.getString("title"));
                assertNull(result.getString("model_url"));
            }
        } finally {
            try (var connection = connect(""); var statement = connection.createStatement()) {
                statement.execute("DROP DATABASE `" + migrationSchema + "`");
            }
        }
    }

    private User saveUser(String name, String role) {
        User user = new User();
        user.setUsername(name);
        user.setPassword("test-only-password");
        user.setPhone(name);
        user.setRole(role);
        return users.saveAndFlush(user);
    }
}
