package com.escaplix.backend.common.file;

import com.escaplix.backend.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 이미지 저장 (ERD 6번 규칙). DB 에는 반환된 "경로 문자열"만 저장한다.
 * - 테마 사진: savePoster → "/uploads/posters/uuid.jpg" (공개 주소)
 * - 매장 사진: saveStorePhoto → "/uploads/stores/uuid.jpg" (공개 주소)
 * - 사업자등록증·사업장 증빙 서류: savePrivate("biz" | "biz-place") → "biz/uuid.png" (비공개, loadPrivate 로만 꺼냄)
 * jpg·png·webp, 5MB 이하만. 확장자가 아니라 파일 앞부분(시그니처)으로 형식을 확인한다.
 */
@Component
public class FileStorage {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final Path publicRoot;
    private final Path privateRoot;

    public FileStorage(@Value("${app.upload.public-dir}") String publicDir,
                       @Value("${app.upload.private-dir}") String privateDir) {
        this.publicRoot = Path.of(publicDir).toAbsolutePath().normalize();
        this.privateRoot = Path.of(privateDir).toAbsolutePath().normalize();
    }

    public String savePoster(MultipartFile file) {
        String name = store(file, publicRoot.resolve("posters"));
        return "/uploads/posters/" + name;
    }

    /** 매장 사진 (C-3, C-4) → "/uploads/stores/uuid.jpg" (공개 주소) */
    public String saveStorePhoto(MultipartFile file) {
        String name = store(file, publicRoot.resolve("stores"));
        return "/uploads/stores/" + name;
    }

    public String savePrivate(MultipartFile file, String folder) {
        String name = store(file, privateRoot.resolve(folder));
        return folder + "/" + name;
    }

    public Resource loadPrivate(String storedPath) {
        Path p = privateRoot.resolve(storedPath).normalize();
        if (!p.startsWith(privateRoot) || !Files.exists(p)) {
            throw ApiException.notFound("파일이 없습니다.");
        }
        return new FileSystemResource(p);
    }

    /** DB 저장이 실패했을 때 이미 저장한 파일을 지우는 데 쓴다 */
    public void deleteQuietly(String storedPath) {
        if (storedPath == null) return;
        try {
            // 공개 사진 "/uploads/posters/x", "/uploads/stores/x" → uploads/ 아래, 나머지는 비공개 폴더
            Path p = storedPath.startsWith("/uploads/")
                    ? publicRoot.resolve(storedPath.substring("/uploads/".length()))
                    : privateRoot.resolve(storedPath);
            p = p.normalize();
            if (!p.startsWith(publicRoot) && !p.startsWith(privateRoot)) return;   // ../ 로 밖을 가리키면 무시
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
        }
    }

    public static String contentType(String storedPath) {
        if (storedPath.endsWith(".png")) return "image/png";
        if (storedPath.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private String store(MultipartFile file, Path dir) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("파일을 첨부해 주세요.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw ApiException.badRequest("파일은 5MB까지 올릴 수 있습니다.");
        }
        String ext = detectExtension(file);
        String name = UUID.randomUUID() + ext;
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(name));
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패", e);
        }
        return name;
    }

    private String detectExtension(MultipartFile file) {
        byte[] h = new byte[12];
        try (InputStream in = file.getInputStream()) {
            int n = in.readNBytes(h, 0, h.length);
            if (n >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return ".jpg";
            if (n >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return ".png";
            if (n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                    && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return ".webp";
        } catch (IOException e) {
            throw ApiException.badRequest("파일을 읽을 수 없습니다.");
        }
        throw ApiException.badRequest("이미지 파일(jpg, png, webp)만 올릴 수 있습니다.");
    }
}
