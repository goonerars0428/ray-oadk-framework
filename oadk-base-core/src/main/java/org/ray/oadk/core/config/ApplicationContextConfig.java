package org.ray.oadk.core.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @auth congr@inspur.com
 * @date 2019/4/18
 * @description 获取spring管理的bean配置
 */
@Component("ApplicationContextConfig")
public class ApplicationContextConfig implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        if (ApplicationContextConfig.applicationContext == null) {
            ApplicationContextConfig.applicationContext = applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
