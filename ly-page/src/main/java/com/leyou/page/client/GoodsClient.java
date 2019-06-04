package com.leyou.page.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")//被调用的微服务名称
public interface GoodsClient  extends GoodsApi{
}
