package com.project.blog.service;

import com.project.blog.dto.PostSearchResultMapper;
import com.project.blog.dto.PostSummaryDto;
import com.project.blog.dto.PostSummaryMapper;
import com.project.blog.exception.PostNotFoundException;
import com.project.blog.exception.PostOwnershipException;
import com.project.blog.model.Post;
import com.project.blog.model.Tag;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private PostSummaryMapper summaryMapper;
    @Mock private PostSearchResultMapper searchResultMapper;
    @Mock private TagService tagService;

    @InjectMocks private PostService postService;

    private User alice;
    private User bob;

    @BeforeEach
    void setupUsers() {
        alice = new User();
        alice.setId(1L);
        alice.setUsername("alice");

        bob = new User();
        bob.setId(2L);
        bob.setUsername("bob");
    }

    @Test
    void createPost_persistsPostWithAuthorTitleContentAndTags() {
        Tag spring = new Tag();
        spring.setName("spring");
        when(tagService.resolveOrCreate("spring")).thenReturn(Set.of(spring));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        Post created = postService.createPost(alice, "Hello", "<p>Body</p>", "spring");

        assertThat(created.getUser()).isEqualTo(alice);
        assertThat(created.getTitle()).isEqualTo("Hello");
        assertThat(created.getContent()).isEqualTo("<p>Body</p>");
        assertThat(created.getTags()).containsExactly(spring);
        verify(postRepository).save(created);
    }

    @Test
    void updatePost_overwritesTitleContentAndTagsWhenOwner() {
        Post existing = new Post();
        existing.setId(10L);
        existing.setUser(alice);
        existing.setTitle("Old");
        existing.setContent("Old body");
        when(postRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(tagService.resolveOrCreate("new-tag")).thenReturn(Set.of());
        when(postRepository.save(existing)).thenReturn(existing);

        Post updated = postService.updatePost(10L, alice, "New", "<p>New body</p>", "new-tag");

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getContent()).isEqualTo("<p>New body</p>");
    }

    @Test
    void updatePost_throwsPostOwnershipExceptionWhenRequesterIsNotOwner() {
        Post existing = new Post();
        existing.setId(10L);
        existing.setUser(alice);
        when(postRepository.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> postService.updatePost(10L, bob, "x", "x", ""))
                .isInstanceOf(PostOwnershipException.class);

        verify(postRepository, never()).save(any());
    }

    @Test
    void getPostForView_throwsPostNotFoundExceptionWhenMissing() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostForView(99L))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    void getFeaturedPosts_returnsAtMostThree() {
        when(postRepository.findAllIds())
                .thenReturn(new ArrayList<>(List.of(1L, 2L, 3L, 4L, 5L)));
        when(postRepository.findAllById(anyIterable())).thenAnswer(inv -> {
            Iterable<Long> ids = inv.getArgument(0);
            List<Post> result = new ArrayList<>();
            ids.forEach(id -> {
                Post p = new Post();
                p.setId(id);
                result.add(p);
            });
            return result;
        });
        PostSummaryDto placeholder = new PostSummaryDto(1L, "t", "s", "u", null, List.of());
        when(summaryMapper.toDto(any(Post.class))).thenReturn(placeholder);

        List<PostSummaryDto> featured = postService.getFeaturedPosts();

        assertThat(featured).hasSize(3);
    }

    @Test
    void getFeaturedPosts_returnsAllWhenFewerThanThreePostsExist() {
        when(postRepository.findAllIds())
                .thenReturn(new ArrayList<>(List.of(1L, 2L)));
        when(postRepository.findAllById(anyIterable())).thenAnswer(inv -> {
            Iterable<Long> ids = inv.getArgument(0);
            List<Post> result = new ArrayList<>();
            ids.forEach(id -> {
                Post p = new Post();
                p.setId(id);
                result.add(p);
            });
            return result;
        });
        PostSummaryDto placeholder = new PostSummaryDto(1L, "t", "s", "u", null, List.of());
        when(summaryMapper.toDto(any(Post.class))).thenReturn(placeholder);

        List<PostSummaryDto> featured = postService.getFeaturedPosts();

        assertThat(featured).hasSize(2);
    }

    @Test
    void getFeaturedPosts_returnsEmptyWhenNoPostsExist() {
        when(postRepository.findAllIds()).thenReturn(new ArrayList<>());
        when(postRepository.findAllById(anyIterable())).thenReturn(List.of());

        List<PostSummaryDto> featured = postService.getFeaturedPosts();

        assertThat(featured).isEmpty();
    }
}
