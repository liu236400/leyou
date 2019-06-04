package com.leyou.gateway.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author bystander
 * @date 2018/10/1
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;
    private PublicKey publicKey;


    /**
     * 对象一旦实例化后，就应该读取公钥和私钥
     */
    @PostConstruct//构造函数执行完毕以后执行，相当于init方法
    public void init() {
        try {
            //读取公钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);

        } catch (Exception e) {
            log.error("初始化公钥私钥失败",e);
            throw new RuntimeException();
        }
    }
}
