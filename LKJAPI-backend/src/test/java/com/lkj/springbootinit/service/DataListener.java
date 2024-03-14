package com.lkj.springbootinit.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;

// 有个很重要的点 DemoDataListener 不能被spring管理，要每次读取excel都要new,然后里面用到spring可以构造方法传进去
@Slf4j
public class DataListener implements ReadListener<TestData> {

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param testData    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param analysisContext
     */
    @Override
    public void invoke(TestData testData, AnalysisContext analysisContext) {
        System.out.println(testData);
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("已解析完成");
    }

}