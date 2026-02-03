package com.infoworks.utils.rest.config;

import org.apache.http.client.HttpClient;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateConfig {

    private static RestTemplate _template;

    public static RestTemplate getTemplate() {
        if (_template == null) {
            HttpClient client = ApacheHttpClientConfig.defaultHttpClient();
            ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(client);
            _template = new RestTemplate(requestFactory);
        }
        return _template;
    }

}
