package com.leyou.sms.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.properties.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author bystander
 * @date 2018/9/29
 */
@Slf4j
@Component
@EnableConfigurationProperties(value = SmsProperties.class)
public class SmsListener {

    @Autowired
    private SmsProperties props;

    @Autowired
    private SmsUtils smsUtils;

    /**
     * 发送短信验证码
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange", type = ExchangeTypes.TOPIC),
            key = "sms.verify.code"
    ))
    public void listenVerifyCode(Map<String, Object> msg) {
        if (msg == null) {
            return;
        }
        String phone = (String) msg.remove("phone");//获取并删除元素
        //String phone = (String) msg.get("phone");
        if (StringUtils.isBlank(phone)) {
            return;
        }
        smsUtils.sendSms(phone, props.getSignName(), props.getVerifyCodeTemplate(),msg);
        //发送短信日志
        log.info("短信服务，发送短信验证码，手机号【】",phone);
    }


}
