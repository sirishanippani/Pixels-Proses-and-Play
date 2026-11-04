package com.project.blog.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerTest {

    private final HtmlSanitizer sanitizer = new HtmlSanitizer();

    @Test
    void nullInputReturnsNull() {
        assertThat(sanitizer.sanitize(null)).isNull();
    }

    @Test
    void stripsScriptTagsEntirely() {
        String dirty = "<p>Hello</p><script>alert('pwn')</script>";
        assertThat(sanitizer.sanitize(dirty)).doesNotContain("script").doesNotContain("alert");
    }

    @Test
    void stripsInlineEventHandlersOnImages() {
        String dirty = "<img src=\"x\" onerror=\"alert(1)\">";
        String clean = sanitizer.sanitize(dirty);
        assertThat(clean).doesNotContain("onerror").doesNotContain("alert");
    }

    @Test
    void stripsJavascriptSchemeLinks() {
        String dirty = "<a href=\"javascript:alert(1)\">click</a>";
        assertThat(sanitizer.sanitize(dirty)).doesNotContain("javascript:");
    }

    @Test
    void stripsIframeTags() {
        String dirty = "<iframe src=\"https://evil.example\"></iframe><p>safe</p>";
        String clean = sanitizer.sanitize(dirty);
        assertThat(clean).doesNotContain("iframe").contains("safe");
    }

    @Test
    void stripsInlineEventHandlersOnDivs() {
        String dirty = "<div onclick=\"alert(1)\">x</div>";
        assertThat(sanitizer.sanitize(dirty)).doesNotContain("onclick");
    }

    @Test
    void preservesBasicFormatting() {
        String clean = sanitizer.sanitize("<p><strong>Hi</strong> <em>there</em></p>");
        assertThat(clean).contains("<strong>").contains("<em>").contains("Hi").contains("there");
    }

    @Test
    void preservesSafeAnchorHrefs() {
        String clean = sanitizer.sanitize("<a href=\"https://example.com\">link</a>");
        assertThat(clean).contains("href").contains("https://example.com");
    }

    @Test
    void preservesSafeImageSources() {
        String clean = sanitizer.sanitize("<img src=\"https://example.com/pic.png\" alt=\"pic\">");
        assertThat(clean).contains("<img").contains("https://example.com/pic.png");
    }
}
