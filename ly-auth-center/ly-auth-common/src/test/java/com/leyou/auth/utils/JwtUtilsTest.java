package com.leyou.auth.utils;


import com.leyou.auth.entity.UserInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author bystander
 * @date 2018/10/1
 */
public class JwtUtilsTest {

    private static final String publicKeyPath = "F:\\TD\\rsa.pub";
    private static final String privateKeyPath = "F:\\TD\\rsa.pri";

    private PrivateKey privateKey;
    private PublicKey publicKey;


    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(publicKeyPath, privateKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        publicKey = RsaUtils.getPublicKey(publicKeyPath);
    }

    @Test
    public void generateToken() {
        //生成Token
        String s = JwtUtils.generateToken(new UserInfo(20L, "Jack"), privateKey, 5);
        System.out.println("s = " + s);
    }



    @Test
    public void parseToken() {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiSmFjayIsImV4cCI6MTU1Njg4MzczNH0.h4XTgvcn1ZYy2GwxYH7PmscDvVMmrWkz8uTb28HVvHfRcJw1_5EON9vOSk1rEHKMjUrH6aUQlNOOJmgEex9HK23aDpnViHZuD9h4YcA1DtUwkZWtCVQT03e_tkedJxwLFI3k2kAsLH7YKUED_LD0F7AaGH1Pn0gYUNGuK8zNkdE";
        UserInfo userInfo = JwtUtils.getUserInfo(publicKey, token);
        System.out.println("id:" + userInfo.getId());
        System.out.println("name:" + userInfo.getUsername());
    }

    @Test
    public void parseToken1() {
    }

    @Test
    public void getUserInfo() {
    }

    @Test
    public void getUserInfo1() {
    }
}