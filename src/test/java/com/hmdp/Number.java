package com.hmdp;

public class Number {
        public synchronized void a() throws InterruptedException {
            Thread.sleep(10000);
            System.out.println("1");
        }
        public synchronized void b() {
            System.out.println("2");
        }
    public static void main(String[] args) {
        Number n1 = new Number();
        Number n2 = new Number();
        new Thread(()->{
            try {
                n1.a();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(()->{ n2.b(); }).start();
    }
}
