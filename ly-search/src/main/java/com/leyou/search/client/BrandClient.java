package com.leyou.search.client;

import com.leyou.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")//被调用的微服务名称
public interface BrandClient extends BrandApi {

}
