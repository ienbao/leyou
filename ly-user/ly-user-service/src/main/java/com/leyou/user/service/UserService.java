package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RabbitTemplate template;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone";

    public Boolean checkUserData(String data, Integer type) {
        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnums.VALIDATE_USER_DATA_ERROR);
        }
        return (0 == userMapper.selectCount(user));
    }

    /**
     * 发送短信
     *
     * @param phone
     */
    public void sendCode(String phone) {
        //生成key
        String key = KEY_PREFIX + phone;
        //生成code
        String code = NumberUtils.generateCode(6);
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        //发送验证码
        template.convertAndSend("ly.smstx.exchange", "smstx.verify.code", msg);
        //保存验证码
        redisTemplate.opsForValue().set(key, code, 3, TimeUnit.MINUTES);

    }

    public void register(User user, String code) {
        //获取存在的验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        //校验验证码
        if (!cacheCode.equals(code)) {
            throw new LyException(ExceptionEnums.VALIDATE_CODE_ERROR);
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        //用户保存salt
        user.setSalt(salt);

        //对密码进行加密
        String md5Hex = CodecUtils.md5Hex(user.getPassword(), salt);
        user.setPassword(md5Hex);
        //写入数据库
        user.setCreated(new Date());

        userMapper.insert(user);
    }

    public User queryUserByUserNameAndPassword(String username, String password) {
        //查询用户
        User recod = new User();
        recod.setUsername(username);
        User user = userMapper.selectOne(recod);
        //校验
        if (user == null){
            throw new LyException(ExceptionEnums.USERNAME_PASSWORD_CONNOT_FIND);
        }
        //校验密码
        if (!StringUtils.equals(user.getPassword(),CodecUtils.md5Hex(password,user.getSalt()))) {
            throw new LyException(ExceptionEnums.USERNAME_PASSWORD_CONNOT_FIND);
        }
        //用户名和密码正确
        return user;
    }
}
