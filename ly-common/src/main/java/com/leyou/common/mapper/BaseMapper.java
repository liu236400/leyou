package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author bystander
 * @date 2018/9/19
 */
@RegisterMapper//使mapper生效
public interface BaseMapper<T, PK> extends Mapper<T>, IdListMapper<T, PK>, InsertListMapper<T> {
}
