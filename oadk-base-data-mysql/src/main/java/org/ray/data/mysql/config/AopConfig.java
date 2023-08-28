package org.ray.data.mysql.config;

import org.ray.data.mysql.aop.MultiTransactionManagerAop;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {


    /**
     * 注册多数据源事务至spring管理
     * @return
     */
    @Bean
    public MultiTransactionManagerAop multiTransactionManagerAop() {
        MultiTransactionManagerAop multiTransactionManagerAop = new MultiTransactionManagerAop();
        return multiTransactionManagerAop;
    }
}
