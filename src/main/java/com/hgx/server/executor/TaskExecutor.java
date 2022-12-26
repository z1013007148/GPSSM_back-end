package com.hgx.server.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author fish
 * @create 2021-11-24 18:52
 */
public class TaskExecutor {
    private volatile static ExecutorService INSTANCE =
            new ThreadPoolExecutor(100,100,60,
                    TimeUnit.SECONDS,new LinkedBlockingDeque<>(2000),
                    new ThreadPoolExecutor.CallerRunsPolicy());

    public static ExecutorService getINSTANCE(){
        return INSTANCE;
    }
}
