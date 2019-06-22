package com.leyou.sms.utils;

import com.github.qcloudsms.SmsMultiSender;
import com.github.qcloudsms.SmsMultiSenderResult;
import com.leyou.sms.config.SmsTxProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableConfigurationProperties(SmsTxProperties.class)
public class SmsTxUtils {
    @Autowired
    private SmsTxProperties prop;
    String verifyCode = "456321";
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SMSTX_PREFIX = "smstx:phone:";
    private static final int SMSTX_PHONE_MIN_TIME = 1000*60*3;
    public  void sendSmsTx(String[] phoneNumbers, String[] params) {
        String key = SMSTX_PREFIX+phoneNumbers[0];
        //TODO 按照手机号码进行限流
        String latestTime = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(latestTime)){
            Long latest = Long.valueOf(latestTime);
            if (System.currentTimeMillis() - latest < SMSTX_PHONE_MIN_TIME){
                return;
            }
        }
        // 指定模板ID单发短信
        try {
            SmsMultiSender msender = new SmsMultiSender(prop.getAppid(), prop.getAppkey());
            SmsMultiSenderResult result =  msender.sendWithParam("86", phoneNumbers,
                    prop.getTemplateId(), params, prop.getSmsSign(), "", "");  // 签名参数未提供或者为空时，会使用默认签名发送短信
            if (!"OK".equals(result.getResponse())){
                log.info("【短信服务】:发送短信失败，phoneNumbers{}",phoneNumbers);
            }
            System.out.print(result);
            log.info("手机短信是否发送成功的详细信息{}",result);

            //把发送短信的手机号码存入redis中 10分钟后再一次发送
            redisTemplate.opsForValue().set(key,String.valueOf(System.currentTimeMillis()),prop.getVerifyCodeTime(),TimeUnit.MINUTES);

        } catch (HTTPException e) {
            // HTTP响应码错误
            e.printStackTrace();
        } catch (JSONException e) {
            // json解析错误
            e.printStackTrace();
        } catch (IOException e) {
            // 网络IO错误
            e.printStackTrace();
        } catch (com.github.qcloudsms.httpclient.HTTPException e) {
            e.printStackTrace();
        }
    }

}
