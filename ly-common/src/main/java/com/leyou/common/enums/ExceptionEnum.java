package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400,"价格不能为空！"),
    CATEGORY_NOT_FOND(404,"商品分类没查到"),
    BRAND_NOT_FOUND(404,"品牌没查询到" ),
    BRAND_CREATE_FAILED(500,"新增品牌失败" ),
    INAVLID_FILE_TYPE(400,"无效的文件类型"),
    GROUP_NOT_FOUNT(404,"商品规格组不存在"),
    PARAM_NOT_FOUNT(404,"规格参数未找到"),
    GOODS_SAVE_ERROR(500, "新增商品错误"),
    GOODS_NOT_FOUND(400, "商品未查询到"),
    GOODS_NOT_SALEABLE(400, "商品未上架"),
    GOODS_UPDATE_ERROR(500, "商品更新失败"),
    DELETE_GOODS_ERROR(500, "删除商品错误"),
    UPDATE_SALEABLE_ERROR(500, "更新商品销售状态错误"),
    STOCK_NOT_ENOUGH(500, "商品库存不足"),
    GOODS_STOCK_NOT_FOND(404,"商品库存不存在01"),

    CATEGORY_NOT_FOUND(204, "分类未查询到"),
    STOCK_NOT_FOUND(204, "库存查询失败"),
    SPU_NOT_FOUND(201, "SPU未查询到"),
    SKU_NOT_FOUND(201, "SKU未查询到"),

    RECEIVER_ADDRESS_NOT_FOUND(400, "收获地址不存在"),
    ORDER_NOT_FOUND(404, "订单不存在"),
    ORDER_STATUS_EXCEPTION(500, "订单状态异常"),
    CREATE_PAY_URL_ERROR(500, "常见支付链接异常"),
    WX_PAY_SIGN_INVALID(400, "微信支付签名异常"),
    WX_PAY_NOTIFY_PARAM_ERROR(400, "微信支付回调参数异常"),

    INVALID_FILE_FORMAT(400, "文件格式错误"),
    UPLOAD_IMAGE_EXCEPTION(500, "文件上传异常"),
    INVALID_PARAM(400, "参数错误"),
    USERNAME_OR_PASSWORD_ERROR(400, "账号或密码错误"),
    VERIFY_CODE_NOT_MATCHING(400, "验证码错误"),
    PASSWORD_NOT_MATCHING(400, "密码错误"),
    USER_NOT_EXIST(404, "用户不存在"),

    SPEC_PARAM_NOT_FOUND(204, "规格参数查询失败"),
    UPDATE_SPEC_PARAM_FAILED(500, "商品规格参数更新失败"),
    DELETE_SPEC_PARAM_FAILED(500, "商品规格参数删除失败"),
    SPEC_PARAM_CREATE_FAILED(500, "新增规格参数失败"),
    USER_NOT_LOGIN(401, "用户未登录，请登录"),

    SPEC_GROUP_CREATE_FAILED(500, "新增规格组失败"),
    SPEC_GROUP_NOT_FOUND(204, "规格组查询失败"),
    DELETE_SPEC_GROUP_FAILED(500, "商品规格组删除失败"),
    UPDATE_SPEC_GROUP_FAILED(500, "商品规格组更新失败"),
    GOODID_CAN_NOT_BE_NULL(400,"spuid不能为空"),
    GOODS_SKU_NOT_FOUND(404,"GOODS_SKU_NOT_FOUND"),
    INVALID_USER_DATA_TYPE_ERROR(400,"用户数据类型无效"),

    INAVLID_VERIFY_CODE(400,"无效的验证码"),
    invalid_Username_password(400,"无效的用户名或密码" ),
    CREATE_TOKEN_ERROR(500,"生成token失败" ),
    CREATE_ORDER_ERROR(500,"创建订单失败"),
    CONNECT_WXPAY_FAIL(400,"连接微信支付失败"),
    INVALID_SIGN(400,"无效的签名"),
    UPDATE_ORDER_STATUE_FAIL(400,"更新订单状态失败！");
    private int code;//错误代码
    private String msg;
}
