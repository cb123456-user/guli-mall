package com.cb.gulimall.product.Thread;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TestThread {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /**
         * 4种常见线程池
         */
        /*Executors.newFixedThreadPool();
        Executors.newCachedThreadPool();
        Executors.newScheduledThreadPool();
        Executors.newSingleThreadExecutor();*/

        ExecutorService executor = Executors.newFixedThreadPool(10);

        System.out.println("main start");

//        Runnable runnable = () -> {
//            System.out.println("任务开始：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任务执行结束");
//        };
//
//        Supplier<Integer> supplier = () -> {
//            System.out.println("任务开始：" + Thread.currentThread().getId());
//            int i = 10 / 2;
////            int i = 10 / 0;
//            System.out.println("任务执行结束");
//            return i;
//        };

        /**
         * 创建异步对象
         */
//        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable);
//        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable, executor);
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(supplier);
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(supplier, executor);

        /**
         * whenComplete只能感知异常，不能处理
         * exceptionally可以处理异常，可以修改返回值
         * handle可以处理异常，可以修改返回值
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(supplier, executor)
//                .whenComplete((result, exception) -> {
//                    System.out.println("完成时回调：" + "result： "+ result + " exception: "+ exception);
//                })
//                .exceptionally(exception ->{
//                    return 10;
//                });
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(supplier, executor)
//                .handle((result, exception) -> {
//                    if (result != null) {
//                        return result;
//                    }
//                    if (exception != null) {
//                        return 10;
//                    }
//                    return 10;
//                });


        Supplier<String> task1 = () -> {
            System.out.println("任务1开始");
            String result = "ok1";
            System.out.println("任务1执行结束");
            return result;
        };

        Supplier<String> task2 = () -> {
            System.out.println("任务2开始");
            String result = "ok2";
            System.out.println("任务2执行结束");
            return result;
        };

        Supplier<String> task3 = () -> {
            System.out.println("任务3开始");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String result = "ok3";
            System.out.println("任务3执行结束");
            return result;
        };


        /**
         * thenRunAsync依赖任务完成后执行，不需要依赖任务结果，无返回值
         * thenAcceptAsync依赖任务完成后执行，需要依赖任务结果，无返回值
         * thenApplyAsync依赖任务完成后执行，需要依赖任务结果，有返回值
         */
//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(task1, executor)
//                .thenRunAsync(() -> System.out.println("哈哈哈"), executor);
//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(task1, executor)
//                .thenAcceptAsync(result -> System.out.println("上一步返回值：" + result), executor);
//        CompletableFuture<Object> future = CompletableFuture.supplyAsync(task1, executor)
//                .handle((r, e) -> {
//                    if (r != null) {
//                        return r;
//                    }
//                    if (e != null) {
//                        return "default";
//                    }
//                    return "default";
//                })
//                .thenApplyAsync(result -> {
//                    System.out.println("上一步返回值：" + result);
//                    return result + "," + "thenApplyAsync";
//                }, executor);


//        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(task1, executor);
//        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(task2, executor);

        /**
         * runAfterBothAsync 2个组合任务都要完成，不需要组合任务返回值,无返回值
         * thenAcceptBothAsync 2个组合任务都要完成，需要组合任务返回值,无返回值
         * thenCombineAsync 2个组合任务都要完成，需要组合任务返回值,有返回值
         */
//        CompletableFuture<Void> future = future1.runAfterBothAsync(future2, () -> System.out.println("哈哈哈"), executor);
//        CompletableFuture<Void> future = future1.thenAcceptBothAsync(future2, (f1, f2) -> {
//            System.out.println("f1: " + f1 + " f2: " + f2);
//        }, executor);
//        CompletableFuture<String> future = future1.thenCombineAsync(future2, (f1, f2) -> {
//            return "f1: " + f1 + " f2: " + f2 + " f3: ok";
//        }, executor);

//        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(task1, executor);
//        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(task3, executor);

        /**
         * runAfterEitherAsync 组合任务有一个完成就执行，不需要上一次任务的结果，无返回值
         * acceptEitherAsync 组合任务有一个完成就执行，需要上一次任务的结果，无返回值
         * applyToEitherAsync 组合任务有一个完成就执行，需要上一次任务的结果，有返回值
         */
//        CompletableFuture<Void> future = future1.runAfterEitherAsync(future3,
//                () -> System.out.println("哈哈哈"), executor);
//        CompletableFuture<Void> future = future1.acceptEitherAsync(future3,
//                f -> System.out.println(f), executor);
//        CompletableFuture<Object> future = future1.applyToEitherAsync(future3, f -> {
//            return f + "==> ok";
//        }, executor);

//        System.out.println("执行结果： " + future.get());


        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(task1, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(task2, executor);
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(task3, executor);

        /**
         * allOf 多任务组合
         */
//        CompletableFuture<Void> future = CompletableFuture.allOf(future1, future2, future3);
        CompletableFuture<Object> future = CompletableFuture.anyOf(future1, future2, future3);

        future.get();
        System.out.println("组合任务执行结束");
//        System.out.println(future1.get());
//        System.out.println(future2.get());
//        System.out.println(future3.get());

        System.out.println("main end");
    }
}
