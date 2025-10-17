package com.wtl.novel.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

// 1.继承HttpServletRequestWrapper
public class CustomRequestWrapper extends HttpServletRequestWrapper {

    // 2.定义final属性，用于缓存请求体内容
    private final byte[] content;

    public CustomRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        // 3.构造方法中将请求体内容缓存到内部属性中
        this.content = StreamUtils.copyToByteArray(request.getInputStream());
    }

    // 4.重新getInputStream()
    @Override
    public ServletInputStream getInputStream() {
        // 5.将缓存下来的内容转换为字节流
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() {
                // 6.读取时读取第5步初始化的字节流
                return byteArrayInputStream.read();
            }
        };
    }

    // 7.重写getReader()方法，这里复用getInputStream()的逻辑
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}
