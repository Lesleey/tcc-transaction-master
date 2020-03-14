package org.mengyun.tcctransaction.sample.http.order.service;

import org.apache.commons.lang3.tuple.Pair;
import org.mengyun.tcctransaction.CancellingException;
import org.mengyun.tcctransaction.ConfirmingException;
import org.mengyun.tcctransaction.sample.http.order.domain.entity.Order;
import org.mengyun.tcctransaction.sample.http.order.domain.entity.Shop;
import org.mengyun.tcctransaction.sample.http.order.domain.repository.ShopRepository;
import org.mengyun.tcctransaction.sample.http.order.domain.service.OrderServiceImpl;
import org.mengyun.tcctransaction.sample.http.order.domain.service.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付订单服务实现
 *
 * Created by changming.xie on 4/1/16.
 */
@Service
public class PlaceOrderServiceImpl {

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    OrderServiceImpl orderService;

    @Autowired
    PaymentServiceImpl paymentService;

    public String placeOrder(long payerUserId, long shopId, List<Pair<Long, Integer>> productQuantities, BigDecimal redPacketPayAmount) {
        //1. 构建支付订单实体
        Shop shop = shopRepository.findById(shopId);
        Order order = orderService.createOrder(payerUserId, shop.getOwnerUserId(), productQuantities);
        Boolean result = false;
        try {
            //2. 进入支付主流程
            paymentService.makePayment(order, redPacketPayAmount, order.getTotalAmount().subtract(redPacketPayAmount));
        } catch (ConfirmingException confirmingException) {
            //当事务状态为confirm时抛出异常,tcc-transaction框架会自动重试保证数据一致性
            result = true;
        } catch (CancellingException cancellingException) {
            //当事务状态为cancel时抛出异常,tcc-transaction框架会自动重试保证数据一致性
        } catch (Throwable e) {
            // other exceptions throws at TRYING stage.
            // you can retry or cancel the operation.
            e.printStackTrace();
        }
        return order.getMerchantOrderNo();
    }

}
