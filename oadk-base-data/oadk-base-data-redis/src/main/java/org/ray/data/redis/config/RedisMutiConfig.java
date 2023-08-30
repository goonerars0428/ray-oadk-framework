package org.ray.data.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.ray.data.redis.config.jedis.JedisConfigure;
import org.ray.data.redis.config.lettuce.LettuceConfigure;
import org.ray.data.redis.properties.RedisExtProperties;
import org.ray.data.redis.utils.RedisUtil;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Configuration
public class RedisMutiConfig implements EnvironmentAware {


    /**
     * 配置上下文
     */
    private Environment env;
    /**
     * 参数绑定工具
     */
    private Binder binder;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
        // 绑定配置器
        binder = Binder.get(env);
        //创建自定义redisTemplate
        customRedisTemplateRegist();
    }

    /**
     * 创建默认redis模板
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = createRedisTemplate(redisConnectionFactory);
        RedisUtil.REDIS_HOLDER.put("default", redisTemplate);
        return redisTemplate;
    }

    /**
     * 注册自定义redis模板，支持多redis数据源
     */
    public void customRedisTemplateRegist() {
        //获取默认配置
        RedisProperties defaultConfig = binder.bind("spring.redis", Bindable.of(RedisProperties.class)).get();
        //获取自定义多数据源配置
        List<RedisExtProperties> customConfigs = binder.bind("spring.redis.custom", Bindable.listOf(RedisExtProperties.class)).get();
        for (RedisExtProperties customConfig : customConfigs) {
            //执行数据源注册
            doRegist(defaultConfig, customConfig);
        }
    }

    private void doRegist(RedisProperties defaultConfig, RedisExtProperties customConfig) {
        if (customConfig == null) {
            return;
        }
        //必须属性校验
        String redisKey = customConfig.getRedisKey();
        RedisExtProperties.RedisType redisType = customConfig.getRedisType();
        if (StringUtils.isBlank(redisKey) || redisType == null) {
            throw new RuntimeException("缺少自定义数据源参数");
        }
        //连接池判断，lettuce优先于jedis
        RedisProperties.ClientType clientType = defaultConfig.getClientType();
        LettuceClientConfiguration lettuceClientConfiguration = null;
        JedisClientConfiguration jedisClientConfiguration = null;
        if (RedisProperties.ClientType.LETTUCE.equals(clientType)) {
            lettuceClientConfiguration = new LettuceConfigure(defaultConfig, customConfig).getLettuceClientConfiguration();
        } else {
            jedisClientConfiguration = new JedisConfigure(defaultConfig, customConfig).getJedisClientConfiguration();
        }
        RedisConnectionFactory redisConnectionFactory = null;
        //模式判断，cluster优先于standalone
        if (RedisExtProperties.RedisType.CLUSTER.equals(redisType)) {
            //获取集群模式的连接配置
            RedisClusterConfiguration redisClusterConfiguration = getClusterConfiguration(customConfig);
            if (RedisProperties.ClientType.LETTUCE.equals(clientType)) {
                LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisClusterConfiguration, lettuceClientConfiguration);
                lettuceConnectionFactory.afterPropertiesSet();
                redisConnectionFactory = lettuceConnectionFactory;
            } else {
                JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration, jedisClientConfiguration);
                jedisConnectionFactory.afterPropertiesSet();
                redisConnectionFactory = jedisConnectionFactory;
            }
        } else {
            //获取单机模式的连接配置
            RedisStandaloneConfiguration redisStandaloneConfiguration = getStandaloneConfig(customConfig);
            if (RedisProperties.ClientType.LETTUCE.equals(clientType)) {
                LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
                lettuceConnectionFactory.afterPropertiesSet();
                redisConnectionFactory = lettuceConnectionFactory;
            } else {
                JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
                jedisConnectionFactory.afterPropertiesSet();
                redisConnectionFactory = jedisConnectionFactory;
            }
        }
        if (redisConnectionFactory == null) {
            throw new RuntimeException("自定义redis数据源加载错误，redisConnectionFactory为空，请检查配置");
        }
        //创建customRedisTemplate
        RedisTemplate redisTemplate = createRedisTemplate(redisConnectionFactory);
        RedisUtil.REDIS_HOLDER.put(redisKey, redisTemplate);
    }


    /**
     * 获取单机模式连接配置，只能使用自定义配置
     *
     * @param customConfig
     * @return
     */
    private RedisStandaloneConfiguration getStandaloneConfig(RedisExtProperties customConfig) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (org.springframework.util.StringUtils.hasText(customConfig.getUrl())) {
            ConnectionInfo connectionInfo = parseUrl(customConfig.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            config.setUsername(connectionInfo.getUsername());
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        } else {
            config.setHostName(customConfig.getHost());
            config.setPort(customConfig.getPort());
            config.setUsername(customConfig.getUsername());
            config.setPassword(RedisPassword.of(customConfig.getPassword()));
        }
        config.setDatabase(customConfig.getDatabase());
        return config;
    }

    /**
     * 获取集群模式连接配置，只能使用自定义配置
     *
     * @param customConfig
     * @return
     */
    private RedisClusterConfiguration getClusterConfiguration(RedisExtProperties customConfig) {
        if (customConfig.getCluster() == null) {
            return null;
        }
        RedisProperties.Cluster clusterProperties = customConfig.getCluster();
        RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
        if (clusterProperties.getMaxRedirects() != null) {
            config.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        config.setUsername(customConfig.getUsername());
        if (customConfig.getPassword() != null) {
            config.setPassword(RedisPassword.of(customConfig.getPassword()));
        }
        return config;
    }


    /**
     * 判断连接池是否启用
     *
     * @param pool
     * @return
     */
    public static boolean isPoolEnabled(RedisProperties.Pool pool) {
        Boolean enabled = pool.getEnabled();
        return (enabled != null) ? enabled : true;
    }

    /**
     * 解析配置参数中的url参数
     *
     * @param url
     * @return
     */
    public static ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                throw new RuntimeException("redis连接属性url处理错误：" + url);
            }
            boolean useSsl = ("rediss".equals(scheme));
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String candidate = uri.getUserInfo();
                int index = candidate.indexOf(':');
                if (index >= 0) {
                    username = candidate.substring(0, index);
                    password = candidate.substring(index + 1);
                } else {
                    password = candidate;
                }
            }
            return new ConnectionInfo(uri, useSsl, username, password);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("redis连接属性url处理错误：" + url);
        }
    }

    /**
     * 创建redisTemplate通用模板
     *
     * @param redisConnectionFactory
     * @return
     */
    private RedisTemplate createRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        //使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 通用连接信息内部类
     */
    public static class ConnectionInfo {

        private final URI uri;

        private final boolean useSsl;

        private final String username;

        private final String password;

        ConnectionInfo(URI uri, boolean useSsl, String username, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.username = username;
            this.password = password;
        }

        public boolean isUseSsl() {
            return this.useSsl;
        }

        String getHostName() {
            return this.uri.getHost();
        }

        int getPort() {
            return this.uri.getPort();
        }

        String getUsername() {
            return this.username;
        }

        String getPassword() {
            return this.password;
        }

    }

}
