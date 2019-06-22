package com.leyou.sms.mq;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {
    @Autowired
    private SmsUtils smsUtils;
    @Autowired
    private SmsProperties prop;

    /**
     * 发送短信验证码
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(name = "sms.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange",type = ExchangeTypes.TOPIC),
            key = {"sms.verify.code"}
    ))
    public void listenerInsertOrUpdate(Map<String ,String> msg){

        if (CollectionUtils.isEmpty(msg)){
            return;
        }
        //获取map中的电话号码,并且移除此phone数据
        String phone = msg.remove("phone");
        if (StringUtils.isBlank(phone)){
            return;
        }
    //手机发送短信
        try {
            smsUtils.sendSms(phone,prop.getSignName(),prop.getVerifyCodeTemplate(),JsonUtils.toString(msg));
        } catch (Exception e) {
            log.error("[SmsListener]，发送短信出现异常。");
            e.printStackTrace();
        }
    }


}
