package com.lkj.springbootinit.Test;

import org.junit.jupiter.api.Test;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.Test
 * @Project：LKJAPI
 * @name：Test1
 * @Date：2024/3/25 15:18
 * @Filename：Test1
 */
public class Test1 {
    abstract static class Shap{
        abstract void draw();
    }

    static class  draw extends Shap{

        @Override
        void draw() {
            System.out.println("画一个圆圈");
        }
    }

    @Test
    public static void main(String[] args) {
        Shap shap = new draw();
        shap.draw();
    }
}
