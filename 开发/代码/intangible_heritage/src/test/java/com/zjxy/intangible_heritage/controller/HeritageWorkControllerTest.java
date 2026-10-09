package com.zjxy.intangible_heritage.controller;

import com.zjxy.intangible_heritage.entity.HeritageWork;
import com.zjxy.intangible_heritage.entity.User;
import com.zjxy.intangible_heritage.service.HeritageWorkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HeritageWorkControllerTest {
    private HeritageWorkService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(HeritageWorkService.class);
        mvc = MockMvcBuilders.standaloneSetup(new HeritageWorkController(service)).build();
    }

    @Test
    void publicListPassesCategoryAndDisplaysSelection() throws Exception {
        when(service.findAll("木雕")).thenReturn(List.of());
        mvc.perform(get("/work/list").param("category", "木雕"))
                .andExpect(status().isOk()).andExpect(view().name("work/list"))
                .andExpect(model().attribute("selectedCategory", "木雕"));
        verify(service).findAll("木雕");
        verify(service).findCategories(null);
    }

    @Test
    void personalListUsesLoggedInOwner() throws Exception {
        mvc.perform(get("/work/mine").param("category", "苏绣").sessionAttr("loginUser", owner()))
                .andExpect(status().isOk());
        verify(service).findByCraftsman(10L, "苏绣");
        verify(service).findCategories(10L);
    }

    @Test
    void createBindsCategory() throws Exception {
        HeritageWork saved = new HeritageWork();
        saved.setId(100L);
        when(service.create(any(), anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(saved);
        mvc.perform(multipart("/work/create").param("title", "Title").param("category", "木雕")
                        .sessionAttr("loginUser", owner()))
                .andExpect(redirectedUrl("/work/100"));
        verify(service).create(any(), eq("Title"), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), eq("木雕"));
    }

    @Test
    void editFormIncludesLegacyCategory() throws Exception {
        HeritageWork work = new HeritageWork();
        work.setId(100L);
        work.setCraftsman(owner());
        work.setCategory("缂丝");
        when(service.findById(100L)).thenReturn(Optional.of(work));
        mvc.perform(get("/work/100/edit").sessionAttr("loginUser", owner()))
                .andExpect(model().attribute("categoryOptions", org.hamcrest.Matchers.hasItem("缂丝")));
    }

    private User owner() {
        User owner = new User();
        owner.setId(10L);
        owner.setRole("1");
        return owner;
    }
}
