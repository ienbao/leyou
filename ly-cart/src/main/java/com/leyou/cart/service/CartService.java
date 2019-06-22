package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:user:id:";

    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();

        //hashKey
        String hashKey = cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        Integer num = cart.getNum();
        //判断当前购物车商品是否存在
        if (operations.hasKey(hashKey)) {
            //存在,修改redis中的数量
            String json = operations.get(hashKey).toString();
            cart = JsonUtils.parse(json, Cart.class);
            cart.setNum(cart.getNum() + num);
        }
            //写回到redis
            operations.put(hashKey,JsonUtils.toString(cart));

        //是，修改数量
        //否，新增
    }

    public List<Cart> queryCartList() {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key =KEY_PREFIX + user.getId();
        if (!redisTemplate.hasKey(key)) {
            //key不存在，返回404
            throw new LyException(ExceptionEnums.CART_NOT_FIND);
        }
        //获取登录用户的所有购物车
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        List<Cart> cartList = operations.values().stream()
                .map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
        return cartList;
    }

    public void updateCartNum(Long skuId, Integer num) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key =KEY_PREFIX + user.getId();
        //hashKey
        String hashKey = skuId.toString();
        //获取操作
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

        //判断是否存在
        if (!operations.hasKey(hashKey)){
            throw new LyException(ExceptionEnums.CART_NOT_FIND);
        }
        //查询购物车
        Cart cart = JsonUtils.parse(operations.get(hashKey).toString(), Cart.class);
        cart.setNum(num);

        //写回到redis
        operations.put(hashKey,JsonUtils.toString(cart));

    }

    public void deleteCart(Long skuId) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key =KEY_PREFIX + user.getId();
        //hashKey
        String hashKey = skuId.toString();
        //删除
        redisTemplate.opsForHash().delete(key,hashKey);

    }
}
