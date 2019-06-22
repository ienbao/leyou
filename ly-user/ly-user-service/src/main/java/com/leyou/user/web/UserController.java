package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.naming.Binding;
import javax.validation.Valid;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 校验数据
     *
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        Boolean bool = userService.checkUserData(data, type);
        if (bool == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(bool);
    }

    /**
     * 发送短信
     *
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone) {

        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code) {
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/query")
    public ResponseEntity<User> queryUser(
            @RequestParam("username")String username,
            @RequestParam("password")String password){
       User user =  userService.queryUserByUserNameAndPassword(username,password);

        return ResponseEntity.ok(user);
    }
}
