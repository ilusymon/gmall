package com.atguigu.gmall.cart.test;

import java.math.BigDecimal;

public class TestBigdecimal {


    public static void main(String[] args) {

        BigDecimal b1 = new BigDecimal(0.01d);
        BigDecimal b2 = new BigDecimal(0.01f);

        System.out.println(b1);
        System.out.println(b2);

        int i = b2.compareTo(b1);// -1 0 1

        System.out.println(i);


        // 初始化
        BigDecimal b3 = new BigDecimal("0.07");
        BigDecimal b4 = new BigDecimal("0.06");
        System.out.println(b3);
        System.out.println(b4);


        // 比较
        int i1 = b3.compareTo(b4);
        System.out.println(i1);

        // 运算
        BigDecimal add = b3.add(b4);

        System.out.println(add);

        BigDecimal subtract = b3.subtract(b4);

        System.out.println(subtract);


        BigDecimal multiply = b3.multiply(b4);


        System.out.println(multiply);


        BigDecimal divide = b3.divide(b4,9,BigDecimal.ROUND_HALF_DOWN);

        System.out.println(divide);

        // 约数
        BigDecimal add1 = b1.add(b2);
        BigDecimal bigDecimal = add1.setScale(5, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(add1);
        System.out.println(bigDecimal);


    }

}
