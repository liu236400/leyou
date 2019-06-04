package com.leyou.order.web;

import com.leyou.common.vo.PageResult;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import com.leyou.order.service.PayLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author bystander
 * @date 2018/10/4
 */
@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayLogService payLogService;

    /**
     * 创建订单
     *
     * @param orderDto
     * @return订单id
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody @Valid OrderDTO orderDto) {
        log.info("orderDto===" + orderDto);
        Long order = orderService.createOrder(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * 生成微信支付链接
     *
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    public ResponseEntity<String> generateUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.generateUrl(orderId));
    }

    /**
     * 根据订单ID查询订单详情
     *
     * @param orderId
     * @return
     */
    @GetMapping()
    public ResponseEntity<Order> queryOrderById(@RequestParam("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryById(orderId));
    }

    /**
     * 查询订单支付状态
     *
     * @param orderId
     * @return
     */
/*    @GetMapping("state/{id2}")
    public ResponseEntity<Integer> queryOrderStateByOrderId2(@PathVariable("id2") Long orderId) {
        return ResponseEntity.ok(payLogService.queryOrderStateByOrderId(orderId));
    }*/

    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderStateByOrderId(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(payLogService.queryOrderStateByOrderId2(orderId).getValue());
    }

    /**
     * 分页查询所有订单
     *
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<PageResult<Order>> queryOrderByPage(@RequestParam("page") Integer page,
                                                              @RequestParam("rows") Integer rows) {

        return ResponseEntity.ok(orderService.queryOrderByPage(page, rows));
    }
}
