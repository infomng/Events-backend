package com.events.common.supabase.service;

import com.events.common.config.properties.SupabaseProperties;
import com.events.common.exception.ServerException;
import com.events.common.utils.contants.Constants;
import com.events.common.utils.file.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class SupabaseStorageService implements ISupabaseStorageService {

    private final SupabaseProperties properties;
    private final WebClient webClient;

    public SupabaseStorageService(SupabaseProperties properties) {
        this.properties = properties;

        this.webClient = WebClient.builder()
                .baseUrl(properties.projectUrl() + properties.storageObject())
                .defaultHeader(Constants.AUTHORIZATION, Constants.BEARER + properties.serviceRoleKey())
                .build();
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {

        String newFileName = FileUtils.generateFileName(file);

        try {
            webClient.put()
                    .uri(properties.bucketName() + "/" + newFileName)
                    .contentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())))
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (IOException e) {
            throw new ServerException(e.getMessage());
        }

        return properties.projectUrl() + properties.storageObjectPublic() + properties.bucketName() + "/" + newFileName;
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files) {
        return files != null ?
                Stream.of(files).map(file -> {
                            try {
                                return uploadFile(file);
                            } catch (IOException e) {
                                throw new ServerException(e.getMessage());
                            }
                        }

                ).toList() : List.of();
    }
}
