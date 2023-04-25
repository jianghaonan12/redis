package com.hmdp;

import org.junit.jupiter.api.Test;

public class test2 {

        static int counter = 0;
        //static修饰，则元素是属于类本身的，不属于对象  ，与类一起加载一次，只有一个
        static final Object room = new Object();
        public static void main(String[] args) throws InterruptedException {
            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 5000; i++) {
                    synchronized (room) {
                        counter++;
                    }
                    System.out.println("A:"+counter);
                }
            }, "t1");
            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 4999; i++) {
                    synchronized (room) {
                        counter--;
                    }
                    System.out.println("B:"+counter);
                }
            }, "t2");
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            System.out.println(counter);

        }
        @Test
        public void test(){
            test2 test2 = new test2();

            System.out.println(this);
        }

    }

