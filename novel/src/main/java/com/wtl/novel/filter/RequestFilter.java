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

import java.time.LocalDateTime;
import static com.wtl.novel.util.URLMatcher.matches;

@Component
public class RequestFilter implements Filter {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private RequestLogService requestLogService;

    @Autowired
    private ChapterService chapterService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            if (handleOptionsRequest(httpRequest, httpResponse)) {
                return;
            }

            String requestURI = httpRequest.getRequestURI();

            if (handleActuatorRequest(httpRequest, httpResponse, requestURI)) {
                return;
            }

            if (!requestURI.startsWith("/api/")) {
                chain.doFilter(request, response);
                return;
            }

            if (matches(requestURI)) {
                chain.doFilter(request, response);
                return;
            }

            if (!validateAuthorization(httpRequest, httpResponse)) {
                return;
            }

            String authHeader = httpRequest.getHeader("Authorization");
            String[] authorizationInfo = authHeader.split(";");
            String token = authorizationInfo[0];

            Credential credential = credentialService.findByToken(token);
            if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
                httpResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "无效的凭据");
                return;
            }

            if (!requestLogService.checkRequestLimit(credential, requestURI)) {
                httpResponse.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "请求次数已用尽");
                return;
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean handleOptionsRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (!httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            return false;
        }

        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        return true;
    }

    private boolean handleActuatorRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String requestURI) {
        if (!requestURI.startsWith("/actuator")) {
            return false;
        }

        String remoteAddr = httpRequest.getRemoteAddr();
        System.out.println("[RequestFilter] Actuator访问 - URI: " + requestURI + ", RemoteAddr: " + remoteAddr);
        
        if (remoteAddr != null && (remoteAddr.equals("127.0.0.1") ||
                remoteAddr.equals("0:0:0:0:0:0:0:1") ||
                remoteAddr.equals("::1") ||
                remoteAddr.equals("localhost"))) {
            return false; // 返回false表示应该继续处理请求
        } else {
            try {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                httpResponse.setContentType("text/html;charset=UTF-8");
                httpResponse.getWriter().write("");
                httpResponse.getWriter().flush();
            } catch (Exception e) {
                System.out.println("Error writing response: " + e.getMessage());
            }
            return true; // 返回true表示已经处理了请求
        }
    }

    private boolean validateAuthorization(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            try {
                httpResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "缺少 Authorization header");
            } catch (Exception e) {
                System.out.println("Error sending error response: " + e.getMessage());
            }
            return false;
        }

        String[] authorizationInfo = authHeader.split(";");
        if (authorizationInfo.length == 0) {
            try {
                httpResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "无效的 Authorization 格式");
            } catch (Exception e) {
                System.out.println("Error sending error response: " + e.getMessage());
            }
            return false;
        }

        return true;
    }
}