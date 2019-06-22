package com.leyou.test;

import com.leyou.sms.utils.SmsTxUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTxTest {
    @Autowired
    private SmsTxUtils utils;
    @Autowired
    private AmqpTemplate template;
    @Test
    public void smsTest() throws InterruptedException {
        Map<String,String> msg = new HashMap<>();
       template.convertAndSend("ly.smstx.exchange","smstx.verify.code",msg);
       Thread.sleep(10000);

    }

}
