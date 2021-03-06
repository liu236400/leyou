package com.leyou.item.service;

import com.leyou.item.dto.CartDto;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest {
    @Autowired
    private GoodsService goodsService;
    @org.junit.Test
    public void decreaseStock() {
        List<CartDto> list = Arrays.asList(new CartDto(2600242L, 2), new CartDto(2600248l, 2));
        goodsService.decreaseStock(list);
    }
}