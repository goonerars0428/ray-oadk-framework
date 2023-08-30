package org.ray.data.mysql.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.ray.data.mysql.annotation.DataSource;
import org.ray.data.mysql.core.DataSourceRouting;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知增强
 */
@Component
public class DataSourceChangeInterceptor implements MethodInterceptor {

    /**
     * 存储方法和对应数据源
     */
    private static final Map<Method, String> METHOD_CACHE = new HashMap<>();

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        //获取方法指定的数据源名称
        String dataSource = determineDataSource(methodInvocation);
        //设置当前线程应用指定的数据源
        DataSourceRouting.setDataSourceRouterKey(dataSource);
        //执行方法
        Object proceed = methodInvocation.proceed();
        //执行方法后移除指定的数据源
        DataSourceRouting.removeDataSourceRouterKey();
        return proceed;
    }

    /**
     * 获取方法上的dataSource注解，判定方法要使用的数据源，如果方法上没有dataSource注解，获取方法所在类上的dataSource注解
     *
     * @param invocation
     * @return
     */
    private String determineDataSource(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        if (METHOD_CACHE.containsKey(method)) {
            return METHOD_CACHE.get(method);
        } else {
            DataSource ds = method.isAnnotationPresent(DataSource.class) ? method.getAnnotation(DataSource.class) :
                    AnnotationUtils.findAnnotation(method.getDeclaringClass(), DataSource.class);
            METHOD_CACHE.put(method, ds.key());
            return ds.key();
        }
    }
}
