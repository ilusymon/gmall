package com.atguigu.gmall.test.proxy;

public class Lj implements Rent{

    Student student;

    public Lj(Student student){
        this.student =student;
    }


    @Override
    public void rent() {


        student.rent();

        System.out.println("加钱收取中介分");
        System.out.println("中介赚钱了");

    }
}
