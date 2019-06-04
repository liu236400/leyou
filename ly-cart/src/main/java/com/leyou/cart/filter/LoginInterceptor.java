package com.leyou.cart.filter;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author bystander
 * @date 2018/10/3
 */
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    //@Autowired
    private JwtProperties props;

    //定义一个线程域，存放登录的对象
    private static final ThreadLocal<UserInfo> t1 = new ThreadLocal<>();

    public LoginInterceptor() {
        super();
    }

    public LoginInterceptor(JwtProperties props) {//构造方法注入
        this.props = props;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //查询Token
        String token = CookieUtils.getCookieValue(request, props.getCookieName());
        if (StringUtils.isBlank(token)) {
            //用户未登录,返回401，拦截
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        //用户已登录，获取用户信息
        try {
            UserInfo userInfo = JwtUtils.getUserInfo(props.getPublicKey(), token);
            //放入线程域中
            t1.set(userInfo);
            return true;
        } catch (Exception e) {
            log.error("购物车解析用户失败",e);
            //抛出异常，未登录
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    /**
     * 该方法视图渲染完成后执行  controller层方法已经执行完毕
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //过滤器完成后，从线程域中删除用户信息
        t1.remove();
    }

    /**
     * 获取登陆用户
     * @return
     */
    public static UserInfo getLoginUser() {
        return t1.get();
    }
}
