package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author bystander
 * @date 2018/9/26
 */
@Controller//restController把很多结果当成json处理，现在不需要
public class LyPageController {

    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")//页面跳转都是get请求
    public String toItemPage(@PathVariable("id") Long spuId, Model model) {
        //查询模型数据
        Map<String, Object> attributes = pageService.loadModel(spuId);
        //准备模型数据
        model.addAllAttributes(attributes);
        //返回视图
        return "item";
    }

}
