package com.lagou.rpc.consumer.processor;

import com.lagou.rpc.consumer.anno.RpcReference;
import com.lagou.rpc.consumer.proxy.RpcClientProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    RpcClientProxy rpcClientProxy;

    // 前置处理器
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    // 后置处理器 --- 此处具体的实现用的是Java中的动态代理
    public Object postProcessAfterInitialization(final Object beanInstance, String beanName) throws BeansException {
        // 为当前 bean 对象注册监控代理对象，负责增强 bean 对象方法的能力
        //1.获得Class对象
        Class<?> aClass = beanInstance.getClass();
        //2.通过Class对象获取属性
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            //3.获得字段声明的注解
            RpcReference annotation = declaredField.getAnnotation(RpcReference.class);
            if (annotation != null) {
                //4.创建代理对象
                Object proxy = rpcClientProxy.createProxy(declaredField.getType());
                try {
                    //5.设置属性值
                    declaredField.setAccessible(true);
                    declaredField.set(beanInstance, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        //6.返回bean
        return beanInstance;
    }
}