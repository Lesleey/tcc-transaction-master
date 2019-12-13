package org.mengyun.tcctransaction;

/**
 * TCC Confirm 执行异常
 *
 * Created by changming.xie on 7/21/16.
 */
public class ConfirmingException extends RuntimeException {

    public ConfirmingException(Throwable cause) {
        super(cause);
    }
}
