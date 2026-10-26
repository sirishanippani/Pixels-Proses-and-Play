package com.project.blog.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * String must be non-null and, after HTML tags are stripped and whitespace trimmed, non-empty.
 * Use for form fields backed by a WYSIWYG editor, which emit shell markup like
 * <p><br></p> even when the user has typed nothing.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankHtmlValidator.class)
public @interface NotBlankHtml {

    String message() default "must contain visible content";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
