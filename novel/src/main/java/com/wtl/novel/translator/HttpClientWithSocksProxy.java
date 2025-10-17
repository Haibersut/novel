package com.wtl.novel.translator;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class HttpClientWithSocksProxy {

    // 自定义 SOCKS 代理的 Socket 工厂（支持 HTTP）
    public static class SocksPlainSocketFactory extends PlainConnectionSocketFactory {
        private final Proxy proxy;

        public SocksPlainSocketFactory(Proxy proxy) {
            this.proxy = proxy;
        }

        @Override
        public Socket createSocket(HttpContext context) throws IOException {
            return new Socket(proxy);
        }
    }

    // 自定义 SOCKS 代理的 Socket 工厂（支持 HTTPS）
    public static class SocksSSLSocketFactory extends SSLConnectionSocketFactory {
        private final Proxy proxy;

        public SocksSSLSocketFactory(SSLContext sslContext, Proxy proxy) {
            super(sslContext, NoopHostnameVerifier.INSTANCE);
            this.proxy = proxy;
        }

        @Override
        public Socket createSocket(HttpContext context) throws IOException {
            return new Socket(proxy);
        }
    }

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = createHttpClient()) {
            String url = "https://novelpia.com/novel/120102";
            String cookie = "your_cookie_here";

            String response = sendGetRequest(httpClient, url, cookie);
            System.out.println("响应长度: " + response.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static CloseableHttpClient createHttpClient() throws Exception {
        // 创建 SOCKS 代理
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 7890));

        // 创建 SSL 上下文
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial((chain, authType) -> true) // 信任所有证书
                .build();

        // 注册自定义的 Socket 工厂
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new SocksPlainSocketFactory(proxy))
                .register("https", new SocksSSLSocketFactory(sslContext, proxy))
                .build();

        // 设置连接池
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(20);
        connectionManager.setDefaultMaxPerRoute(10);

        // 设置超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setSocketTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private static String sendGetRequest(CloseableHttpClient httpClient, String url, String cookie) throws Exception {
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        if (cookie != null && !cookie.isEmpty()) {
            request.setHeader("Cookie", cookie);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        }
    }
}
