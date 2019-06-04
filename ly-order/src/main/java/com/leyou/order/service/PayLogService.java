package com.leyou.order.service;

import com.leyou.order.dto.OrderStatusEnum;
import com.leyou.order.dto.PayStateEnum;
import com.leyou.order.interceptors.LoginInterceptor;
import com.leyou.order.mapper.PayLogMapper;
import com.leyou.order.mapper.StatusMapper;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.pojo.PayLog;
import com.leyou.order.utils.PayHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author bystander
 * @date 2018/10/5
 */
@Service
public class PayLogService {

    @Autowired
    private PayLogMapper payLogMapper;

    @Autowired
    private PayHelper payHelper;

    @Autowired
    private StatusMapper statusMapper;

    public void createPayLog(Long orderId, Long actualPay) {
        //创建支付对象
        PayLog payLog = new PayLog();
        payLog.setStatus(PayStateEnum.NOT_PAY.getValue());
        payLog.setPayType(1);
        payLog.setOrderId(orderId);
        payLog.setTotalFee(actualPay);
        payLog.setCreateTime(new Date());
        //获取用户信息
        payLog.setUserId(LoginInterceptor.getLoginUser().getId());
        payLogMapper.insertSelective(payLog);



    }

    @Transactional
    public Integer queryOrderStateByOrderId(Long orderId) {
        //优先去支付日志表中查询信息
        PayLog payLog = payLogMapper.selectByPrimaryKey(orderId);
        if (payLog == null || PayStateEnum.NOT_PAY.getValue() == payLog.getStatus()) {
            //未支付，调用微信接口查询订单支付状态
            return payHelper.queryPayState(orderId).getValue();
        }

        if (PayStateEnum.SUCCESS.getValue() == payLog.getStatus()) {
            //支付成功，返回1
            return PayStateEnum.SUCCESS.getValue();
        }

        //如果是其他状态，返回失败
        return PayStateEnum.FAIL.getValue();
    }
    @Transactional
    public PayStateEnum queryOrderStateByOrderId2(Long orderId){
         //查询订单状态(在订单状态表中查询)
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        //判断是否支付
        if(status != OrderStatusEnum.INIT.value()){
            //如果是已支付，则是真的支付
            return PayStateEnum.SUCCESS;
        }
        //如果是未支付，但其实不一定是未支付，必须去微信查询支付状态
        PayStateEnum payStateEnum = payHelper.queryPayState(orderId);

        return payStateEnum;
    }
}
