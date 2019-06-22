package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ly.smstx")
public class SmsTxProperties {
    private Integer appid;                        //短信应用SDK AppID
    private String appkey;                       //短信应用SDK AppKey
    private Integer templateId;                 //短信模板ID
    private String smsSign;                     //签名
    private Long verifyCodeTime;            //验证码有效时长
}
