package com.project.blog.service;

import com.project.blog.config.UploadProperties;
import com.project.blog.exception.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService service() {
        return new StorageService(new UploadProperties(tempDir.toString()));
    }

    @Test
    void store_throwsWhenFileIsNull() {
        assertThatThrownBy(() -> service().store(null))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void store_throwsWhenFileIsEmpty() {
        MultipartFile empty = new MockMultipartFile("image", "photo.png", "image/png", new byte[0]);
        assertThatThrownBy(() -> service().store(empty))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void store_rejectsUnsupportedContentType() {
        MultipartFile exe = new MockMultipartFile("image", "evil.exe", "application/octet-stream", new byte[]{1, 2, 3});
        assertThatThrownBy(() -> service().store(exe))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("application/octet-stream");
    }

    @Test
    void store_writesFileAndReturnsUuidNameWithExtensionDerivedFromContentType() throws Exception {
        MultipartFile png = new MockMultipartFile("image", "../../etc/passwd.png", "image/png", new byte[]{1, 2, 3});

        String storedName = service().store(png);

        assertThat(storedName).endsWith(".png");
        assertThat(storedName).doesNotContain("passwd");
        assertThat(storedName).doesNotContain("/");
        assertThat(storedName).doesNotContain("..");
        assertThat(Files.exists(tempDir.resolve(storedName))).isTrue();
    }

    @Test
    void store_usesJpgExtensionForJpegContentType() {
        MultipartFile jpeg = new MockMultipartFile("image", "photo.jpeg", "image/jpeg", new byte[]{1, 2, 3});
        assertThat(service().store(jpeg)).endsWith(".jpg");
    }
}
