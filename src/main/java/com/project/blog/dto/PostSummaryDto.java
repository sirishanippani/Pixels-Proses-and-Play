package com.project.blog.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostSummaryDto(
        Long id,
        String title,
        String summary,
        String authorUsername,
        LocalDateTime createdAt,
        List<String> tagNames
) {}