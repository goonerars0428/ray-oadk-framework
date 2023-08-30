package org.ray.data.mysql.aop;

import org.aopalliance.aop.Advice;
import org.ray.data.mysql.annotation.DataSource;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * 注册切面，应用切点和通知增强
 * 使用spring aop
 */
@Component
public class DataSourceChangeAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

    /**
     * 通知
     */
    private Advice advice;

    /**
     * 切点
     */
    private Pointcut pointcut;

    public DataSourceChangeAdvisor(DataSourceChangeInterceptor dataSourceChangeInterceptor) {
        this.advice = dataSourceChangeInterceptor;
        this.pointcut = buildPointcut();
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }

    /**
     * 构建切点，方和和类上有dataSource注解
     *
     * @return
     */
    private Pointcut buildPointcut() {

        Pointcut cpc = new AnnotationMatchingPointcut(DataSource.class, true);
        //类注解
        Pointcut ccpc = AnnotationMatchingPointcut.forClassAnnotation(DataSource.class);
        //方法注解
        Pointcut mcpc = AnnotationMatchingPointcut.forMethodAnnotation(DataSource.class);
        return new ComposablePointcut(cpc).union(ccpc).union(mcpc);
    }
}
