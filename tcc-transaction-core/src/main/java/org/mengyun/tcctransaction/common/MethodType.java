package org.mengyun.tcctransaction.common;

/**
 * 方法类型
 *
 * Created by changmingxie on 11/11/15.
 */
public enum  MethodType {

    /**
     * ROOT
     */
    ROOT,
    @Deprecated
    CONSUMER,
    /**
     * PROVIDER
     */
    PROVIDER,
    /**
     * NORMAL
     */
    NORMAL
}
