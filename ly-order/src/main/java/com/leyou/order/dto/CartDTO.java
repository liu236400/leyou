package com.leyou.order.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author bystander
 * @date 2018/10/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO{

    private Long skuId;  //商品skuId

    private Integer num;  //购买数量
}
