package com.yusufu.tradingadvisor;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class ConcurrencyTest {

    static int fetchNumber() {
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        return 42;
    }

    @Test
    public void testThread(){
        Thread thread = new Thread(() -> {
            System.out.println("Thread: " + fetchNumber());
        });
        thread.start();
    }

    @Test
    public void testRunnable(){
        Runnable task = () -> System.out.println("Runnable: " + fetchNumber());
        new Thread(task).start();
    }

    @Test
    public void testCallable() throws ExecutionException, InterruptedException {
        Callable<Integer> task = () -> fetchNumber();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(task);

        System.out.println("Callable result: " + future.get()); // blocks
        executor.shutdown();
    }

    @Test
    public void testRunnableExecutor(){
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.execute(() -> {
            System.out.println("Executor Runnable: " + fetchNumber());
        });

        executor.shutdown();
    }

}
