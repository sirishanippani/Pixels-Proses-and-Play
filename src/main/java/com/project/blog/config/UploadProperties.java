package com.project.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "profile.upload")
public record UploadProperties(String dir) {
}
