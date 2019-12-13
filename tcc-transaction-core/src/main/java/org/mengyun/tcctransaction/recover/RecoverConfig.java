package org.mengyun.tcctransaction.recover;

import java.util.Set;

/**
 * 事务恢复配置接口
 *
 * Created by changming.xie on 6/1/16.
 */
public interface RecoverConfig {

    /**
     * @return 最大重试次数
     */
    int getMaxRetryCount();

    /**
     * @return 恢复间隔时间，单位：秒
     */
    int getRecoverDuration();

    /**
     * @return cron 表达式
     */
    String getCronExpression();

    /**
     * @return 延迟取消异常集合
     */
    Set<Class<? extends Exception>> getDelayCancelExceptions();

    /**
     * 设置延迟取消异常集合
     *
     * @param delayRecoverExceptions 延迟取消异常集合
     */
    void setDelayCancelExceptions(Set<Class<? extends Exception>> delayRecoverExceptions);
}
