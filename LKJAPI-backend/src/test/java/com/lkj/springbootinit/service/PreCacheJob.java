package com.lkj.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.esotericsoftware.minlog.Log;
import com.lkj.apicommon.entity.User;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.redisson.api.RedissonClient;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.service
 * @Project：LKJAPI
 * @name：PreCacheJob
 * @Date：2024/3/14 15:21
 * @Filename：PreCacheJob
 */
@Component
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList = Arrays.asList(1l);

    //不加分布式锁的写法
    //每天执行，预热推荐用户
    @Scheduled(cron = "0 36 15 * * *")
    public void docheUser(){
        //查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
        String rediskey = String.format("friend:user:recommend:%s", mainUserList);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //写缓存，30s过期
        try {
            valueOperations.set(rediskey,userPage,3000, TimeUnit.MINUTES);
        }catch (Exception e){
            Log.error("error");
        }
    }

    //添加分布式锁的写法，看门狗机制，自动续期
    @Scheduled(cron = "0 36 15 * * *")
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("friend:user:recommend:%s");
        try {
            //只有一个线程可以获取到锁
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("线程号：" + Thread.currentThread().getId());
                for (Long userid : mainUserList){
                    //查数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String rediskey = String.format("friend:user:recommend:%s", mainUserList);
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    //写缓存，30s过期
                    try {
                        valueOperations.set(rediskey,userPage,3000, TimeUnit.MINUTES);
                    }catch (Exception e){
                        Log.error("error");
                    }
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock:"+Thread.currentThread().getId());
                lock.unlock();
            }
        }

    }
}
