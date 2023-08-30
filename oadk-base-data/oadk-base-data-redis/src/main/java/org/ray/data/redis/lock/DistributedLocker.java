package org.ray.data.redis.lock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * <p>@className DistributedLocker</p>
 * <p>@description 分布式锁 加锁器</p>
 *
 * @author wangshengyun
 * @date 2020/7/1 14:28
 */
public interface DistributedLocker {

    RLock lock(String lockKey);

    RLock lock(String lockKey, long leaseTime, TimeUnit unit);

    boolean tryLock(String lockKey);

    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit);

    void unlock(String lockKey);

    void unlock(RLock lock);

}
