package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    @NotEmpty(message = "用户名成不能为空")
    @Length(min = 4,max = 32,message = "用户名长度必须在4~32位")
    private String username;        //用户名
    @JsonIgnore
    @Length(min = 4,max = 32,message = "用户名长度必须在4~32位")
    private String password;        //密码
    private Date created;           //创建时间
    @Pattern(regexp = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$",message = "手机格式不正确")
    private String phone;     //电话号码
    @JsonIgnore
    private String salt;            //密码的盐值

}
