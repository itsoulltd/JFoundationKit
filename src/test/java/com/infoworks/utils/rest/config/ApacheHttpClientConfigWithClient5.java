package com.infoworks.utils.rest.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;

public class ApacheHttpClientConfigWithClient5 {

    private static HttpClient _client;

    public static HttpClient defaultHttpClient() {
        if (_client == null) {
            // Config connection pooling
            PoolingHttpClientConnectionManager connectionManager =
                    new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(100);
            connectionManager.setDefaultMaxPerRoute(20);

            // Request config
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(1200))
                    .setResponseTimeout(Timeout.ofMilliseconds(500))
                    .setConnectTimeout(Timeout.ofMilliseconds(1000))
                    .build();

            // Create HTTP client
            HttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            _client = httpClient;
        }
        return _client;
    }

}
