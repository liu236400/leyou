package com.leyou.search.client;

import com.leyou.item.api.CategoryApi;
import com.leyou.item.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")//被调用的微服务名称
public interface CategoryClient extends CategoryApi{
   /* //对应微服务中的方法名称
    @GetMapping("category/list/ids")
    List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);
*/
}
