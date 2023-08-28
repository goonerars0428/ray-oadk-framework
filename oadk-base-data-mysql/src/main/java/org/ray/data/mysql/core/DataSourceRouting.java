package org.ray.data.mysql.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DataSourceRouting extends AbstractRoutingDataSource {

    /**
     * 线程级别的私有变量，用于保存当前线程使用的数据源的key
     */
    private static final ThreadLocal<String> DATASOURCE_CONTEXT_HOLDER = new ThreadLocal<>();
    /**
     * 存储所有已注册的数据源
     */
    private static Map<String, DataSource> DATASOURCE_MAP = new HashMap<>();

    /**
     * 把当前事物下的连接塞入,用于事物处理
     */
    private static ThreadLocal<Map<String, ConnectWarp>> DATASOURCE_CONNECTION_HOLDER = new ThreadLocal<>();


    @Override
    protected Object determineCurrentLookupKey() {
        return getDataSourceRouterKey();
    }


    public DataSource getDataSource(String key) {
        return DATASOURCE_MAP.get(key);
    }

    public static void setDataSource(String name, DataSource dataSource) {
        DATASOURCE_MAP.put(name, dataSource);
    }

    public void buildDataSouce() {
        setTargetDataSources((Map) DATASOURCE_MAP);
    }

    /**
     * 这里只是添加到 dataSourceMap,要真正可以使用,添加后调用buildDataSouce
     */
    public void addDataSouce(String name, DataSource dataSource) {
        DATASOURCE_MAP.put(name, dataSource);
    }

    /**
     * 判定指定的dataSource是否注册了
     *
     * @return
     */
    public static boolean containsDataSource(String key) {
        return DATASOURCE_MAP.containsKey(key);
    }

    /**
     * 获取当前线程使用的数据源的key
     *
     * @return
     */
    public static String getDataSourceRouterKey() {
        return DATASOURCE_CONTEXT_HOLDER.get();
    }

    /**
     * 设置当前线程使用的数据源的key
     *
     * @param dataSourceRouterKey
     */
    public static void setDataSourceRouterKey(String dataSourceRouterKey) {
        if (containsDataSource(dataSourceRouterKey)) {
            DATASOURCE_CONTEXT_HOLDER.set(dataSourceRouterKey);
        } else {
            //如果没有使用默认数据源
            DATASOURCE_CONTEXT_HOLDER.set("default");
        }
    }

    /**
     * 移除当前线程使用的数据源的key
     */
    public static void removeDataSourceRouterKey() {
        DATASOURCE_CONTEXT_HOLDER.remove();
    }

    /**
     * 开启事物的时候,把连接放入 线程中,后续crud 都会拿对应的连接操作
     *
     * @param key
     * @param connection
     */
    public void bindConnection(String key, Connection connection) {
        Map<String, ConnectWarp> connectionMap = DATASOURCE_CONNECTION_HOLDER.get();
        if (connectionMap == null) {
            connectionMap = new HashMap<>();
            DATASOURCE_CONNECTION_HOLDER.set(connectionMap);
        }
        //包装一下 不然给 spring把我关闭了
        ConnectWarp connectWarp = new ConnectWarp(connection);
        connectionMap.put(key, connectWarp);
    }


    /**
     * 提交事物
     *
     * @throws SQLException
     */
    public void doCommit() throws SQLException {
        Map<String, ConnectWarp> stringConnectionMap = DATASOURCE_CONNECTION_HOLDER.get();
        if (stringConnectionMap == null) {
            return;
        }
        for (String dataSourceName : stringConnectionMap.keySet()) {
            ConnectWarp connection = stringConnectionMap.get(dataSourceName);
            connection.commit(true);
            connection.close(true);
        }
        removeConnectionThreadLocal();
    }

    /**
     * 事务回滚
     *
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        Map<String, ConnectWarp> stringConnectionMap = DATASOURCE_CONNECTION_HOLDER.get();
        if (stringConnectionMap == null) {
            return;
        }
        for (String dataSourceName : stringConnectionMap.keySet()) {
            ConnectWarp connection = stringConnectionMap.get(dataSourceName);
            connection.rollback();
            connection.close(true);
        }
        removeConnectionThreadLocal();
    }

    public void removeConnectionThreadLocal() {
        DATASOURCE_CONNECTION_HOLDER.remove();
    }


    /**
     * 如果 在connectionThreadLocal 中有 说明开启了事物,就从这里面拿
     *
     * @return
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        Map<String, ConnectWarp> stringConnectionMap = DATASOURCE_CONNECTION_HOLDER.get();
        if (stringConnectionMap == null) {
            //没开事物直接获取
            return determineTargetDataSource().getConnection();
        } else {
            //开了事物,从当前线程中拿,而且拿到的是 包装过的connect 只有我能关闭O__O "…
            String currentName = (String) determineCurrentLookupKey();
            //增加判断，如果currentName为空，是没有注解的类或方法，使用默认数据源
            currentName = StringUtils.isBlank(currentName) ? "default" : currentName;
            return stringConnectionMap.get(currentName);
        }

    }

}
