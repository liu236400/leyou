package com.leyou.sms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "ly.sms")//自动读取以ly.sms开头的属性
public class SmsProperties {

    String accessKeyId;

    String accessKeySecret;

    String signName;

    String verifyCodeTemplate;
}
