package com.example.onlineshop;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlineshop.model.ActivityLog;
import com.example.onlineshop.model.Product;
import com.example.onlineshop.repository.LogRepository;
import com.example.onlineshop.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// 每个测试在事务中执行并回滚，不会在数据库里留下测试日志
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActivityLogTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProductService products;

    @Autowired
    private LogRepository logs;

    @Test
    void anonymousViewIsLogged() throws Exception {
        Product product = products.search("", "").get(0);

        mvc.perform(get("/product/{id}", product.id()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(product.name())));

        ActivityLog latest = logs.findRecent("VIEW", 1).get(0);
        assertThat(latest.productId()).isEqualTo(product.id());
        assertThat(latest.productName()).isEqualTo(product.name());
        assertThat(latest.userId()).isNull();
    }

    @Test
    void searchKeywordIsLogged() throws Exception {
        mvc.perform(get("/").param("q", "  日志测试关键词  "))
                .andExpect(status().isOk());

        assertThat(logs.findRecent("SEARCH", 1).get(0).detail()).isEqualTo("日志测试关键词");
    }

    @Test
    void blankSearchIsNotLogged() throws Exception {
        logs.insert(null, null, null, "SEARCH", "空搜索前的最后一条", "127.0.0.1");

        mvc.perform(get("/")).andExpect(status().isOk());

        assertThat(logs.findRecent("SEARCH", 1).get(0).detail()).isEqualTo("空搜索前的最后一条");
    }

    @Test
    void missingProductReturns404() throws Exception {
        mvc.perform(get("/product/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanFilterLogs() throws Exception {
        logs.insert(null, null, null, "SEARCH", "后台筛选测试", "127.0.0.1");

        mvc.perform(get("/admin/logs").param("action", "SEARCH"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("后台筛选测试")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotOpenLogs() throws Exception {
        mvc.perform(get("/admin/logs"))
                .andExpect(status().isForbidden());
    }
}
