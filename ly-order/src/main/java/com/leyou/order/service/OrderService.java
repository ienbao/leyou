package com.leyou.order.service;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.auth.entity.UserInfo;

import com.leyou.common.dto.CartDto;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.config.PayConfig;
import com.leyou.order.config.WXPayConfiguration;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PatStatus;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.WXPayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper detailMapper;
    @Autowired
    private OrderStatusMapper statusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private WXPayHelper payHelper;
    @Autowired
    private PayConfig config;

    @Transactional
    public Long createOrder(OrderDto orderDto) {
        //1、新增订单
        Order order = new Order();
        //1.1 订单编号，基本信息
        long orderId = idWorker.nextId();//订单编号
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDto.getPaymentType());

        //1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());//用户id
        order.setBuyerNick(user.getUsername());//用户的昵称
        order.setBuyerRate(false);//买家是否评价
        //1.3 收货人地址
        AddressDTO addr = AddressClient.findById(orderDto.getAddressId());
        order.setReceiver(addr.getName());//收货人姓名
        order.setReceiverAddress(addr.getAddress());//收货人地址
        order.setReceiverState(addr.getState());//收货人的省份
        order.setReceiverCity(addr.getCity());//收货人的城市
        order.setReceiverDistrict(addr.getDistrict());//收货人的街区
        order.setReceiverZip(addr.getZipCode());//收货人的邮编
        order.setReceiverMobile(addr.getPhone());//收货人的手机号

        //1.4 金额
        Map<Long, Integer> numMap = orderDto.getCarts().stream().collect(Collectors.toMap(CartDto::getSkuId, CartDto::getNum));

        Set<Long> ids = numMap.keySet();
        List<Sku> skus = goodsClient.querySkuBySpuIds(new ArrayList<>(ids));
        long totalPay = 0L;
        //准备orderDetails
        List<OrderDetail> details = new ArrayList<>();

        for (Sku sku : skus) {
            totalPay += sku.getPrice() * numMap.get(sku.getId());
            //封装orderDetails
            OrderDetail detail = new OrderDetail();
            detail.setNum(numMap.get(sku.getId()));//获取订单详情数量
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setTitle(sku.getTitle());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getSpuId());
            details.add(detail);
        }
        order.setTotalPay(totalPay);
        order.setPostFee(totalPay + order.getPostFee() - 0);
        //1.5 订单写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1) {
            log.error("[创建订单] 创建订单失败，orderId:{}", orderId);
            throw new LyException(ExceptionEnums.CREATE_ORDER_FAILE);
        }
        //2 新增订单详情
        count = detailMapper.insertList(details);
        if (count != details.size()) {
            log.error("[创建订单] 创建订单失败,orderId:{}", orderId);
            throw new LyException(ExceptionEnums.CREATE_ORDER_FAILE);
        }
        //3 新增订单状态
        OrderStatus status = new OrderStatus();
        status.setOrderId(orderId);
        status.setCreateTime(new Date());
        status.setStatus(OrderStatusEnum.PAYED.value());
        count = statusMapper.insertSelective(status);
        if (count != 1) {
            log.error("[创建订单] 创建订单失败，orderId:{}", orderId);
            throw new LyException(ExceptionEnums.CREATE_ORDER_FAILE);
        }
        //4 减库存
        List<CartDto> carts = orderDto.getCarts();
        goodsClient.decreaseStock(carts);


        return orderId;
    }

    public Order queryOrderById(Long id) {
//        Order recoder = new Order();
//        recoder.setOrderId(id);
//        Order order = orderMapper.selectOne(recoder);
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnums.ORDER_CONNOT_FIND);
        }
        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = detailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)) {
            throw new LyException(ExceptionEnums.ORDER_DETAIL_NOT_FIND);
        }
        order.setOrderDetails(details);
        //查询订单状态

        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            //不存在
            throw new LyException(ExceptionEnums.ORDERSTATUS_NOT_FIND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    /**
     * 创建支付链接
     *
     * @param id
     * @return
     */
    public String createPayUrl(Long id) {
        //查询支付价格
        Order order = queryOrderById(id);
        //判断订单的状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.value()) {
            //订单状态异常
            throw new LyException(ExceptionEnums.ORDER_STATUS_ERROR);
        }
        //支付金额
        Long actualPay = order.getActualPay();
        //查询商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String title = detail.getTitle();

        //创建url
        String url = payHelper.createOrder(id, actualPay, title);
        return url;
    }

    /**
     * 更新订单状态
     * @param result
     */
    public void handleNotify(Map<String, String> result) {
        //数据校验
        payHelper.isSuccess(result);
        //校验签名
        payHelper.isValidSign(result);

        //校验金额
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
        if (totalFee != 1L/*order.getActualPay()*/){
            //金额不足
            throw new LyException(ExceptionEnums.TOTALFEESTR_OR_TRADNO_NOT_FIND);
        }
        //修改订单的状态
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAYED.value());
        status.setOrderId(orderId);//修改订单id
        status.setPaymentTime(new Date());
        //写回到数据库
        int count = statusMapper.updateByPrimaryKeySelective(status);
        if (count != 1){
            throw new LyException(ExceptionEnums.STATUS_UPDATE_ERROR);
        }
        log.info("[微信回调] 微信支付成功,订单编号:{}",orderId);
    }

    /**
     * 根据订单的id查询订单的状态
     * @param id
     * @return
     */
    public PatStatus queryStatusById(Long id) {
        //查询订单状态
        OrderStatus orderStatus = statusMapper.selectByPrimaryKey(id);
        Integer status = orderStatus.getStatus();
        //判断是否支付
        if (status != OrderStatusEnum.UN_PAY.value()){
            //如果已支付，是真的已支付
            return PatStatus.SUCCESS;
        }
        //如果未支付不一定是未支付,必须去微信查询支付状态

        return  payHelper.queryPayStatus(id);
    }
}
