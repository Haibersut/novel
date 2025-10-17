package com.wtl.novel.filter;

import com.wtl.novel.util.URLMatcher;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestWrapperFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        
        String requestURI = httpRequest.getRequestURI();
        
        // 只对 API 请求应用 CustomRequestWrapper
        if (!requestURI.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        
        if (URLMatcher.matchesISValid(requestURI)) {
            chain.doFilter(request, response);
        } else {
            CustomRequestWrapper requestWrapper = new CustomRequestWrapper(httpRequest);
            chain.doFilter(requestWrapper, response);
        }
    }
}