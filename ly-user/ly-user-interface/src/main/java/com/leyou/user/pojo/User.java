package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    @NotEmpty(message = "用户名不能为空")
    @Length(max = 30, min = 4, message = "用户名长度只能在4-30之间")
    private String username;// 用户名

    @JsonIgnore //需要引入jackson的包,不转换成josn数据
    @Length(max = 30, min = 4, message = "密码长度只能在4-30之间")
    private String password;// 密码

    @Pattern(regexp = "^1[35678]\\d{9}$",message = "手机号格式不正确")
    private String phone;// 电话

    private Date created;// 创建时间

    @JsonIgnore
    private String salt;// 密码的盐值

}