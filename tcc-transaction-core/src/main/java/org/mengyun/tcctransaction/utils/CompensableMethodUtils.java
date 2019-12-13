package org.mengyun.tcctransaction.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mengyun.tcctransaction.api.Compensable;
import org.mengyun.tcctransaction.api.Propagation;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.common.MethodType;

import java.lang.reflect.Method;

/**
 * Created by changmingxie on 11/21/15.
 */
public class CompensableMethodUtils {

    /**
     * 从切点获得带 @Compensable 注解的方法
     *
     * @param pjp 切面点
     * @return 方法
     */
    public static Method getCompensableMethod(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) (pjp.getSignature())).getMethod(); // 代理方法对象
        if (method.getAnnotation(Compensable.class) == null) {
            try {
                method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes()); // 实际方法对象
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
        return method;
    }

    /**
     * 计算方法类型
     *
     * @param propagation 传播级别
     * @param isTransactionActive 是否事务开启
     * @param transactionContext 事务上下文
     * @return 方法类型
     */
    public static MethodType calculateMethodType(Propagation propagation, boolean isTransactionActive, TransactionContext transactionContext) {
        //如果事务传播级别为required， 且当前没有事务且transactionContext为null | 传播级别为required_new 则是根节点方法
        if ((propagation.equals(Propagation.REQUIRED) && !isTransactionActive && transactionContext == null)
                || propagation.equals(Propagation.REQUIRES_NEW)) {
            return MethodType.ROOT;
        //如果当前节点是required | manatory 且没有当前不存在事务 且transactionContext不为null 则为提供者方法类型
        } else if ((propagation.equals(Propagation.REQUIRED)
                    || propagation.equals(Propagation.MANDATORY))
                && !isTransactionActive && transactionContext != null) {
            return MethodType.PROVIDER;
        } else {
            return MethodType.NORMAL;
        }
    }

    public static MethodType calculateMethodType(TransactionContext transactionContext, boolean isCompensable) {
        if (transactionContext == null && isCompensable) {
            //isRootTransactionMethod
            return MethodType.ROOT;
        } else if (transactionContext == null && !isCompensable) {
            //isSoaConsumer
            return MethodType.CONSUMER;
        } else if (transactionContext != null && isCompensable) {
            //isSoaProvider
            return MethodType.PROVIDER;
        } else {
            return MethodType.NORMAL;
        }
    }

    public static int getTransactionContextParamPosition(Class<?>[] parameterTypes) {
        int position = -1;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(org.mengyun.tcctransaction.api.TransactionContext.class)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public static TransactionContext getTransactionContextFromArgs(Object[] args) {
        TransactionContext transactionContext = null;
        for (Object arg : args) {
            if (arg != null && org.mengyun.tcctransaction.api.TransactionContext.class.isAssignableFrom(arg.getClass())) {

                transactionContext = (org.mengyun.tcctransaction.api.TransactionContext) arg;
            }
        }
        return transactionContext;
    }

}
