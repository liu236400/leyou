package com.leyou.item.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.Item2;
import com.leyou.item.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("item")
@Slf4j
public class ItemController {
    @Autowired
    private ItemService itemService;
    @PostMapping
    //泛型是指响应体里面是Item
    public ResponseEntity<Item2> saveItem(Item2 item){

        //校验价格
        if (item.getPrice() == null){
            //return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            //throw new RuntimeException("价格不能为空！");
            throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
        }
        item = itemService.saveItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);//body里面存放的是返回的结果
    }
}
