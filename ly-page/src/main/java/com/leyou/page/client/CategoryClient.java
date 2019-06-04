package com.leyou.page.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")//被调用的微服务名称
public interface CategoryClient extends CategoryApi{
   /* //对应微服务中的方法名称
    @GetMapping("category/list/ids")
    List<Category> queryCategoryByIds(@RequestParam("ids") List<Long> ids);
*/
}
