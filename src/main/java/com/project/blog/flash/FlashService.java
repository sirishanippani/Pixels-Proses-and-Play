package com.project.blog.flash;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Service
public class FlashService {

    static final String FLASH_ATTR = "flashMessage";

    private final MessageSource messageSource;

    public FlashService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void success(RedirectAttributes attrs, String code, Object... args) {
        attach(attrs, code, FlashType.SUCCESS, args);
    }

    public void error(RedirectAttributes attrs, String code, Object... args) {
        attach(attrs, code, FlashType.ERROR, args);
    }

    public void warning(RedirectAttributes attrs, String code, Object... args) {
        attach(attrs, code, FlashType.WARNING, args);
    }

    public void info(RedirectAttributes attrs, String code, Object... args) {
        attach(attrs, code, FlashType.INFO, args);
    }

    private void attach(RedirectAttributes attrs, String code, FlashType type, Object... args) {
        String text = messageSource.getMessage(code, args, Locale.getDefault());
        attrs.addFlashAttribute(FLASH_ATTR, new FlashMessage(text, type));
    }
}
