package org.ray.data.redis.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RedisUtil {

    public static final Map<String, RedisTemplate> REDIS_HOLDER = new ConcurrentHashMap<>();

    public static RedisTemplate createRedisTemplate(String key) {
        if (REDIS_HOLDER.containsKey(key)) {
            return REDIS_HOLDER.get(key);
        }
        //如果没有指定key获取指定错了key，返回默认redisTemplate
        return REDIS_HOLDER.get("default");
    }

    // =============================common============================

    /**
     * 加载lua脚本
     *
     * @param db
     * @param defaultRedisScript
     */
    public static Object luaLoadScript(String db, DefaultRedisScript defaultRedisScript, String key) {
        return createRedisTemplate(db).execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Object nativeConnection = redisConnection.getNativeConnection();
                if (nativeConnection instanceof JedisCluster) {
                    return ((JedisCluster) nativeConnection).scriptLoad(defaultRedisScript.getScriptAsString(), key);
                } else if (nativeConnection instanceof Jedis) {
                    return ((Jedis) nativeConnection).scriptLoad(defaultRedisScript.getScriptAsString());
                }
                return "";
            }
        });
    }

    /**
     * 执行lua脚本
     *
     * @param keys
     * @param args
     * @return
     */
    public static Object luaExecute(String db, String defaultRedisScript, List<String> keys, List<String> args) {
        return createRedisTemplate(db).execute(new RedisCallback() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Object nativeConnection = redisConnection.getNativeConnection();
                if (nativeConnection instanceof JedisCluster) {
                    return ((JedisCluster) nativeConnection).eval(defaultRedisScript, keys, args).toString();
                } else if (nativeConnection instanceof Jedis) {
                    return ((Jedis) nativeConnection).eval(defaultRedisScript, keys, args).toString();
                }
                return null;
            }
        });
    }

    public static Object luaExecuteSha(String db, DefaultRedisScript defaultRedisScript, String defaultRedisScriptSha, List<String> keys, List<String> args) {
        return createRedisTemplate(db).execute(new RedisCallback() {
            @Override
            public String doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Object nativeConnection = redisConnection.getNativeConnection();
                String result = null;
                if (nativeConnection instanceof JedisCluster) {
                    JedisCluster jedisCluster = (JedisCluster) nativeConnection;
/*                    if (StringUtils.isEmpty(defaultRedisScriptSha) || !jedisCluster.scriptExists(defaultRedisScriptSha, keys.get(0))) {
                        sha = String.valueOf(luaLoadScript("", defaultRedisScript, keys.get(0)));
                    }*/

                    try {
                        result = jedisCluster.evalsha(defaultRedisScriptSha, keys, args).toString();
                    } catch (Exception e) {
                        luaLoadScript("", defaultRedisScript, keys.get(0));
                        result = jedisCluster.evalsha(defaultRedisScriptSha, keys, args).toString();
                    }
                } else if (nativeConnection instanceof Jedis) {
                    Jedis jedis = (Jedis) nativeConnection;
/*                    if (StringUtils.isEmpty(defaultRedisScriptSha) || !jedis.scriptExists(defaultRedisScriptSha)) {
                        sha = String.valueOf(luaLoadScript("", defaultRedisScript, keys.get(0)));
                    }*/
                    try {
                        result = jedis.evalsha(defaultRedisScriptSha, keys, args).toString();
                    } catch (Exception e) {
                        luaLoadScript("", defaultRedisScript, keys.get(0));
                        result = jedis.evalsha(defaultRedisScriptSha, keys, args).toString();
                    }
                }
                return result;
            }
        });
    }

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */


    public static boolean expire(String db, String key, long time) {

        try {
            if (time > 0) {
                createRedisTemplate(db).expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键
     *            不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */


    public static long getExpire(String db, String key) {

        return createRedisTemplate(db).getExpire(key, TimeUnit.SECONDS);

    }

    /**
     * 判断key是否存在
     *
     * @param db  redis库
     * @param key 键
     * @return true
     * 存在 false不存在
     */


    public static boolean hasKey(String db, String key) {

        try {
            return createRedisTemplate(db).hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值
     *            或多个
     */


    @SuppressWarnings("unchecked")

    public static void del(String db, String... key) {

        if (key != null && key.length > 0) {
            if (key.length == 1) {
                createRedisTemplate(db).delete(key[0]);
            } else {
                createRedisTemplate(db).delete(CollectionUtils.arrayToList(key));
            }
        }

    }

    // ============================String=============================

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */


    public static Object get(String db, String key) {

        return key == null ? null : createRedisTemplate(db).opsForValue().get(key);

    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */


    public static boolean set(String db, String key, Object value) {

        try {
            createRedisTemplate(db).opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */


    public static boolean set(String db, String key, Object value, long time) {

        try {
            if (time > 0) {
                createRedisTemplate(db).opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(db, key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */


    public static long incr(String db, String key, long delta) {

        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return createRedisTemplate(db).opsForValue().increment(key, delta);

    }

    public static long incr(String db, String key, long delta, long time) {

        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        Long increment = createRedisTemplate(db).opsForValue().increment(key, delta);
        expire("", key, time);
        return increment;

    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */


    public static long decr(String db, String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return createRedisTemplate(db).opsForValue().increment(key, -delta);

    }

    // ================================Map=================================

    /**
     * HashGet
     *
     * @param key  键
     *             不能为null
     * @param item 项
     *             不能为null
     * @return 值
     */


    public static Object hget(String db, String key, String item) {

        return createRedisTemplate(db).opsForHash().get(key, item);

    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */


    public static Map<Object, Object> hmget(String db, String key) {

        return createRedisTemplate(db).opsForHash().entries(key);

    }

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */


    public static boolean hmset(String db, String key, Map<String, Object> map) {

        try {
            createRedisTemplate(db).opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */


    public static boolean hmset(String db, String key, Map<String, Object> map, long time) {

        try {
            createRedisTemplate(db).opsForHash().putAll(key, map);
            if (time > 0) {
                expire(db, key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true
     * 成功 false失败
     */


    public static boolean hset(String db, String key, String item, Object value) {

        try {
            createRedisTemplate(db).opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true
     * 成功 false失败
     */


    public static boolean hset(String db, String key, String item, Object value, long time) {

        try {
            createRedisTemplate(db).opsForHash().put(key, item, value);
            if (time > 0) {
                expire(db, key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 删除hash表中的值
     *
     * @param key  键
     *             不能为null
     * @param item 项
     *             可以使多个 不能为null
     */


    public static void hdel(String db, String key, Object... item) {

        createRedisTemplate(db).opsForHash().delete(key, item);

    }


    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键
     *             不能为null
     * @param item 项
     *             不能为null
     * @return true
     * 存在 false不存在
     */


    public static boolean hHasKey(String db, String key, String item) {

        return createRedisTemplate(db).opsForHash().hasKey(key, item);

    }


    /**
     * hash递增 如果不存在, 就会创建一个
     * 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */


    public static double hincr(String db, String key, String item, double by) {

        return createRedisTemplate(db).opsForHash().increment(key, item, by);

    }


    /**
     * hash递减
     *
     * @param key  键
     * @param item 项
     * @param by   要减少记(小于0)
     * @return
     */


    public static double hdecr(String db, String key, String item, double by) {

        return createRedisTemplate(db).opsForHash().increment(key, item, -by);

    }


    /**
     * 尝试向redis中放入指定key的hash数据，如果成功返回true，失败返回false
     *
     * @param db
     * @param key
     * @param value
     * @return
     */
    public static boolean hifAbsent(String db, String key, String item, Object value, long time) {
        Boolean aBoolean = createRedisTemplate(db).opsForHash().putIfAbsent(key, item, value);
        if (aBoolean && time > 0) {
            expire(db, key, time);
        }
        return aBoolean;
    }

    /**
     * 尝试向redis中放入指定key的数据，如果成功返回true，失败返回false
     *
     * @param db
     * @param key
     * @param value
     * @param time
     * @return
     */
    public static boolean ifAbsent(String db, String key, Object value, long time) {
        Boolean aBoolean = createRedisTemplate(db).opsForValue().setIfAbsent(key, value);
        if (aBoolean && time > 0) {
            expire(db, key, time);
        }
        return aBoolean;
    }

    // ============================set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */


    public static Set<Object> sGet(String db, String key) {

        try {
            return createRedisTemplate(db).opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true
     * 存在 false不存在
     */


    public static boolean sHasKey(String db, String key, Object value) {

        try {
            return createRedisTemplate(db).opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值
     *               可以是多个
     * @return 成功个数
     */


    public static long sSet(String db, String key, Object... values) {

        try {
            return createRedisTemplate(db).opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }


    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值
     *               可以是多个
     * @return 成功个数
     */


    public static long sSetAndTime(String db, String key, long time, Object... values) {

        try {
            Long count = createRedisTemplate(db).opsForSet().add(key, values);
            if (time > 0) {
                expire(db, key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }


    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */


    public static long sGetSetSize(String db, String key) {

        try {
            return createRedisTemplate(db).opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }


    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值
     *               可以是多个
     * @return 移除的个数
     */


    public static long setRemove(String db, String key, Object... values) {

        try {
            Long count = createRedisTemplate(db).opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    // ===============================list=================================


    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0到 -1代表所有值
     * @return
     */


    public static List<Object> lGet(String db, String key, long start, long end) {

        try {
            return createRedisTemplate(db).opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */


    public static long lGetListSize(String db, String key) {

        try {
            return createRedisTemplate(db).opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }


    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引
     *              index>=0时， 0表头，1第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */


    public static Object lGetIndex(String db, String key, long index) {

        try {
            return createRedisTemplate(db).opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */


    public static boolean lSet(String db, String key, Object value) {

        try {
            createRedisTemplate(db).opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */


    public static boolean lSet(String db, String key, Object value, long time) {

        try {
            createRedisTemplate(db).opsForList().rightPush(key, value);
            if (time > 0) {
                expire(db, key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */


    public static boolean lSet(String db, String key, List<Object> value) {

        try {
            createRedisTemplate(db).opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */


    public static boolean lSet(String db, String key, List<Object> value, long time) {

        try {
            createRedisTemplate(db).opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(db, key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */


    public static boolean lUpdateIndex(String db, String key, long index, Object value) {

        try {
            createRedisTemplate(db).opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */


    public static long lRemove(String db, String key, long count, Object value) {

        try {
            Long remove = createRedisTemplate(db).opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * 向指定topic发数据
     */
    public static void push(String db, String topic, Object msg) {
        createRedisTemplate(db).convertAndSend(topic, msg);
    }
}
