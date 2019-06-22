package com.leyou.order.web;

import com.leyou.order.dto.OrderDto;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;

@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    /**
     * 创建订单
     * @param orderDto
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDto orderDto){
       Long l =  orderService.createOrder(orderDto);
        return ResponseEntity.ok(l);
    }

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){

        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 创建支付链接
     * @param id
     * @return
     */
    @GetMapping("/utl/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id") Long id){

        return ResponseEntity.ok(orderService.createPayUrl(id));
    }

    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> queryStatusById(@PathVariable("id") Long id){

        return ResponseEntity.ok(orderService.queryStatusById(id).getValue());
    }

 }
