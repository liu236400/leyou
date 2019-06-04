package com.leyou.page.web;

import com.leyou.page.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    @GetMapping("hello")
    public String toHello(){
        return  "hello";
    }

    @GetMapping("hello2")
    public String toHello2(Model model){
        model.addAttribute("msg","hello,thymeleaf!");//向模型中添加键值对
        return  "hello";
    }
    @GetMapping("show2")
    public String show2(Model model){
        User user = new User();
        user.setAge(21);
        user.setName("Jack Chen");
        user.setFriend(new User("李小龙", 30,null));

        model.addAttribute("user", user);
        return "hello";
    }
}
