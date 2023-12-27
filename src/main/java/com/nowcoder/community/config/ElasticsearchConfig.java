package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .usingSsl("2129455a49bbd8873c6c321680754eb5294157f302e7cc96b7aa9421ebc23eb8")
                .withBasicAuth("elastic","TGli0Mcpl7q+46vwagAz")
                .build();
    }
}
