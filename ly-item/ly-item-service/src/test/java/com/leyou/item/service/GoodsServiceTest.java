package com.leyou.item.service;


import com.leyou.common.dto.CartDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest {

    @Autowired
    private GoodsService goodsService;
    @Test
    public void decreaseStock() {

        List<CartDto> carts = new ArrayList<>();
        carts.add(new CartDto(2600242L,2));
        carts.add(new CartDto(2600248L,3));
        goodsService.decreaseStock(carts);

    }
}
