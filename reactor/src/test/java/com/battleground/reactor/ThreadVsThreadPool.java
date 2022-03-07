package com.battleground.reactor;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadVsThreadPool {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("no corePoolSize");
            return;
        }

        for (int i = 0; i < 10; i++) {
            System.out.printf("run %s: \n", i + 1);
            for (String s : args) {
                int corePoolSize = Integer.parseInt(s);
                new Process(corePoolSize).run();
            }
            System.out.println("------");
        }
    }

    private static class Process {

        private final int corePoolSize;

        private final ExecutorService executor;

        public Process(int corePoolSize) {
            this.corePoolSize = corePoolSize;
            this.executor = Executors.newFixedThreadPool(corePoolSize);
        }

        public void run() {
            CountDownLatch latch = new CountDownLatch(COUNT);
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < COUNT; i++) {
                executor.submit(() -> {
                    sum(VALUES);
                    latch.countDown();
                });
            }

            try {
                latch.await();
                System.out.printf("%03d core pool size, use: %s\n", corePoolSize, System.currentTimeMillis() - startTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            executor.shutdownNow();
        }

    }

    public static int sum(int[] values) {
        return Arrays.stream(values)
            .map(x -> x + 1)
            .map(x -> x * 2)
            .map(x -> x + 2)
            .reduce(0, Integer::sum);
    }

    private static final int[] VALUES = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    private static final int COUNT = 100_000;

}
