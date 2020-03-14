package org.mengyun.tcctransaction.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 反射工具类
 *
 * Created by changmingxie on 11/22/15.
 */
public class ReflectionUtils {

    //使当前方法可以被访问
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }

    /**
     * 设置注解属性值
     *
     * @param annotation 注解
     * @param key 属性
     * @param newValue 新值
     * @return 老值
     * @throws NoSuchFieldException 当属性不存在时
     * @throws SecurityException 当 SecurityException
     * @throws IllegalArgumentException 当参数不正确时
     * @throws IllegalAccessException 当不允许访问时
     */
    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field f = handler.getClass().getDeclaredField("memberValues");
        f.setAccessible(true);
        Map<String, Object> memberValues;
        memberValues = (Map<String, Object>) f.get(handler);
        // 属性不存在 或 属性类型不正确
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        // 设置
        memberValues.put(key, newValue);
        return oldValue;
    }

    //获取声明某方法的实际类
    public static Class getDeclaringType(Class aClass, String methodName, Class<?>[] parameterTypes) {
        Method method;
        Class findClass = aClass;
        do {
            Class[] clazzes = findClass.getInterfaces();
            for (Class clazz : clazzes) {
                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e) {
                    method = null;
                }
                if (method != null) {
                    return clazz;
                }
            }
            findClass = findClass.getSuperclass();
        } while (!findClass.equals(Object.class));
        return aClass;
    }

    public static Object getNullValue(Class type) {
        // 处理基本类型
        if (boolean.class.equals(type)) {
            return false;
        } else if (byte.class.equals(type)) {
            return 0;
        } else if (short.class.equals(type)) {
            return 0;
        } else if (int.class.equals(type)) {
            return 0;
        } else if (long.class.equals(type)) {
            return 0;
        } else if (float.class.equals(type)) {
            return 0;
        } else if (double.class.equals(type)) {
            return 0;
        }
        // 处理对象
        return null;
    }
}
