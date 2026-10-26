package com.project.blog.service;

import com.project.blog.model.Tag;
import com.project.blog.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock private TagRepository tagRepository;
    @InjectMocks private TagService tagService;

    @Test
    void resolveOrCreate_returnsEmptySetForBlankOrNullInput() {
        assertThat(tagService.resolveOrCreate("")).isEmpty();
        assertThat(tagService.resolveOrCreate("   ")).isEmpty();
        assertThat(tagService.resolveOrCreate(null)).isEmpty();
    }

    @Test
    void resolveOrCreate_reusesExistingTagsLowercasesAndCreatesMissingOnes() {
        Tag existing = new Tag();
        existing.setName("spring");
        when(tagRepository.findByName("spring")).thenReturn(Optional.of(existing));
        when(tagRepository.findByName("new-one")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));

        Set<Tag> tags = tagService.resolveOrCreate("Spring, NEW-ONE");

        assertThat(tags).extracting(Tag::getName)
                .containsExactlyInAnyOrder("spring", "new-one");
    }
}
