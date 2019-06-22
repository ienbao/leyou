package com.leyou.auth.web;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.properties.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties prop;

    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response,
            HttpServletRequest request
            ){
        //登录
        String  token = authService.login(username,password);
        //TODO 写入cookie
        CookieUtils.newBuilder(response).httpOnly().request(request)
                .build(prop.getCookieName(),token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,HttpServletRequest request, HttpServletResponse response){
        if (StringUtils.isBlank(token)) {
            //如果没有token，表示为登录，返回403
            throw new LyException(ExceptionEnums.UNAUTHORIZED);
        }

        try{
            //解析token，获取token中的用户名
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
            String newToken = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            //将新的Token写入cookie中，并设置httpOnly
            CookieUtils.newBuilder(response).httpOnly().maxAge(prop.getCookieMaxAge()).request(request).build(prop.getCookieName(), newToken);
            //已登录，返回用户信息
            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            throw new LyException(ExceptionEnums.UNAUTHORIZED);
        }
    }

}
