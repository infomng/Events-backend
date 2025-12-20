package com.events.common.supabase.service;

import com.events.common.config.properties.SupabaseProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Objects;

@Service
@Transactional
public class SupabaseStorageService {

    private final SupabaseProperties properties;
    private final WebClient webClient;

    public SupabaseStorageService(SupabaseProperties properties) {
        this.properties = properties;

        this.webClient = WebClient.builder()
                .baseUrl(properties.projectUrl() + "/storage/v1/object/")
                .defaultHeader("Authorization", "Bearer " + properties.serviceRoleKey())
                .build();
    }

    private static String cleanFileName(MultipartFile file) {
        return Normalizer.normalize(Objects.requireNonNull(file.getOriginalFilename()), Normalizer.Form.NFD)
                .replaceAll("[^a-zA-Z0-9.\\-_]", "_")
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[%?!@#]", "");
    }

    public String uploadFile(MultipartFile file) throws IOException {

        String cleanedFileName = cleanFileName(file);

        webClient.put()
                .uri(properties.bucketName() + "/" + cleanedFileName)
                .contentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())))
                .bodyValue(file.getBytes())
                .retrieve()
                .toBodilessEntity()
                .block();

        return properties.projectUrl() + "/storage/v1/object/public/" + properties.bucketName() + "/" + cleanedFileName;
    }
}
