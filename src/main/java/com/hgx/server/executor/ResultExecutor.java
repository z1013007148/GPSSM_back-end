package com.hgx.server.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author fish
 * @create 2021-12-30 20:16
 */
public class ResultExecutor {
    private volatile static ExecutorService INSTANCE =
            new ThreadPoolExecutor(2,4,60,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<>(100),
                    new ThreadPoolExecutor.DiscardPolicy());

    public static ExecutorService getINSTANCE(){
        return INSTANCE;
    }
}
