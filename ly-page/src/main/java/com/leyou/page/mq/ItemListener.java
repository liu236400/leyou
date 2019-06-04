package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {
    @Autowired
    private PageService pageService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.insert.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",
                    type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true"),
            key = {"item.insert", "item.update"}
    ))
    //监听新增或更新
    public void listenInsertOrUpdate(Long spuId) {

        if (spuId == null) {
            return;
        }
        //处理消息，对静态页进行新增
       pageService.createHtml(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "page.item.delete.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",
                    type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true"),
            key = "item.delete"
    ))
    public void listenDelete(Long spuId) {
        //监听删除
        if (spuId == null) {
            return;
        }
        //处理消息,删除静态页
        pageService.deleteHtml(spuId);
    }
}