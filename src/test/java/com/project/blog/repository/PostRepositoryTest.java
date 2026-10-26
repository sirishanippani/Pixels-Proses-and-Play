package com.project.blog.repository;

import com.project.blog.model.Post;
import com.project.blog.model.Role;
import com.project.blog.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("local")
class PostRepositoryTest {

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void findByUser_returnsOnlyThatUsersPosts() {
        User alice = newUser("alice", "alice@example.com");
        User bob = newUser("bob", "bob@example.com");
        userRepository.save(alice);
        userRepository.save(bob);

        postRepository.save(newPost("Alice post 1", alice));
        postRepository.save(newPost("Alice post 2", alice));
        postRepository.save(newPost("Bob post", bob));

        List<Post> alicePosts = postRepository.findByUser(alice);

        assertThat(alicePosts).hasSize(2);
        assertThat(alicePosts).extracting(Post::getTitle)
                .containsExactlyInAnyOrder("Alice post 1", "Alice post 2");
    }

    @Test
    void findTop10ByOrderByCreatedAtDesc_returnsRecentFirst() {
        User u = newUser("u", "u@example.com");
        userRepository.save(u);
        for (int i = 0; i < 12; i++) {
            postRepository.save(newPost("Post " + i, u));
        }

        List<Post> recent = postRepository.findTop10ByOrderByCreatedAtDesc();

        assertThat(recent).hasSize(10);
    }

    @Test
    void findRandomPosts_returnsAtMostThreePosts() {
        User u = newUser("rand", "rand@example.com");
        userRepository.save(u);
        for (int i = 0; i < 5; i++) {
            postRepository.save(newPost("Random " + i, u));
        }

        List<Post> random = postRepository.findRandomPosts();

        assertThat(random).hasSizeLessThanOrEqualTo(3);
        assertThat(random).extracting(Post::getTitle).allMatch(t -> t.startsWith("Random "));
    }

    private User newUser(String username, String email) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("x");
        u.setRole(Role.ROLE_USER);
        return u;
    }

    private Post newPost(String title, User user) {
        Post p = new Post();
        p.setTitle(title);
        p.setContent("<p>body</p>");
        p.setUser(user);
        return p;
    }
}