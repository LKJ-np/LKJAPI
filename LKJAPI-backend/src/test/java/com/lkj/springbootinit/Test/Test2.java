package com.lkj.springbootinit.Test;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 用线程池执行10个线程,然后打印1-10
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.Test
 * @Project：LKJAPI
 * @name：Test1
 * @Date：2024/3/25 15:18
 * @Filename：Test1
 */
public class Test2 {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10,20,1000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));
        for (int i = 1; i <= 10; i++) {
            final int taskNumber = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("Task " + taskNumber + ": Thread:" + Thread.currentThread().getName());
            }, executor);
        }
        executor.shutdown();
    }
}
