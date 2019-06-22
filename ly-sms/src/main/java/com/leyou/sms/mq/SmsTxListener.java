package com.leyou.sms.mq;

import com.leyou.sms.config.SmsTxProperties;
import com.leyou.sms.utils.SmsTxUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.Map;

@Slf4j
@Component
@EnableConfigurationProperties(SmsTxProperties.class)
public class SmsTxListener {

    @Autowired
    private SmsTxUtils smsTxUtils;
    @Autowired
    private SmsTxProperties prop;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "smstx.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "ly.smstx.exchange",type = ExchangeTypes.TOPIC),
            key = "smstx.verify.code"
    ))
    public void sendSmsTx(Map<String,String> msg){
//        if (CollectionUtils.isEmpty(msg)){
//            return;
//        }

        String pho = msg.remove("phone");
        String [] phone =  {pho};
        String  verifyCode = msg.get("code");
        String[] params = {"",verifyCode,prop.getVerifyCodeTime().toString()};
//      phone[2] = msg.get("123");
        smsTxUtils.sendSmsTx(phone,params);
        log.info("[短信服务]：发送短信验证码，手机号{}",phone);
    }

}
