package org.mengyun.tcctransaction;

import org.apache.log4j.Logger;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.common.TransactionType;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 事务管理器，管理所有新建的事务
 *
 * Created by changmingxie on 10/26/15.
 */
public class TransactionManager {

    static final Logger logger = Logger.getLogger(TransactionManager.class.getSimpleName());

    /**
     * 事务仓库，用来存取数据库的事务记录：jdbc、redis、zk等
     */
    private TransactionRepository transactionRepository;
    /**
     * 当前线程事务队列
     */
    private static final ThreadLocal<Deque<Transaction>> CURRENT = new ThreadLocal<Deque<Transaction>>();

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * 发起根事务
     *
     * @return 事务
     */
    public Transaction begin() {
        // 1. 创建 根事务，事务状态为try
        Transaction transaction = new Transaction(TransactionType.ROOT);
        // 2. 将该事务存储到数据库中
        transactionRepository.create(transaction);
        // 3.  将事务添加到双端队列中
        registerTransaction(transaction);
        return transaction;
    }

    /**
     * 传播发起分支事务
     *
     * @param transactionContext 事务上下文
     * @return 分支事务
     */
    public Transaction propagationNewBegin(TransactionContext transactionContext) {
        //1. 创建分支事务
        Transaction transaction = new Transaction(transactionContext);
        transactionRepository.create(transaction);
        //2. 将事务注册导本地线程中
        registerTransaction(transaction);
        return transaction;
    }

    /**
     * 传播获取分支事务
     *
     * @param transactionContext 事务上下文
     * @return 分支事务
     * @throws NoExistedTransactionException 当事务不存在时
     */
    public Transaction propagationExistBegin(TransactionContext transactionContext) throws NoExistedTransactionException {
        // 1. 从数据库中查询该根事务对应的分支事务
        Transaction transaction = transactionRepository.findByXid(transactionContext.getXid());
        if (transaction != null) {
            // 2. 设置事务状态
            transaction.changeStatus(TransactionStatus.valueOf(transactionContext.getStatus()));
            // 3. 注册事务到本地线程的双端队列中
            registerTransaction(transaction);
            return transaction;
        } else {
            throw new NoExistedTransactionException();
        }
    }

    /**
     * 提交事务
     */
    public void commit() {
        // 1. 获取当前事务
        Transaction transaction = getCurrentTransaction();
        // 2. 设置当前的事务状态 为 CONFIRMING 并更新到数据库
        transaction.changeStatus(TransactionStatus.CONFIRMING);
        transactionRepository.update(transaction);
        try {
            //3. 提交事务
            transaction.commit();
            //4. 事务完成，从数据库中删除该事务记录
            transactionRepository.delete(transaction);
        } catch (Throwable commitException) {
            logger.error("compensable transaction confirm failed.", commitException);
            throw new ConfirmingException(commitException);
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        // 1. 获取当前事务
        Transaction transaction = getCurrentTransaction();
        // 2. 修改事务状态为 CANCELLING 并更新到数据库
        transaction.changeStatus(TransactionStatus.CANCELLING);
        transactionRepository.update(transaction);
        try {
            //3. 回滚事务
            transaction.rollback();
            //4. 从数据库中删除该记录
            transactionRepository.delete(transaction);
        } catch (Throwable rollbackException) {
            logger.error("compensable transaction rollback failed.", rollbackException);
            throw new CancellingException(rollbackException);
        }
    }

    /**
     * 注册事务到当前线程事务队列
     *
     * @param transaction 事务
     */
    private void registerTransaction(Transaction transaction) {
        if (CURRENT.get() == null) {
            CURRENT.set(new LinkedList<Transaction>());
        }
        CURRENT.get().push(transaction); // 添加到头部
    }

    /**
     * 获取当前线程事务第一个(头部)元素
     *
     * @return 事务
     */
    public Transaction getCurrentTransaction() {
        if (isTransactionActive()) {
            return CURRENT.get().peek(); // 获得头部元素
        }
        return null;
    }

    /**
     * 当前线程是否在事务中
     *
     * @return 是否在事务中
     */
    public boolean isTransactionActive() {
        Deque<Transaction> transactions = CURRENT.get();
        return transactions != null && !transactions.isEmpty();
    }

    /**
     * 将事务从当前线程事务队列移除
     *
     * @param transaction 事务
     */
    public void cleanAfterCompletion(Transaction transaction) {
        if (isTransactionActive() && transaction != null) {
            Transaction currentTransaction = getCurrentTransaction();
            if (currentTransaction == transaction) {
                CURRENT.get().pop();
            } else {
                throw new SystemException("Illegal transaction when clean after completion");
            }
        }
    }

    /**
     * 添加参与者到事务
     *
     * @param participant 参与者
     */
    public void enlistParticipant(Participant participant) {
        // 获取 事务
        Transaction transaction = this.getCurrentTransaction();
        // 添加参与者
        transaction.enlistParticipant(participant);
        // 更新 事务
        transactionRepository.update(transaction);
    }
}
