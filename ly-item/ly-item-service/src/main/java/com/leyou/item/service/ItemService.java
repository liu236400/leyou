package com.leyou.item.service;

import com.leyou.item.pojo.Item2;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ItemService {
    public Item2 saveItem(Item2 item){
        int id = new Random().nextInt(5);
        item.setId(id);
        return item;
    }
}
