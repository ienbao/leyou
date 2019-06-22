package com.leyou.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long skuId; //商品skuid
    private Integer num;//购买数量
}
