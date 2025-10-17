package com.wtl.novel.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA (Single Page Application) 路由控制器
 * 用于处理 Vue Router 的 HTML5 History 模式
 * 将所有前端路由请求转发到 index.html，让前端路由器处理
 */
@Controller
public class SpaController {

    /**
     * 转发所有非 API、非静态资源的请求到 index.html
     * 这样可以支持 Vue Router 的 HTML5 History 模式
     * 
     * 注意：这个映射的优先级较低，只有在没有其他匹配的映射时才会生效
     */
    @GetMapping(value = {
            "/",
            "/login",
            "/search",
            "/webLibrary",
            "/webLibraryNp",
            "/favorites",
            "/webHistory",
            "/webNote",
            "/novelPlatform",
            "/syosetuNovel",
            "/novelPiaNovel",
            "/uploadAndShare",
            "/uploadNovelDetail",
            "/tagFilter",
            "/messageView",
            "/webStore",
            "/blacklistPage",
            "/modifyPassword",
            "/infoList",
            "/userDetail"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }

    /**
     * 处理带路径参数的前端路由
     */
    @GetMapping(value = {
            "/novelDetail/**",
            "/chapterDetail/**",
            "/recommendationDetail/**",
            "/noteDetail/**",
            "/uploadNovelEdit/**",
            "/uploadChapterAdmin/**",
            "/uploadChapterEdit/**",
            "/glossaryPage/**",
            "/userGlossaryPage/**",
            "/writerDetail/**"
    })
    public String forwardToIndexWithParams(HttpServletRequest request) {
        return "forward:/index.html";
    }
}
