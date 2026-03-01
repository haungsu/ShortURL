package com.example.shorturlpro.controller;

import com.example.shorturlpro.dto.ShortUrlGenerateRequest;
import com.example.shorturlpro.dto.ShortUrlGenerateResponse;
import com.example.shorturlpro.service.ShortUrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortUrlController.class)
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortUrlService shortUrlService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateShortUrl_Success() throws Exception {
        // Given
        ShortUrlGenerateRequest request = new ShortUrlGenerateRequest();
        request.setOriginalUrl("https://www.example.com/test");
        request.setAppId("test-app");

        ShortUrlGenerateResponse response = new ShortUrlGenerateResponse();
        response.setShortCode("abc123");
        response.setShortUrl("http://localhost:8080/abc123");

        when(shortUrlService.generateShortUrl(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/short-url/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc123"));
    }

    @Test
    void generateShortUrl_InvalidUrl_ShouldReturnBadRequest() throws Exception {
        // Given
        ShortUrlGenerateRequest request = new ShortUrlGenerateRequest();
        request.setOriginalUrl("invalid-url"); // 不是有效的URL格式

        // When & Then
        mockMvc.perform(post("/api/short-url/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateShortUrl_EmptyUrl_ShouldReturnBadRequest() throws Exception {
        // Given
        ShortUrlGenerateRequest request = new ShortUrlGenerateRequest();
        request.setOriginalUrl(""); // 空URL

        // When & Then
        mockMvc.perform(post("/api/short-url/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}