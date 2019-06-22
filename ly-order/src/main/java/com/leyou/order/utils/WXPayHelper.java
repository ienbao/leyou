package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;

import static com.github.wxpay.sdk.WXPayConstants.*;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PatStatus;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WXPayHelper {
    @Autowired
    private WXPay wxPay;
    @Autowired
    private PayConfig config;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper statusMapper;

    /**
     * 统一下单工具类
     *
     * @param orderId
     * @param totalPay
     * @param desc
     * @return
     */
    public String createOrder(Long orderId, Long totalPay, String desc) {
        try {
            Map<String, String> reqData = new HashMap<>();
            //商品描述
            reqData.put("body", desc);
            //订单号
            reqData.put("out_trade_no", orderId.toString());
            //金额，单位是分
            reqData.put("total_fee", totalPay.toString());
            //调用微信支付的终端ip
            reqData.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            reqData.put("notify_url", config.getNotifyUrl());
            //交易类型为扫码支付
            reqData.put("trade_type", "NATIVE");

            //利用WXPay工具，完成下单
            Map<String, String> result = wxPay.unifiedOrder(reqData);
            //判断通讯标识
            String return_code = result.get("return_code");
            if (FAIL.equals(return_code)) {
                //通讯失败
                log.error("[微信下单] 微信下单通知失败，失败原因:{}", result.get("return_msg"));
                throw new LyException(ExceptionEnums.WXPAY_ORDER_ERROR);
            }
            //判断业务标识
            String result_code = result.get("result_code");
            if (FAIL.equals(result_code)) {
                //通信失败
                log.error("[微信下单] 微信下单通知失败，错误码:{},错误描述:{}"
                        , result.get("err_code"), result.get("err_code_des"));
                throw new LyException(ExceptionEnums.WXPAY_ORDER_ERROR);
            }
            //下单成功，获取支付链接
            String url = result.get("code_url");
            return url;

        } catch (Exception e) {
            log.error("[微信下单]创建交易订单失败", e);
            return null;
        }
    }

    public void isSuccess(Map<String,String> result) {
        //判断通讯标识
        String return_code = result.get("return_code");
        if (FAIL.equals(return_code)) {
            //通讯失败
            log.error("[微信下单] 微信下单通知失败，失败原因:{}", result.get("return_msg"));
            throw new LyException(ExceptionEnums.WXPAY_ORDER_ERROR);
        }
        //判断业务标识
        String result_code = result.get("result_code");
        if (FAIL.equals(result_code)) {
            //通信失败
            log.error("[微信下单] 微信下单通知失败，错误码:{},错误描述:{}"
                    , result.get("err_code"), result.get("err_code_des"));
            throw new LyException(ExceptionEnums.WXPAY_ORDER_ERROR);
        }
    }


    public void isValidSign(Map<String, String> data) {
        try {
            //重新生成签名
            String sign1 = WXPayUtil.generateSignature(data, config.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(data, config.getKey(), WXPayConstants.SignType.MD5);
            //和传过来的签名进行比较
            String sign = data.get("sign");
            if (!StringUtils.equals(sign1, sign) && !StringUtils.equals(sign2, sign)) {
                throw new LyException(ExceptionEnums.SIGN_VALIDATE_ERROR);
            }
        } catch (Exception e) {
            throw new LyException(ExceptionEnums.SIGN_VALIDATE_ERROR);
        }
    }

    public PatStatus queryPayStatus(Long id)  {
        try {
            //组值请求参数
            Map<String, String> data = new HashMap<>();
            //订单号
            data.put("out_trade_no", id.toString());
            Map<String, String> result = wxPay.orderQuery(data);
            //校验状态
            isSuccess(result);
            //校验签名
            isValidSign(result);

            //3 、校验金额
            String totalFeeStr = result.get("total_fee");
            //订单号
            String tradNo = result.get("out_trade_no");
            if (StringUtils.isBlank(totalFeeStr) || StringUtils.isBlank(tradNo)) {
                throw new LyException(ExceptionEnums.TOTALFEESTR_OR_TRADNO_NOT_FIND);
            }
            //获取结果中的金额
            Long totalFee = Long.valueOf(totalFeeStr);
            //获取订单中的金额
            Long orderId = Long.valueOf(tradNo);
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if (totalFee != 1L/*order.getActualPay()*/) {
                //金额不足
                throw new LyException(ExceptionEnums.TOTALFEESTR_OR_TRADNO_NOT_FIND);
            }
            String trade_state = result.get("trade_state");
            if(SUCCESS.equals(trade_state)){
                //支付成功，修改订单状态
                OrderStatus status = new OrderStatus();
                status.setStatus(OrderStatusEnum.PAYED.value());
                status.setOrderId(orderId);//修改订单id
                status.setPaymentTime(new Date());
                //写回到数据库
                int count = statusMapper.updateByPrimaryKeySelective(status);
                if (count != 1) {
                    throw new LyException(ExceptionEnums.STATUS_UPDATE_ERROR);
                }
                //返回成功
                return PatStatus.SUCCESS;
            }
            if("NOTPAY".equals(trade_state) || "USERPAYING".equals(trade_state)){
                return PatStatus.NON_PAY;
            }
            return PatStatus.FAIL;
        }catch (Exception e){
            return PatStatus.NON_PAY;
        }
    }
}
