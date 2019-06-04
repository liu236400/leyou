package com.leyou.order.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.dto.OrderStatusEnum;
import com.leyou.order.interceptors.LoginInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bystander
 * @date 2018/10/4
 */
@Service
@Slf4j
@Transactional
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private PayHelper payHelper;

    @Autowired
    private PayLogService payLogService;

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Transactional
    public Long createOrder(OrderDTO orderDto) {

        //1.1 生成订单ID，采用自己的算法生成订单ID
        Long orderId = idWorker.nextId();
        String substring = orderId.toString().substring(4);
        orderId = Long.valueOf(substring);

        //填充order，订单中的用户信息数据从Token中获取，填充到order中
        Order order = new Order();
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setPaymentType(orderDto.getPaymentType());
        order.setPostFee(0L);  //// TODO 调用物流信息，根据地址计算邮费

        // 1.2 获取用户信息
        UserInfo user = LoginInterceptor.getLoginUser();
        log.info(" LoginInterceptor.getLoginUser()======"+user);
        order.setUserId(user.getId());
        order.setBuyerNick(user.getName());
        order.setBuyerRate(false);  //卖家为留言

        //1.3 收货人地址信息，应该从数据库中物流信息中获取，这里使用的是假的数据
        AddressDTO addressDTO = AddressClient.findById(orderDto.getAddressId());
        if (addressDTO == null) {
            // 地址不存在，抛出异常
            throw new LyException(ExceptionEnum.RECEIVER_ADDRESS_NOT_FOUND);
        }
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());
        order.setReceiverState(addressDTO.getState());


        //1.4 付款金额相关，首先把orderDto转化成map，其中key为skuId,值为购物车中该sku的购买数量
        Map<Long, Integer> numMap = orderDto.getCarts().stream().collect(Collectors.toMap(c -> c.getSkuId(), c -> c.getNum()));

        //查询商品信息，根据skuIds批量查询sku详情
        List<Sku> skus = goodsClient.querySkusByIds(new ArrayList<>(numMap.keySet()));

        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        Double totalPay = 0.0;

        //2. 填充orderDetail
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();

        //遍历skus，填充orderDetail
        for (Sku sku : skus) {
            Integer num = numMap.get(sku.getId());
            totalPay += num * sku.getPrice();

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setNum(num);
            orderDetail.setPrice(sku.getPrice().longValue());
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));

            orderDetails.add(orderDetail);
        }
        //实付金额
        order.setActualPay((totalPay.longValue() + order.getPostFee()));  //todo 还要减去优惠金额
        //总付金额
        order.setTotalPay(totalPay.longValue());

        //1.5 保存order
        int count = orderMapper.insertSelective(order);
        if (count != 1){
            log.error("创建订单失败" + orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //保存detail
        orderDetailMapper.insertList(orderDetails);
        if (count != orderDetails.size()){
            log.error("创建订单详情失败");
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //填充orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(new Date());

        //新增订单状态orderStatus
        orderStatusMapper.insertSelective(orderStatus);

        //减库存
        goodsClient.decreaseStock(orderDto.getCarts());


        //todo 删除购物车中已经下单的商品数据, 采用异步mq的方式通知购物车系统删除已购买的商品，传送商品ID和用户ID
        HashMap<String, Object> map = new HashMap<>();
        try {
            map.put("skuIds", numMap.keySet());
            map.put("userId", user.getId());
            amqpTemplate.convertAndSend("ly.cart.exchange", "cart.delete", JsonUtils.toString(map));
        } catch (Exception e) {
            log.error("删除购物车消息发送异常，商品ID：{}", numMap.keySet(), e);
        }

        log.info("生成订单，订单编号====：{}，用户id：{}", orderId, user.getId());
        return orderId;

    }

    public String generateUrl(Long orderId) {
        //根据订单ID查询订单
        Order order = queryById(orderId);
        //判断订单状态
        if (order.getOrderStatus().getStatus() != OrderStatusEnum.INIT.value()) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_EXCEPTION);
        }

        //todo 这里传入一份钱，用于测试使用，实际中使用订单中的实付金额
        String url = payHelper.createPayUrl(orderId, "乐优商城测试", /*order.getActualPay()*/1L);
        if (StringUtils.isBlank(url)) {
            throw new LyException(ExceptionEnum.CREATE_PAY_URL_ERROR);
        }

        //生成支付日志
        payLogService.createPayLog(orderId, order.getActualPay());

        return url;

    }

    //根据id查询订单
    public Order queryById(Long orderId) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情，注意查询不到时的异常判断
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        order.setOrderDetails(orderDetails);
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        order.setOrderStatus(orderStatus);
        return order;
    }

    @Transactional
    public void handleNotify(Map<String, String> msg) {
        payHelper.handleNotify(msg);
    }

    public PageResult<Order> queryOrderByPage(Integer page, Integer rows) {

        //开启分页
        PageHelper.startPage(page, rows);

        Example example = new Example(Order.class);

        //查询订单
        List<Order> orders = orderMapper.selectByExample(example);


        //查询订单详情
        for (Order order : orders) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getOrderId());
            List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

            order.setOrderDetails(orderDetailList);

            //查询订单状态
            OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(order.getOrderId());
            order.setOrderStatus(orderStatus);
        }

        PageInfo<Order> pageInfo = new PageInfo<>(orders);

        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), pageInfo.getList());
    }
}
