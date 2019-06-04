
package com.leyou.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author bystander
 * @date 2018/9/16
 */
@Data
@ConfigurationProperties(prefix = "ly.upload")
@Component
@Primary
public class UploadProperties {
    private String baseUrl;
    private List<String> allowTypes;
}