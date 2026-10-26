package com.project.blog.controller;

import com.project.blog.dto.PostSearchResultDto;
import com.project.blog.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
@WithMockUser
class SearchControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PostService postService;

    @Test
    void searchPosts_returnsJsonArrayFromService() throws Exception {
        when(postService.searchByTitle("spring"))
                .thenReturn(List.of(
                        new PostSearchResultDto(1L, "Spring Boot"),
                        new PostSearchResultDto(2L, "Spring Security")));

        mockMvc.perform(get("/search").param("q", "spring"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Spring Boot"))
                .andExpect(jsonPath("$[1].title").value("Spring Security"));
    }
}
