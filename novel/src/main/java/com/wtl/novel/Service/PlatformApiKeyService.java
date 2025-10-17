package com.wtl.novel.Service;


import com.wtl.novel.entity.PlatformApiKey;
import com.wtl.novel.repository.PlatformApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformApiKeyService {

    @Autowired
    private PlatformApiKeyRepository platformApiKeyRepository;


    public List<PlatformApiKey> getAllApiKeys() {
        return platformApiKeyRepository.findAll();
    }

    public PlatformApiKey getApiKeyById(Long id) {
        return platformApiKeyRepository.findById(id).orElse(null);
    }

    public List<PlatformApiKey> getApiKeysByPlatformId(Long platformId) {
        return platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platformId);
    }

    public PlatformApiKey saveApiKey(PlatformApiKey apiKey) {
        return platformApiKeyRepository.save(apiKey);
    }

    public void deleteApiKey(Long id) {
        platformApiKeyRepository.deleteById(id);
    }
}