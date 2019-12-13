package org.mengyun.tcctransaction;

/**
 * TCC Cancel 执行异常
 *
 * Created by changming.xie on 7/21/16.
 */
public class CancellingException extends RuntimeException {

    public CancellingException(Throwable cause) {
        super(cause);
    }

}
