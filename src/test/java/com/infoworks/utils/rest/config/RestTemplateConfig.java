package com.infoworks.utils.rest.config;

import org.apache.http.client.HttpClient;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateConfig {

    private static ClientHttpRequestFactory _requestFactory;

    public static RestTemplate getTemplate() {
        if (_requestFactory == null) {
            HttpClient client = ApacheHttpClientConfig.defaultHttpClient();
            _requestFactory = new HttpComponentsClientHttpRequestFactory(client);
        }
        RestTemplate template = new RestTemplate(_requestFactory);
        return template;
    }

}
