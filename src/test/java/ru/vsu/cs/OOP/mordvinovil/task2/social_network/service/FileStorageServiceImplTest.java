package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileProcessingException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileEmptyException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileOversizeException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileUnsupportedFormat;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.storage.FileStorageServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceImplTest {

    @InjectMocks
    private FileStorageServiceImpl fileStorageServiceImpl;

    @TempDir
    Path tempDir;

    @Test
    void saveFile_whenFileIsValid() {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        String result = fileStorageServiceImpl.saveFile(file, "test-dir");

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/test-dir/"));
        assertTrue(result.endsWith(".jpg"));

        Path savedFile = tempDir.resolve("test-dir").resolve(result.replace("/uploads/test-dir/", ""));
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void saveFile_whenDirectoryNotExists() throws IOException {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test image content".getBytes()
        );

        String result = fileStorageServiceImpl.saveFile(file, "new-directory");

        assertNotNull(result);
        assertTrue(Files.exists(tempDir.resolve("new-directory")));
    }

    @Test
    void saveFile_whenFileWithoutExtension() {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testfile",
                "image/jpeg",
                "test image content".getBytes()
        );

        String result = fileStorageServiceImpl.saveFile(file, "test-dir");

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/test-dir/"));
        assertFalse(result.contains("."));
    }

    @Test
    void saveFile_whenIOExceptionOccurs() {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", "/invalid/path");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        FileProcessingException exception = assertThrows(FileProcessingException.class,
                () -> fileStorageServiceImpl.saveFile(file, "test-dir"));

        assertEquals(ResponseMessageConstants.FAILURE_FILE_SAVE, exception.getMessage());
    }

    @Test
    void deleteFile_whenFileExists() throws IOException {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        Path testDir = tempDir.resolve("test-dir");
        Files.createDirectories(testDir);
        Path testFile = testDir.resolve("test-file.jpg");
        Files.createFile(testFile);

        String fileUrl = "/uploads/test-dir/test-file.jpg";

        boolean result = fileStorageServiceImpl.deleteFile(fileUrl);

        assertTrue(result);
        assertFalse(Files.exists(testFile));
    }

    @Test
    void deleteFile_whenFileNotExists() {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        String fileUrl = "/uploads/nonexistent/test-file.jpg";

        boolean result = fileStorageServiceImpl.deleteFile(fileUrl);

        assertFalse(result);
    }

    @Test
    void deleteFile_whenFileUrlIsNull() {
        boolean result = fileStorageServiceImpl.deleteFile(null);

        assertFalse(result);
    }

    @Test
    void deleteFile_whenFileUrlIsEmpty() {
        boolean result = fileStorageServiceImpl.deleteFile("");

        assertFalse(result);
    }

    @Test
    void deleteFile_whenInvalidPath() {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        String fileUrl = "/uploads/../sensitive-file.jpg";

        SecurityException securityException = assertThrows(SecurityException.class,
                () -> fileStorageServiceImpl.deleteFile(fileUrl));

        assertEquals(ResponseMessageConstants.FAILURE_INCORRECT_PATH_TO_FILE, securityException.getMessage());
    }

    @Test
    void deleteFile_whenIOExceptionOccurs() throws IOException {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        Path testDir = tempDir.resolve("test-dir");
        Files.createDirectories(testDir);
        Path testFile = testDir.resolve("test-file.jpg");
        Files.createFile(testFile);

        String fileUrl = "/uploads/test-dir/test-file.jpg";

        Files.delete(testFile);

        boolean result = fileStorageServiceImpl.deleteFile(fileUrl);

        assertFalse(result);
    }

    @Test
    void validateImageFile_whenFileIsValid() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[1024]
        );

        assertDoesNotThrow(() -> fileStorageServiceImpl.validateImageFile(file));
    }

    @Test
    void validateImageFile_whenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        FileEmptyException exception = assertThrows(FileEmptyException.class,
                () -> fileStorageServiceImpl.validateImageFile(file));

        assertEquals(ResponseMessageConstants.FAILURE_FILE_EMPTY, exception.getMessage());
    }

    @Test
    void validateImageFile_whenFileSizeExceedsLimit() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[6 * 1024 * 1024]
        );

        FileOversizeException exception = assertThrows(FileOversizeException.class,
                () -> fileStorageServiceImpl.validateImageFile(file));

        assertEquals(ResponseMessageConstants.FAILURE_FILE_OVERSIZE, exception.getMessage());
    }

    @Test
    void validateImageFile_whenContentTypeIsNull() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                null,
                new byte[1024]
        );

        FileUnsupportedFormat exception = assertThrows(FileUnsupportedFormat.class,
                () -> fileStorageServiceImpl.validateImageFile(file));

        assertEquals(ResponseMessageConstants.FAILURE_FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    void validateImageFile_whenContentTypeIsNotImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[1024]
        );

        FileUnsupportedFormat exception = assertThrows(FileUnsupportedFormat.class,
                () -> fileStorageServiceImpl.validateImageFile(file));

        assertEquals(ResponseMessageConstants.FAILURE_FILE_MUST_BE_IMAGE, exception.getMessage());
    }

    @Test
    void validateImageFile_whenInvalidFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "image/jpeg",
                new byte[1024]
        );

        FileUnsupportedFormat exception = assertThrows(FileUnsupportedFormat.class,
                () -> fileStorageServiceImpl.validateImageFile(file));

        assertEquals(ResponseMessageConstants.FAULURE_FILE_UNSUPPORTED_FORMAT, exception.getMessage());
    }


    @Test
    void validateImageFile_whenFilenameIsNull() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "image/jpeg",
                new byte[1024]
        );

        FileUnsupportedFormat exception = assertThrows(FileUnsupportedFormat.class,
                () -> fileStorageServiceImpl.validateImageFile(file));

        assertEquals(ResponseMessageConstants.FAULURE_FILE_UNSUPPORTED_FORMAT, exception.getMessage());
    }


    @Test
    void validateImageFile_whenValidFileWithSupportedExtension() {
        String[] supportedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

        for (String extension : supportedExtensions) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test" + extension,
                    "image/" + (extension.equals(".jpg") || extension.equals(".jpeg") ? "jpeg" : extension.substring(1)),
                    new byte[1024]
            );

            assertDoesNotThrow(() -> fileStorageServiceImpl.validateImageFile(file),
                    "Should not throw exception for extension: " + extension);
        }
    }

    @Test
    void getFileExtension_whenFilenameWithExtension() {
        String result = ReflectionTestUtils.invokeMethod(fileStorageServiceImpl, "getFileExtension", "test.jpg");

        assertEquals(".jpg", result);
    }

    @Test
    void getFileExtension_whenFilenameWithoutExtension() {
        String result = ReflectionTestUtils.invokeMethod(fileStorageServiceImpl, "getFileExtension", "testfile");

        assertEquals("", result);
    }

    @Test
    void getFileExtension_whenFilenameIsNull() {
        String result = ReflectionTestUtils.invokeMethod(fileStorageServiceImpl, "getFileExtension", (String) null);

        assertEquals("", result);
    }

    @Test
    void getFileExtension_whenMultipleDots() {
        String result = ReflectionTestUtils.invokeMethod(fileStorageServiceImpl, "getFileExtension", "test.file.jpg");

        assertEquals(".jpg", result);
    }

    @Test
    void savePostImage() throws IOException {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "post.jpg",
                "image/jpeg",
                "post image content".getBytes()
        );

        Long postId = 123L;
        String result = fileStorageServiceImpl.savePostImage(file, postId);

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/post-images/123/"));
        assertTrue(result.endsWith(".jpg"));

        Path savedFile = tempDir.resolve("post-images").resolve("123").resolve(result.replace("/uploads/post-images/123/", ""));
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void saveAvatar() throws IOException {
        ReflectionTestUtils.setField(fileStorageServiceImpl, "uploadDir", tempDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "avatar image content".getBytes()
        );

        Long userId = 456L;
        String result = fileStorageServiceImpl.saveAvatar(file, userId);

        assertNotNull(result);
        assertTrue(result.startsWith("/uploads/avatars/456/"));
        assertTrue(result.endsWith(".png"));

        Path savedFile = tempDir.resolve("avatars").resolve("456").resolve(result.replace("/uploads/avatars/456/", ""));
        assertTrue(Files.exists(savedFile));
    }
}





