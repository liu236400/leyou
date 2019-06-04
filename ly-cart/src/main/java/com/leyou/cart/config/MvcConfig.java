package com.leyou.cart.config;

import com.leyou.cart.filter.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**使拦截器生效
 * @author bystander
 * @date 2018/10/3
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtProperties props;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //配置登录拦截器
        registry.addInterceptor(new LoginInterceptor(props)).addPathPatterns("/**");//拦截一切路径 /cart/**拦截以cart开头的路径
    }
}
