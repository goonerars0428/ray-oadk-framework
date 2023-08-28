package org.ray.data.mysql.annotation;

import java.lang.annotation.*;
import java.sql.Connection;

/**
 * 跨数据源事务注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TransactionMulti {
    String[] dataSourceKey() default {};

    int transactionType() default Connection.TRANSACTION_READ_UNCOMMITTED;


    /*
    #使用示例
    @TransactionMulti(value={"demo1","demo2"},transactionType=2)
    public void demo() {

    }*/
}
