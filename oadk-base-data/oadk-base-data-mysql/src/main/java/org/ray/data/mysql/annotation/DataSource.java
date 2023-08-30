package org.ray.data.mysql.annotation;

import java.lang.annotation.*;

/**
 * 指定方法、接口使用的数据源
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DataSource {
    //指定数据源名称
    String key() default "";

    /*
    #使用示例
    @DataSource(value = "demo")
    public void demo() {
    }*/
}
