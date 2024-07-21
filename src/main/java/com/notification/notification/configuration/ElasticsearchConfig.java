package com.notification.notification.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.port:0}")
    private Integer esPort;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
                // Use your Elasticsearch cluster's host and port
                new HttpHost("localhost", 9200, "http")
        );
        return new RestHighLevelClient(builder);
    }


    public RestHighLevelClient getRestHighLevelClient() throws Exception {
        return client();
    }

    @Bean(destroyMethod = "close")
    RestHighLevelClient client() throws Exception {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(esHost, esPort)));
    }

}


