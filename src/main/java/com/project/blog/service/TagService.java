package com.project.blog.service;

import com.project.blog.model.Tag;
import com.project.blog.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Set<Tag> resolveOrCreate(String tagCsv) {
        Set<Tag> tags = new HashSet<>();
        if (tagCsv == null || tagCsv.isBlank()) {
            return tags;
        }
        for (String raw : tagCsv.split(",")) {
            String name = raw.trim().toLowerCase();
            if (name.isEmpty()) {
                continue;
            }
            Tag tag = tagRepository.findByName(name).orElseGet(() -> {
                Tag created = new Tag();
                created.setName(name);
                return tagRepository.save(created);
            });
            tags.add(tag);
        }
        return tags;
    }
}
