package com.leyou.item.mapper;
/*
package com.leyou.item.mapper;
import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.common.Mapper;
public interface CategoryMapper extends Mapper<Category> {
}
*/

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author bystander
 * @date 2018/9/15
 * IdListMapper<Category, Long> 参数为实体类类型和主键类型
 */
public interface CategoryMapper extends Mapper<Category>, IdListMapper<Category, Long> {
}

