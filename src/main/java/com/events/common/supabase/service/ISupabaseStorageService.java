package com.events.common.supabase.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ISupabaseStorageService {
    String uploadFile(MultipartFile file) throws IOException;
    List<String> uploadFiles(MultipartFile[] files) throws IOException;
}
