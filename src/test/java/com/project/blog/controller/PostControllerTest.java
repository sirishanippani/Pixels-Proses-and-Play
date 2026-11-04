package com.project.blog.controller;

import com.project.blog.exception.PostOwnershipException;
import com.project.blog.flash.FlashService;
import com.project.blog.model.Role;
import com.project.blog.model.User;
import com.project.blog.security.AppUserDetails;
import com.project.blog.service.CommentService;
import com.project.blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PostService postService;
    @MockitoBean private CommentService commentService;
    @MockitoBean private FlashService flash;

    private AppUserDetails alice;

    @BeforeEach
    void setupPrincipal() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("ignored");
        user.setRole(Role.ROLE_USER);
        alice = new AppUserDetails(user);
    }

    @Test
    void getCreateForm_seedsEmptyPostForm() throws Exception {
        mockMvc.perform(get("/post/create").with(user(alice)))
                .andExpect(status().isOk())
                .andExpect(view().name("create_post"))
                .andExpect(model().attributeExists("postForm"));
    }

    @Test
    void savePost_withValidForm_delegatesToServiceAndRedirectsToDashboard() throws Exception {
        mockMvc.perform(post("/post/create")
                        .with(user(alice)).with(csrf())
                        .param("title", "Hello")
                        .param("content", "<p>Body</p>")
                        .param("tagInput", "spring"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(postService).createPost(eq(alice.getUser()), eq("Hello"), eq("<p>Body</p>"), eq("spring"));
    }

    @Test
    void savePost_withBlankTitle_redisplaysFormWithBindingErrors() throws Exception {
        mockMvc.perform(post("/post/create")
                        .with(user(alice)).with(csrf())
                        .param("title", "")
                        .param("content", "<p>Body</p>")
                        .param("tagInput", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("create_post"))
                .andExpect(model().attributeHasFieldErrors("postForm", "title"));
    }

    @Test
    void updatePost_whenServiceThrowsOwnership_redirectsToDashboard() throws Exception {
        org.mockito.Mockito.doThrow(new PostOwnershipException("ownership violated for post 42"))
                .when(postService).updatePost(eq(42L), any(), any(), any(), any());

        mockMvc.perform(post("/post/edit/42")
                        .with(user(alice)).with(csrf())
                        .param("title", "x")
                        .param("content", "<p>x</p>")
                        .param("tagInput", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }
}
