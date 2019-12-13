package org.mengyun.tcctransaction.sample.http.order.domain.entity;

/**
 * 商店表
 *
 * Created by changming.xie on 4/1/16.
 */
public class Shop {

    /**
     * 商店编号
     */
    private long id;
    /**
     * 所有者用户编号
     */
    private long ownerUserId;

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public long getId() {
        return id;
    }
}
