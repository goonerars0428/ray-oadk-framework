package org.ray.data.redis.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * <p>@className RedissonDistributedLocker</p>
 * <p>@description Redisson分布式锁 加锁器</p>
 *
 * @author wangshengyun
 * @date 2020/7/1 16:07
 */
@Component
public class RedissonDistributedLocker implements DistributedLocker {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * <p>@title lock</p>
     * <p>@description 加锁（拿不到锁阻塞）</p>
     *
     * @param lockKey 锁key
     * @return RLock 锁
     * @author wangshengyun
     * @date 2020/7/1 16:59
     */
    @Override
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    /**
     * <p>@title lock</p>
     * <p>@description 加锁（拿不到锁阻塞）</p>
     *
     * @param lockKey   锁key
     * @param leaseTime 租用时间，超时时间
     * @param unit      时间单位
     * @return org.redisson.api.RLock
     * @author wangshengyun
     * @date 2020/7/1 17:04
     */
    @Override
    public RLock lock(String lockKey, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, unit);
        return lock;
    }

    /**
     * <p>@title tryLock</p>
     * <p>@description 尝试加锁（非阻塞，拿到锁返回true，拿不到锁返回false）</p>
     *
     * @param lockKey 锁key
     * @return boolean
     * @author wangshengyun
     * @date 2020/7/1 17:12
     */
    @Override
    public boolean tryLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.tryLock();
    }

    /**
     * <p>@title tryLock</p>
     * <p>@description 尝试加锁（非阻塞，拿到锁返回true，拿不到锁返回false）</p>
     *
     * @param lockKey   锁key
     * @param waitTime  等待时间
     * @param leaseTime 租用时间，超时时间
     * @param unit      时间单位
     * @return boolean
     * @author wangshengyun
     * @date 2020/7/1 17:15
     */
    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * <p>@title unlock</p>
     * <p>@description 解锁</p>
     *
     * @param lockKey 锁key
     * @author wangshengyun
     * @date 2020/7/1 17:19
     */
    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        //解锁前先判断是否是本线程上的锁
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * <p>@title unlock</p>
     * <p>@description 解锁</p>
     *
     * @param lock 锁
     * @author wangshengyun
     * @date 2020/7/1 17:19
     */
    @Override
    public void unlock(RLock lock) {
        //解锁前先判断是否是本线程上的锁
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

}
