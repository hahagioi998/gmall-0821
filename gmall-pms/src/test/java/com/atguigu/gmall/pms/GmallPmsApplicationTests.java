package com.atguigu.gmall.pms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
class GmallPmsApplicationTests {

    @Test
    void contextLoads() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("hello异步编排");
        });

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("hello异步编排");
            return "我是结果集";
        }).whenCompleteAsync((t,u) -> {
            System.out.println("上一个任务的返回结果集t：" + t);
            System.out.println("上一个任务的异常信息u：" + u);
            System.out.println("执行另一个任务");
        }).exceptionally(t ->{
            System.out.println("上一个任务的异常信息t" + t);
            System.out.println("异常后的处理任务");
            return "hello exceptionally";
        });

        CompletableFuture.supplyAsync(() -> {
            System.out.println("hello异步编排");
            return "我是结果集1";
        }).thenApplyAsync(t -> {
            System.out.println("上个任务的返回结果：" + t);
            return "我是串行结果集2";
        }).thenApplyAsync(t -> {
            System.out.println("上个任务的返回结果：" + t);
            return "我是串行结果集3";
        }).thenAcceptAsync(t ->{
            //thenAcceptAsync()没有返回结果集
            System.out.println("上个任务的返回结果："  + t);
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("hello异步编排");
            return "我是结果集1";
        });

        CompletableFuture<String> future3 = future2.thenApplyAsync(t -> {
            System.out.println("上个任务的返回结果：" + t);
            return "我是并行结果集2";
        });

        CompletableFuture<String> future4 = future2.thenApplyAsync(t -> {
            System.out.println("上个任务的返回结果：" + t);
            return "我是并行结果集3";
        });

        CompletableFuture<Void> future5 = future2.thenAcceptAsync(t -> {
            System.out.println("上个任务的返回结果：" + t);
        });

        CompletableFuture.allOf(future2, future3, future4, future5).join();

    }

}
