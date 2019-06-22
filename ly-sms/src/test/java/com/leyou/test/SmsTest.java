package com.leyou.test;

import com.leyou.sms.mq.SmsListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTest {
    @Autowired
    private SmsListener smsListener;
    @Autowired
    private AmqpTemplate template;

   @Test
    public void sendMessage() throws InterruptedException {
       Map<String,String> msg = new HashMap<>();
       msg.put("phone","18675611910");
       msg.put("code","54132");
       template.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
       Thread.sleep(10000);

   }

}
