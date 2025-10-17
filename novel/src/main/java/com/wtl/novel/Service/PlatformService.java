package com.wtl.novel.Service;

import com.wtl.novel.entity.Platform;
import com.wtl.novel.repository.PlatformRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformService {


    @Autowired
    private PlatformRepository platformRepository;


    public List<Platform> getAllPlatforms() {
        return platformRepository.findAll();
    }

    public Platform getPlatformById(Long id) {
        return platformRepository.findById(id).orElse(null);
    }

    public Platform savePlatform(Platform platform) {
        return platformRepository.save(platform);
    }

    public Platform findPlatformByPlatformName(String platformName) {
        return platformRepository.findPlatformByPlatformName(platformName);
    }

    public void deletePlatform(Long id) {
        platformRepository.deleteById(id);
    }

    public List<Platform> getPlatformsByType(String platformType) {
        return platformRepository.findPlatformsByPlatformType(platformType);
    }
}