package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.ware.WareOrderTask;
import com.atguigu.gmall.model.ware.WareOrderTaskDetail;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RabbitService rabbitService;

    @Transactional
    @Override
    public String save(OrderInfo order) {

        order.setProcessStatus(ProcessStatus.UNPAID.getComment());
        order.setOrderStatus(OrderStatus.UNPAID.getComment());
        List<OrderDetail> orderDetailList = order.getOrderDetailList();
        order.setTotalAmount(getTotalAmount(orderDetailList));
        order.setOrderComment("尚硅谷测试订单");
        String timeMills = System.currentTimeMillis()+"";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDDHHmmss");
        String format = sdf.format(new Date());
        String outTradeNo = "atguigu"+format;
        order.setOutTradeNo(outTradeNo);
        order.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        Date time = calendar.getTime();
        order.setExpireTime(time);
        order.setImgUrl(orderDetailList.get(0).getImgUrl());
        order.setPaymentWay(PaymentWay.ONLINE.getComment());


        orderInfoMapper.insert(order);

        Long id = order.getId();

        for (OrderDetail orderDetail : orderDetailList) {

            // 验价，验库存
//            SkuInfo skuInfoById = productFeignClient.getSkuInfoById(orderDetail.getSkuId());
//            BigDecimal price = skuInfoById.getPrice();
//            BigDecimal orderPrice = orderDetail.getOrderPrice();
//            int i = price.compareTo(orderPrice);
//            if(i!=0){
//                return null;
//            }
            orderDetail.setOrderId(id);
            orderDetailMapper.insert(orderDetail);
        }

        // 删除购物车的订单数据
        // cartFeignClient.delCart();

        return id+"";
    }

    @Override
    public String genTradeNo(String userId) {

        String tradeNo = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(RedisConst.USER_KEY_PREFIX+userId+":tradeNo",tradeNo);

        return tradeNo;
    }

    @Override
    public boolean checkTradeNo(String userId, String tradeNo) {

        boolean b = false;

        String tradeNoCache = (String)redisTemplate.opsForValue().get(RedisConst.USER_KEY_PREFIX+userId+":tradeNo");

        if(!StringUtils.isEmpty(tradeNoCache)&&tradeNoCache.equals(tradeNo)){
            b = true;
            redisTemplate.delete(RedisConst.USER_KEY_PREFIX+userId+":tradeNo");
        }

        return b;
    }

    @Override
    public OrderInfo getOrderById(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    @Override
    public void updateOrder(Map<String, Object> map) {

        // 修改订单状态
        Long order_id = Long.parseLong(map.get("order_id")+"");

        OrderInfo orderInfo = new OrderInfo();

        orderInfo.setId(order_id);
        orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
        orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());

        orderInfoMapper.updateById(orderInfo);

        // 发送消息队列通知库存系统，锁定订单任务中的库存
        // 将orderInfo转化为wareOrderTask订单任务对象
        OrderInfo orderById = getOrderById(order_id);

        WareOrderTask wareOrderTask = new WareOrderTask();

        wareOrderTask.setDeliveryAddress(orderById.getDeliveryAddress());
        wareOrderTask.setOrderId(orderById.getId()+"");
        wareOrderTask.setConsignee(orderById.getConsignee());
        wareOrderTask.setConsigneeTel(orderById.getConsigneeTel());
        wareOrderTask.setCreateTime(new Date());
        wareOrderTask.setPaymentWay(com.atguigu.gmall.model.ware.enums.PaymentStatus.PAID.name());
        wareOrderTask.setTrackingNo(orderById.getTrackingNo());
        wareOrderTask.setOrderBody(orderById.getTradeBody());
        

        List<WareOrderTaskDetail> wareOrderTaskDetails = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderById.getOrderDetailList();


        for (OrderDetail orderDetail : orderDetailList) {
            WareOrderTaskDetail wareOrderTaskDetail = new WareOrderTaskDetail();
            wareOrderTaskDetail.setSkuId(orderDetail.getSkuId()+"");
            wareOrderTaskDetail.setSkuNum(orderDetail.getSkuNum());
            wareOrderTaskDetail.setSkuName(orderDetail.getSkuName());
            wareOrderTaskDetails.add(wareOrderTaskDetail);

        }

        wareOrderTask.setDetails(wareOrderTaskDetails);

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_WARE_STOCK,MqConst.ROUTING_WARE_STOCK, JSON.toJSONString(wareOrderTask));

    }

    private BigDecimal getTotalAmount(List<OrderDetail> orderDetailList) {

        BigDecimal totalAmount = new BigDecimal("0");

        for (OrderDetail orderDetail : orderDetailList) {
            BigDecimal orderPrice = orderDetail.getOrderPrice();

            totalAmount = totalAmount.add(orderPrice);
        }

        return totalAmount;

    }
}
