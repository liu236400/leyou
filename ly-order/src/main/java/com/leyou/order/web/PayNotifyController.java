package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bystander
 * @date 2018/10/5
 */
@RestController
@Slf4j
@RequestMapping("notify")
public class PayNotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * 微信支付处理回调
     * @param msg
     * @return
     */
    @PostMapping(value = "/pay",produces = "application/xml") //声明返回的一定是xml类型
    public ResponseEntity<String> payNotify(@RequestBody Map <String, String> msg) {
        //处理回调结果
        orderService.handleNotify(msg);
        log.info("【订单支付回调】结果{}" + msg);
        // 没有异常，则返回成功
        String result = "<xml>\n" +
                "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                "</xml>";
/*        // 方式二：返回成功
        Map<String,String> msgs = new HashMap<>();
        msgs.put("return_code","SUCCESS");
        msgs.put("return_msg","OK");*/
        return ResponseEntity.ok(result);

    }

    @GetMapping("{id}")
    public String hello( @PathVariable("id") Long id){
        System.out.println("id = " + id);
        return "hello" + id;
    }
}
