package com.lkj.springbootinit.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 多线程顺序打印ABC100次
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.service
 * @Project：LKJAPI
 * @name：多线程顺序打印ABC100次
 * @Date：2024/1/16 15:51
 * @Filename：多线程顺序打印ABC100次
 */
public class 多线程顺序打印ABC100次 {

    /**
     * 多线程顺序打印ABC100次
     */

    //方法三 ： 通过ReentrantLock的Condition,condition可以指定唤醒对象，signal方法
    private static Lock lock = new ReentrantLock();
    private static Condition A = lock.newCondition();
    private static Condition B = lock.newCondition();
    private static Condition C = lock.newCondition();
    private static int count = 0;

    static class ThreadA extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                for (int i = 0; i < 100; i++) {
                    while (count % 3 != 0)//注意这里是不等于0，也就是说在count % 3为0之前，当前线程一直阻塞状态
                        A.await(); // A释放lock锁
                    System.out.print("A");
                    count++;
                    B.signal(); // A执行完唤醒B线程
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
    static class ThreadB extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                for (int i = 0; i < 100; i++) {
                    while (count % 3 != 1)
                        B.await();// B释放lock锁，当前面A线程执行后会通过B.signal()唤醒该线程
                    System.out.print("B");
                    count++;
                    C.signal();// B执行完唤醒C线程
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
    static class ThreadC extends Thread {
        @Override
        public void run() {
            try {
                lock.lock();
                for (int i = 0; i < 100; i++) {
                    while (count % 3 != 2)
                        C.await();// C释放lock锁，当前面B线程执行后会通过C.signal()唤醒该线程
                    System.out.print("C");
                    count++;
                    A.signal();// C执行完唤醒A线程
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        new ThreadA().start();
        new ThreadB().start();
        new ThreadC().start();
    }

    //方法二 ： 通过ReentrantLock的lock以及unlock
//    static Lock lock = new ReentrantLock();// 通过JDK5中的Lock锁来保证线程的访问的互斥
//    private static int state = 1;//通过state的值来确定是否打印
//    static class ThreadA extends Thread {
//        @Override
//        public void run() {
//            for (int i = 0; i < 100;) {
//                try {
//                    lock.lock();
//                    while (state % 3 == 1) {// 多线程并发，不能用if，必须用循环测试等待条件，避免虚假唤醒
//                        System.out.println("A");
//                        state++;
//                        i++;
//                    }
//                } finally {
//                    lock.unlock();// unlock()操作必须放在finally块中
//                }
//            }
//        }
//    }
//
//    static class ThreadB extends Thread {
//        @Override
//        public void run(){
//            for (int i = 0; i < 100;) {
//                try {
//                    lock.lock();
//                    while (state % 3 == 2){
//                        System.out.println("B");
//                        state++;
//                        i++;
//                    }
//                }finally {
//                    lock.unlock();
//                }
//            }
//        }
//    }
//
//    static class ThreadC extends Thread {
//        @Override
//        public void run(){
//            for (int i = 0; i < 100;) {
//                try {
//                    lock.lock();
//                    while (state % 3 == 0){
//                        System.out.println("C");
//                        state++;
//                        i++;
//                    }
//                }finally {
//                    lock.unlock();
//                }
//            }
//        }
//    }
//    public static void main(String[] args) {
//        new ThreadA().start();
//        new ThreadB().start();
//        new ThreadC().start();
//    }
    //方法一：synchronized + wait + notifyall
//    private static  String message = "A";
//    private static Object lock = new Object();
//    @Test
//    public static void main(String[] args) {
//        new Thread(()->{
//            int count = 1;
//            synchronized (lock){
//                while (count < 101){
//                    while (!message.equals("A")){
//                        try {
//                            lock.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    System.out.println(message);
//                    message = "B";
//                    count ++;
//                    // 前面示例通过多次调用notify() 方法实现 5个线程被唤醒，但并不能保证系统中仅有 5个线程，
//                    // 也就是notify()方法的调用次数小于线程对象的数量，那么会出现部分线程对象没有被唤醒的情况。为了唤醒全部线程可以使用notifyAll() 方法。
//                    //notifyAll()方法会按照执行 wait()方法的倒序依次对其他线程进行唤醒。
//                    lock.notifyAll();
//                }
//            }
//        },"A").start();
//
//        new Thread(()->{
//            int count = 1;
//            synchronized (lock){
//                while (count < 101){
//                    while (!message.equals("B")){
//                        try {
//                            lock.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    System.out.println(message);
//                    message = "C";
//                    count ++;
//                    // 前面示例通过多次调用notify() 方法实现 5个线程被唤醒，但并不能保证系统中仅有 5个线程，
//                    // 也就是notify()方法的调用次数小于线程对象的数量，那么会出现部分线程对象没有被唤醒的情况。为了唤醒全部线程可以使用notifyAll() 方法。
//                    //notifyAll()方法会按照执行 wait()方法的倒序依次对其他线程进行唤醒。
//                    lock.notifyAll();
//                }
//            }
//        },"B").start();
//
//        new Thread(()->{
//            int count = 1;
//            synchronized (lock){
//                while (count < 101){
//                    while (!message.equals("C")){
//                        try {
//                            lock.wait();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    System.out.println(message);
//                    message = "A";
//                    count ++;
//                    // 前面示例通过多次调用notify() 方法实现 5个线程被唤醒，但并不能保证系统中仅有 5个线程，
//                    // 也就是notify()方法的调用次数小于线程对象的数量，那么会出现部分线程对象没有被唤醒的情况。为了唤醒全部线程可以使用notifyAll() 方法。
//                    //notifyAll()方法会按照执行 wait()方法的倒序依次对其他线程进行唤醒。
//                    lock.notifyAll();
//                }
//            }
//        },"C").start();
//    }


}
