package com.events.common.utils.file;

import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.Objects;
import java.util.UUID;

public class FileUtils {
    private static String cleanFileName(MultipartFile file) {
        return Normalizer.normalize(Objects.requireNonNull(file.getOriginalFilename()), Normalizer.Form.NFD)
                .replaceAll("[^a-zA-Z0-9.\\-_]", "_")
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[%?!@#]", "");
    }

    public static String generateFileName(MultipartFile file) {
        String original = file.getOriginalFilename();
        String extension = "";
        if (original != null) {
            int dot = original.lastIndexOf('.');
            extension = dot >= 0 ? original.substring(dot) : "";
        }
        return UUID.randomUUID() + extension;
    }

}
