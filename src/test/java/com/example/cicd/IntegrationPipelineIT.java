package com.example.cicd;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationPipelineIT {

    @Autowired MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    private static final String USER = "ci-user";

    @Test
    @Order(1)
    @DisplayName("проверка работоспособности")
    void health() throws Exception {
        mockMvc.perform(get("/api/catalog/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @Order(2)
    @DisplayName("поиск в каталоге")
    void catalogSearch() throws Exception {
        mockMvc.perform(get("/api/catalog/search").param("name", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", containsString("Laptop")));
    }

    @Test
    @Order(3)
    @DisplayName("полный путь: поиск -> корзина -> оформление -> остаток")
    void fullCheckoutPath() throws Exception {
        MvcResult search = mockMvc.perform(get("/api/catalog/search").param("name", "Mouse"))
                .andExpect(status().isOk())
                .andReturn();

        long productId = objectMapper.readTree(search.getResponse().getContentAsString())
                .get(0).get("id").asLong();
        int stockBefore = objectMapper.readTree(search.getResponse().getContentAsString())
                .get(0).get("stock").asInt();

        mockMvc.perform(post("/api/basket/add")
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/basket").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));

        mockMvc.perform(post("/api/orders/checkout")
                        .header("X-User-Id", USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ci@example.com",
                                  "shippingAddress": "CI Street 1",
                                  "paymentMethod": "CREDIT_CARD",
                                  "paymentDetails": "4111111111111111"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        mockMvc.perform(get("/api/basket").header("X-User-Id", USER))
                .andExpect(jsonPath("$.items", hasSize(0)));

        mockMvc.perform(get("/api/catalog/" + productId))
                .andExpect(jsonPath("$.stock", is(stockBefore - 1)));
    }

    @Test
    @Order(4)
    @DisplayName("негативный - оформление пустой корзины")
    void emptyCart() throws Exception {
        mockMvc.perform(post("/api/orders/checkout")
                        .header("X-User-Id", "empty-ci")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"a@b.com","shippingAddress":"A",
                                 "paymentMethod":"PAYPAL","paymentDetails":"a@p.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Корзина пуста")));
    }

    @Test
    @Order(5)
    @DisplayName("негативный - недостаточно товара")
    void insufficientStock() throws Exception {
        mockMvc.perform(post("/api/basket/add")
                        .header("X-User-Id", "stock-ci")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":99999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Недостаточно товара")));
    }
}