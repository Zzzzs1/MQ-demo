package com.z2zz.loadbalance.api.impl;

import com.github.houbb.heaven.annotation.ThreadSafe;
import com.z2zz.loadbalance.api.ILoadBalanceContext;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 轮询策略
 */
@ThreadSafe
public class LoadBalanceRoundRobbin<T extends IServer> extends AbstractLoadBalance<T> {

    /**
     * 位移指针
     */
    private final AtomicLong indexHolder = new AtomicLong();

    @Override
    protected T doSelect(ILoadBalanceContext<T> context) {
        List<T> servers = context.servers();

        long index = indexHolder.getAndIncrement();
        int actual = (int) (index%servers.size());
        return servers.get(actual);
    }
}
