package com.leyou.cart.config;

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

    @PostConstruct
    public void init() throws Exception {
        //公钥私钥都存在
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }

}

