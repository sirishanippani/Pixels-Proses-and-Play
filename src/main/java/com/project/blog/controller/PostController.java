package com.project.blog.controller;

import com.project.blog.model.Comment;
import com.project.blog.model.Post;
import com.project.blog.model.Tag;
import com.project.blog.model.User;
import com.project.blog.repository.CommentRepository;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.TagRepository;
import com.project.blog.repository.UserRepository;
import com.project.blog.security.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create_post";
    }

    @PostMapping("/create")
    public String savePost(@ModelAttribute("post") Post post,
                           @RequestParam("tagInput") String tagInput,
                           @AuthenticationPrincipal AppUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {


        User user = userRepository.findOptionalByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("🧍‍♀️ Whoopsie! Logged in user vanished into the void."));
        post.setUser(userDetails.getUser());
        postRepository.save(post);
        redirectAttributes.addFlashAttribute("flashMessage", "✨ Your thoughts are now out in the wild!");
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editPostForm(@PathVariable Long id,
                               Model model,
                               @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postRepository.findById(id).orElseThrow();
        if (!post.getUser().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("post", post);

        // Generate comma-separated tag string
        String tagString = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
        model.addAttribute("postTagString", tagString);

        return "edit_post";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + id));
        model.addAttribute("post", post);

        List<Comment> comments = commentRepository.findByPost(post);

        if (userDetails != null) {
            model.addAttribute("currentUser", userDetails.getUser());
        }

        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());
        model.addAttribute("loggedInUser", userDetails != null ? userDetails.getUser() : null);

        return "post_detail";
    }

    @PostMapping("/edit/{id}")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute Post updatedPost,
                             @RequestParam("tagInput") String tagInput,
                             @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postRepository.findById(id).orElseThrow();
        if (post.getUser().getUsername().equals(userDetails.getUsername())) {
            post.setTitle(updatedPost.getTitle());
            post.setContent(updatedPost.getContent());

            // ✨ Parse and update tags
            Set<Tag> tags = parseTags(tagInput);
            post.setTags(tags);
            postRepository.save(post);
        }
        return "redirect:/post/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postRepository.findById(id).orElseThrow();
        if (post.getUser().getUsername().equals(userDetails.getUsername())) {
            postRepository.delete(post);
        }
        return "redirect:/dashboard";
    }

    // Utility to parse and fetch/create tags
    private Set<Tag> parseTags(String tagInput) {
        Set<Tag> tagSet = new HashSet<>();
        if (tagInput != null && !tagInput.isBlank()) {
            String[] tagNames = tagInput.split(",");
            for (String name : tagNames) {
                String trimmed = name.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    Tag tag = tagRepository.findByName(trimmed).orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(trimmed);
                        return tagRepository.save(newTag);
                    });
                    tagSet.add(tag);
                }
            }
        }
        return tagSet;
    }
}