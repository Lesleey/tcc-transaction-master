package org.mengyun.tcctransaction.server.dao;

import org.mengyun.tcctransaction.server.vo.TransactionVo;

import java.util.List;

/**
 * 事务Dao 接口
 *
 * Created by changming.xie on 9/7/16.
 */
public interface TransactionDao {

    /**
     * 获得事务 VO 数组
     *
     * @param domain 领域
     * @param pageNum 第几页
     * @param pageSize 分页大小
     * @return 事务 VO 数组
     */
    List<TransactionVo> findTransactions(String domain, Integer pageNum, int pageSize);

    /**
     * 获得事务总数量
     *
     * @param domain 领域
     * @return 数量
     */
    Integer countOfFindTransactions(String domain);

    /**
     * 重置事务重试次数
     *
     * @param domain 领域
     * @param globalTxId 全局事务编号
     * @param branchQualifier 分支事务编号
     * @return 是否重置成功
     */
    boolean resetRetryCount(String domain, byte[] globalTxId, byte[] branchQualifier);
}

