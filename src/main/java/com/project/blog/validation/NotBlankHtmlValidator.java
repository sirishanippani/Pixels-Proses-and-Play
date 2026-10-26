package com.project.blog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankHtmlValidator implements ConstraintValidator<NotBlankHtml, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        String stripped = value.replaceAll("<[^>]*>", "").trim();
        return !stripped.isEmpty();
    }
}
