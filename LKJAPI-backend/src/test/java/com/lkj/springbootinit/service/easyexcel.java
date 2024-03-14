package com.lkj.springbootinit.service;


import com.alibaba.excel.EasyExcel;
import java.util.List;


/**
 * @Description: 使用easy excel读取excel
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.service
 * @Project：LKJAPI
 * @name：EasyExcel
 * @Date：2024/3/14 14:09
 * @Filename：EasyExcel
 */
public class easyexcel {


    public static void main(String[] args) {
        String fileName = "C:\\Users\\PC\\Desktop\\LKJAPI\\LKJAPI-backend\\src\\test\\java\\com\\lkj\\springbootinit\\service\\data\\text.xlsx";
        System.out.println("监听器读：");
        readByListen(fileName);

        System.out.println("同步读：");
        readBySynchronous(fileName);
    }

    public static void readBySynchronous(String fileName) {
        List<TestData> list = EasyExcel.read(fileName).head(TestData.class).sheet().doReadSync();
        for (TestData a : list) {
            System.out.println(a);
        }
    }

    public static void readByListen(String fileName) {
        EasyExcel.read(fileName, TestData.class,new DataListener()).sheet().doRead();
    }
}
