package com.events.modules.cachemanagement;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cache")
@RequiredArgsConstructor
public class CacheManagementController {
//    private final CacheManager cacheManager;
//
//    @GetMapping("/names")
//    public Collection<String> getCacheNames() {
//        // Récupérer le cache de Hibernate (exemple avec le cache de second niveau)
//        return cacheManager.getCacheNames();
//    }
//
//    @GetMapping
//    public Cache getCacheInfo(@RequestParam String name) {
//        return cacheManager.getCache(name);
//    }
}
