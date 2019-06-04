package com.leyou.page.pojo;

import lombok.AllArgsConstructor;
import lombok.Data ;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    String name;
    int age;
    User friend;// 对象类型属性
}