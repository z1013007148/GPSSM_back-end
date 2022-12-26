package com.hgx.server.executor;

import lombok.NoArgsConstructor;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author fish
 * @create 2021-11-24 19:46
 */
@NoArgsConstructor
public class EmailSenderExecutorPool {
    private volatile static ThreadPoolExecutor INSTANCE = new ThreadPoolExecutor(4, 9,
            60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(2000),
            new ThreadPoolExecutor.DiscardPolicy());

    public static ThreadPoolExecutor getINSTANCE() {
        return INSTANCE;
    }
}
