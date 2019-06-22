package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionEnums {
    PRICE_CANNOT_BE_NULL(400,"价格不能为空！"),
    CATEGORY_NOT_FIND(404,"商品分类没有找到"),
    BRAND_NOT_FIND(404,"品牌不存在"),
    BRAND_SAVE_ERROR(500,"新增品牌失败！"),
    VALIDATE_IMAGE_ERROR(400,"image格式不正确！"),
    UPLOAD_IMAGE_ERROR(500,"image上传失败！"),
    PARAM_CANNOT_FIND(404,"商品规格参数不存在"),
    GOODS_NOT_FIND(404,"商品没有发现"),
    SPEC_GROUP_NOT_FIND(404,"商品规格组没有查到"),
    GOODS_SAVE_ERROR(404,"商品添加失败！"),
    DETAIL_CANNOT_BE_FIND(404,"没有找到商品详情！"),
    GOOD_SKU_CANNOT_BE_FIND(404,"商品SKU不存在！"),
    STOCK_CANNOT_BE_FIND(404,"没有库存"),
    GOODS_ID_CANNOT_BE_NULL(400,"商品id不能为空"),
    SPU_CONNT_FIND(404,"SPU不存在"),
    VALIDATE_USER_DATA_ERROR(404,"用户数据类型无效"),
    VALIDATE_CODE_ERROR(404,"验证码无效"),
    USERNAME_PASSWORD_CONNOT_FIND(404,"用户名或者密码不存在"),
    CREATE_TOKEN_ERROR(404,"用户凭证生成失败！"),
    UNAUTHORIZED(401,"未授权"),
    CART_NOT_FIND(401,"为发现购物车"),
    CREATE_ORDER_FAILE(500,"创建订单失败"),
    STOCK_CONNOT_ENOUGH(500,"库存不足"),
    ORDER_CONNOT_FIND(500,"订单不存在"),
    ORDER_DETAIL_NOT_FIND(500,"订单详情不存在"),
    ORDERSTATUS_NOT_FIND(500,"订单状态不存在"),
    WX_PAY_SIGN_INVALID(500,"微信支付签名验证"),
    WX_PAY_NOTIFY_PARAM_ERROR(500,"支付异常"),
    WXPAY_ORDER_ERROR(500,"微信支付失败"),
    ORDER_STATUS_ERROR(500,"订单状态异常"),
    SIGN_VALIDATE_ERROR(500,"签名校验失败"),
    TOTALFEESTR_OR_TRADNO_NOT_FIND(500,"金额不足"),
    STATUS_UPDATE_ERROR(500,"订单更新失败"),
    FIND_ORDER_STATUS_ERROR(500,"订单状态无效")

    ;
    private int code;
    private String msg;
}

