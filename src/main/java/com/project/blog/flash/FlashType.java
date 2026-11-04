package com.project.blog.flash;

public enum FlashType {
    SUCCESS("success"),
    ERROR("danger"),
    WARNING("warning"),
    INFO("info");

    private final String bootstrapVariant;

    FlashType(String bootstrapVariant) {
        this.bootstrapVariant = bootstrapVariant;
    }

    public String getBootstrapVariant() {
        return bootstrapVariant;
    }
}
