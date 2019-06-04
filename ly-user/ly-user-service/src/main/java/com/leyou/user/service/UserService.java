package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone";

    public Boolean checkData(String data, Integer type) {
        //判断数据类型
        User record = new User();
        switch (type) {
            case 1:
                record.setUsername(data);
                return userMapper.selectCount(record) == 0;
            case 2:
                record.setPassword(data);
                return userMapper.selectCount(record) == 0;
            default:
                throw new LyException(ExceptionEnum.INVALID_USER_DATA_TYPE_ERROR);
        }
    }

    public void sendCode(String phone) {
        //生成key
        String key = KEY_PREFIX + phone;

        //随机生成6位数字验证码
        String code = NumberUtils.generateCode(6);

        Map<String,String> map = new HashMap<>();
        map.put("phone", phone);
        map.put("code", code);
        //发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", map);//优化：可以将参数抽取到配置文件
        //保存验证码
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);//验证码5分钟有效
    }

    public void register(User user, String code) {
        String key = KEY_PREFIX + user.getPhone();

        //从redis取出验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        //校验验证码
        if (!StringUtils.equals(code,cacheCode)){
            throw new LyException(ExceptionEnum.INAVLID_VERIFY_CODE);
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //生成密码
        String md5Pwd = CodecUtils.md5Hex(user.getPassword(), user.getSalt());

        user.setPassword(md5Pwd);

        //保存到数据库
        user.setCreated(new Date());
        int count = userMapper.insert(user);

        if (count != 1) {
            throw new LyException(ExceptionEnum.INVALID_PARAM);
        }
        //把验证码从Redis中删除
        redisTemplate.delete(key);

    }


    public User queryUser(String username, String password) {
        User record = new User();
        record.setUsername(username);

        //首先根据用户名查询用户
        User user = userMapper.selectOne(record);

        if (user == null) {
            throw new LyException(ExceptionEnum.USER_NOT_EXIST);
        }

        //检验密码是否正确
        if (!StringUtils.equals(CodecUtils.md5Hex(password, user.getSalt()), user.getPassword())) {
            //密码不正确
            throw new LyException(ExceptionEnum.PASSWORD_NOT_MATCHING);
        }
        //用户密码都正确
        return user;
    }
}
