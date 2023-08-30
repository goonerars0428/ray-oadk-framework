package org.ray.data.mysql.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.ray.data.mysql.core.DataSourceRouting;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auth ray_cong
 * @date 2019/12/21 15:06
 * @description 动态数据源注册器，服务启动时启动多数据源注册器
 */
public class MultiDataSourceRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware {


    /**
     * 配置上下文，理解为配置文件的获取工具
     */
    private Environment env;

    /**
     * 别名
     */
    private final static ConfigurationPropertyNameAliases aliases = new ConfigurationPropertyNameAliases();

    /**
     * 不同数据源配置不同，使用别名统一处理
     */
    static {
        aliases.addAliases("url", new String[]{"jdbc-url"});
        aliases.addAliases("username", new String[]{"user"});
    }

    /**
     * 存储自定义注册的数据源
     */
    private Map<String, DataSource> customDatasources = new HashMap<>();

    /**
     * 参数绑定工具，springboot2.0使用
     */
    private Binder binder;

    /**
     * EnvironmentAware接口的实现方法，通过aware方式注入environment
     *
     * @param environment
     */
    @Override
    public void setEnvironment(Environment environment) {

        System.out.print("开始注册数据源");
        //配置信息赋值
        this.env = environment;
        //绑定配置器
        binder = Binder.get(env);
    }

    /**
     * ImportBeanDefinitionRegistrar接口的实现方法，通过该方法可以按照自己的方式注册bean
     *
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        //获取默认数据源所有配置
        Map defaultDataSourceProperties = binder.bind("spring.datasource.druid", Map.class).get();
        //获取默认数据源类型
        String dataSourceTypeStr = env.getProperty("spring.datasource.type");
        Class<? extends DataSource> dataSourceType = getDataSourceType(dataSourceTypeStr);
        //绑定默认数据源参数
        DataSource defaultDataSource = bind(dataSourceType, defaultDataSourceProperties);
        //存储默认数据源注册信息
        DataSourceRouting.setDataSource("default", defaultDataSource);


        //获取自定义数据源配置
        List<Map> configs = binder.bind("spring.datasource.custom", Bindable.listOf(Map.class)).get();
        //遍历其他数据源
        for (Map config : configs) {
            //获取自定义数据源类型
            dataSourceTypeStr = (String) config.get("type");
            dataSourceType = getDataSourceType(dataSourceTypeStr);
            //使用自定义配置覆盖默认配置，如果没有自定义，使用默认配置
            defaultDataSourceProperties.putAll(config);
            //绑定自定义数据源参数
            DataSource customDataSource = bind(dataSourceType, defaultDataSourceProperties);
            //获取自定义数据源的key，通过key快速定位数据源
            String key = config.get("key").toString();
            customDatasources.put(key, customDataSource);
            //存储自定义数据源注册信息
            DataSourceRouting.setDataSource(key, customDataSource);
        }

        //bean定义类
        GenericBeanDefinition define = new GenericBeanDefinition();
        //设置bean的类型
        define.setBeanClass(DataSourceRouting.class);
        //需要注入的参数
        MutablePropertyValues propertyValues = define.getPropertyValues();
        //添加默认数据源，避免key不存在的时候无数据源可用
        propertyValues.add("defaultTargetDataSource", defaultDataSource);
        //添加自定义数据源
        propertyValues.add("targetDataSources", customDatasources);
        //将自定义bean注册为dataSource，不使用springboot自动生成的dataSource
        registry.registerBeanDefinition("datasource", define);

    }

    /**
     * 绑定参数，以下三个方法都是参考DataSourceBuilder的bind方法实现的，目的是尽量保证我们自己添加的数据源构造过程与springboot保持一致
     * 根据Class对象和properties绑定
     *
     * @param dataSourceType
     * @param defaultDataSourceProperties
     * @param <T>
     * @return
     */
    private <T extends DataSource> T bind(Class<T> dataSourceType, Map defaultDataSourceProperties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(defaultDataSourceProperties);
        Binder binder = new Binder(new ConfigurationPropertySource[]{source.withAliases(aliases)});
        return binder.bind(ConfigurationPropertyName.EMPTY, Bindable.of(dataSourceType)).get();
    }

    /**
     * 根据datasource对象和properties绑定
     *
     * @param dataSource
     * @param properties
     */
    private void bind(DataSource dataSource, Map properties) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(new ConfigurationPropertySource[]{source.withAliases(aliases)});
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(dataSource));
    }

    /**
     * 根据Class对象和指定路径的配置文件绑定（多了一步把配置文件解析为properties的过程）
     *
     * @param dataSourceType
     * @param sourcePath
     * @param <T>
     * @return
     */
    private <T extends DataSource> T bind(Class<T> dataSourceType, String sourcePath) {
        Map properties = binder.bind(sourcePath, Map.class).get();
        return bind(dataSourceType, properties);
    }

    /**
     * 通过全类名，用反射获取对象描述Class
     *
     * @param dataSourceTypeStr
     * @return
     */
    private Class<? extends DataSource> getDataSourceType(String dataSourceTypeStr) {

        Class<? extends DataSource> dataSourceTypeClass;
        try {
            if (StringUtils.isNotBlank(dataSourceTypeStr)) {
                dataSourceTypeClass = (Class<? extends DataSource>) Class.forName(dataSourceTypeStr);
            } else {
                dataSourceTypeClass = HikariDataSource.class;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //无法通过反射获取class对象的情况则抛出异常，该情况一般是写错了，所以此次抛出一个runtimeexception
            throw new IllegalArgumentException("can not resolve class with type: " + dataSourceTypeStr);
        }
        return dataSourceTypeClass;
    }
}
