package com.wtl.novel.filter;

import com.wtl.novel.Service.ChapterService;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.RequestLogService;
import com.wtl.novel.entity.Credential;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import static com.wtl.novel.Service.RequestLogService.CHAPTERS_ID_PATTERN_AND_GET_ID;
import static com.wtl.novel.util.URLMatcher.matches;

@Component
public class RequestFilter implements Filter {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private RequestLogService requestLogService;

    @Autowired
    private ChapterService chapterService;
//    @Bean
//    public List<String> okUrl() {
//        List<String> urls = new ArrayList<>();
//        urls.add("/api/platform/字符");
//        urls.add("/api/tag/all/字符");
//        urls.add("/api/novels/数字");
//        urls.add("/api/chaptersExecute/novel/数字");
//        urls.add("/api/chapters/getChaptersByNovelId/数字");
//        urls.add("/api/tag/getTagsByNovelId/数字");
//        urls.add("/api/favorites/user/数字/字符");
//        urls.add("/api/novels/getNovelsByPlatform/字符/数字/字符/数字/数字");
//        return urls;
//    }





    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // 如果是 OPTIONS 请求，直接放行
            if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
                httpResponse.setHeader("Access-Control-Allow-Origin", "*");
                httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
                httpResponse.setHeader("Access-Control-Max-Age", "3600");
                httpResponse.setStatus(HttpServletResponse.SC_OK);
                return; // OPTIONS 请求处理完毕，直接返回
            }

            // 获取请求的 URI
            String requestURI = httpRequest.getRequestURI();

//        Matcher matcher = CHAPTERS_ID_PATTERN_AND_GET_ID.matcher(requestURI);
//        if (matcher.matches()) {
//            String id = matcher.group(1); // 获取第一个括号中的内容
//            Integer chapterNumber = chapterService.findChapterNumberById(Long.parseLong(id));
//            if (chapterNumber != null && chapterNumber < 50) {
//                chain.doFilter(request, response);
//                return;
//            }
//        }

            if (matches(requestURI)) {
                chain.doFilter(request, response);
                return;
            }

//        // 如果是登录请求，直接放行
//        if (requestURI.startsWith("/api/auth")) {
//            chain.doFilter(request, response);
//            return;
//        }

            // 非 OPTIONS 请求，继续进行 Authorization 校验
            String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
            String token = authorizationInfo[0];

            // 验证凭据
            Credential credential = credentialService.findByToken(token);
            if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
                httpResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "无效的凭据");
                return;
            }

            // 检查请求次数
            if (!requestLogService.checkRequestLimit(credential, requestURI)) {
                httpResponse.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "请求次数已用尽");
                return;
            }

            // 继续过滤器链
            chain.doFilter(request, response);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}