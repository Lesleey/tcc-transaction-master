package org.mengyun.tcctransaction.sample.http.redpacket.domain.entity;

import java.math.BigDecimal;

/**
 * 红包账户余额
 *
 * Created by changming.xie on 4/2/16.
 */
public class RedPacketAccount {

    /**
     * 账户编号
     */
    private long id;
    /**
     * 用户编号
     */
    private long userId;
    /**
     * 余额
     */
    private BigDecimal balanceAmount;

    public long getUserId() {
        return userId;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void transferFrom(BigDecimal amount) {
        this.balanceAmount = this.balanceAmount.subtract(amount);

        if (BigDecimal.ZERO.compareTo(this.balanceAmount) > 0) {
            throw new RuntimeException("not enough balance!");
        }
    }

    public void transferTo(BigDecimal amount) {
        this.balanceAmount = this.balanceAmount.add(amount);
    }

    public void cancelTransfer(BigDecimal amount) {
        transferTo(amount);
    }
}
