package org.mengyun.tcctransaction.sample.http.order.domain.service;

import org.apache.commons.lang3.tuple.Pair;
import org.mengyun.tcctransaction.sample.http.order.domain.entity.Order;
import org.mengyun.tcctransaction.sample.http.order.domain.factory.OrderFactory;
import org.mengyun.tcctransaction.sample.http.order.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单服务实现
 * <p>
 * Created by changming.xie on 3/25/16.
 */
@Service
public class OrderServiceImpl {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderFactory orderFactory;

    /**
     * 创建商城订单
     *
     * @param payerUserId 支付用户编号
     * @param payeeUserId 收款用户编号
     * @param productQuantities 购买商品映射。key：商品编号；value：商品数量
     * @return 商城订单
     */
    @Transactional
    public Order createOrder(long payerUserId, long payeeUserId, List<Pair<Long, Integer>> productQuantities) {
        Order order = orderFactory.buildOrder(payerUserId, payeeUserId, productQuantities);
        orderRepository.createOrder(order);
        return order;
    }

    /**
     * 查询商城订单状态
     *
     * @param orderNo 商户订单号
     * @return 订单状态
     */
    public String getOrderStatusByMerchantOrderNo(String orderNo) {
        return orderRepository.findByMerchantOrderNo(orderNo).getStatus();
    }

}
