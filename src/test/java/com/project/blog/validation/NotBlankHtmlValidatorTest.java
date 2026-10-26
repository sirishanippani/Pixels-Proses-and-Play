package com.project.blog.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotBlankHtmlValidatorTest {

    private final NotBlankHtmlValidator validator = new NotBlankHtmlValidator();

    @Test
    void nullIsInvalid() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void emptyStringIsInvalid() {
        assertThat(validator.isValid("", null)).isFalse();
    }

    @Test
    void whitespaceOnlyIsInvalid() {
        assertThat(validator.isValid("   \n  ", null)).isFalse();
    }

    @Test
    void wysiwygEmptyShellIsInvalid() {
        assertThat(validator.isValid("<p><br></p>", null)).isFalse();
        assertThat(validator.isValid("<p></p>", null)).isFalse();
        assertThat(validator.isValid("<div>  </div>", null)).isFalse();
    }

    @Test
    void realContentIsValid() {
        assertThat(validator.isValid("<p>Hello world</p>", null)).isTrue();
        assertThat(validator.isValid("plain text", null)).isTrue();
        assertThat(validator.isValid("<h1>Heading</h1><p>Body</p>", null)).isTrue();
    }
}
