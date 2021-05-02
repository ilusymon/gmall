package com.atguigu.gmall.test.proxy;

import java.lang.reflect.Proxy;

public class MinTest {


    public static void main(String[] args) {
        Student student = new Student();

        Lj lj = new Lj(student);

        lj.rent();

        // Proxy.newProxyInstance()

    }
    
}
