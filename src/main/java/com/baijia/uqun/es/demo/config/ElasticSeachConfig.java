package com.baijia.uqun.es.demo.config;

import java.util.Arrays;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @date: 2020/9/3 4:24 下午
 * @author: Jie.He, hejie@baijiahulian.com
 */
@Configuration
public class ElasticSeachConfig {

    /**
     * 使用冒号隔开ip和端口1
     */
    @Value("${elasticsearch.ip}")
    String[] ipAddress;


    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient() {
        HttpHost[] hosts = Arrays.stream(ipAddress)
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("user_incr_user_id_mapping", "dXNlcmluY3J1c2VyaWRtYXBwaW5ncHJvZDEyMw=="));
        RestClientBuilder restClientBuilder = RestClient.builder(hosts);
        restClientBuilder.setHttpClientConfigCallback(f -> f.setDefaultCredentialsProvider(credentialsProvider));
        return new RestHighLevelClient(restClientBuilder);
    }

}
