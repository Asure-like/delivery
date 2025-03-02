package org.example.delivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aliyun.ocr")
@Data
public class AliYunConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String region;
    private String endpoint;

    // Getter/Setter省略
}
