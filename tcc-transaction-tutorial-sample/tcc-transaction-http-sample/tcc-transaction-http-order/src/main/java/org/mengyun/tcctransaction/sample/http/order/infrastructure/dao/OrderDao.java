package org.mengyun.tcctransaction.sample.http.order.infrastructure.dao;

import org.mengyun.tcctransaction.sample.http.order.domain.entity.Order;

/**
 * Created by changming.xie on 4/1/16.
 */
public interface OrderDao {

    public void insert(Order order);

    public void update(Order order);
    //使用merchantOrderNo查询订单
    Order findByMerchantOrderNo(String merchantOrderNo);
}
