package com.wtl.novel.Controller;

import com.wtl.novel.Service.PlatformService;
import com.wtl.novel.entity.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/platform")
public class PlatformController {

    @Autowired
    private PlatformService platformService;

    @GetMapping("/{platformType}")
    public List<Platform> getPlatformsByType(@PathVariable String platformType) {
        return platformService.getPlatformsByType(platformType);
    }

}
