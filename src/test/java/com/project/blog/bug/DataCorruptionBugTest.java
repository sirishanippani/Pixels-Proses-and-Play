package com.project.blog.bug;

import com.project.blog.model.Post;
import com.project.blog.model.Role;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@DirtiesContext
class DataCorruptionBugTest {

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;

    @Test
    void visitingHomePage_mustNotMutatePostContent() throws Exception {
        // Arrange: create a user and a post with long HTML content
        User u = new User();
        u.setUsername("victim");
        u.setEmail("victim@example.com");
        u.setPassword("x");
        u.setRole(Role.ROLE_USER);
        userRepository.save(u);

        String originalContent = "<p>" + "a".repeat(500) + "</p>";
        Post p = new Post();
        p.setTitle("Important");
        p.setContent(originalContent);
        p.setUser(u);
        postRepository.save(p);
        Long postId = p.getId();

        // Act: visit the home page (triggers HomeController)

        List<Post> random = postRepository.findRandomPosts();
        System.out.println("findRandomPosts returned: " + random.size() + " posts");
        List<Post> top10 = postRepository.findTop10ByOrderByCreatedAtDesc();
        System.out.println("findTop10 returned: " + top10.size() + " posts");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity()).build();
        mockMvc.perform(get("/")).andExpect(status().isOk());

        // Assert: the post content in the DB must be unchanged
        Post reloaded = postRepository.findById(postId).orElseThrow();
        assertThat(reloaded.getContent())
                .as("Home page visit must not modify post content in DB")
                .isEqualTo(originalContent);
    }
}
