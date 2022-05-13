package com.z2zz.loadbalance.api.impl;

import com.z2zz.loadbalance.api.ILoadBalanceContext;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机策略
 */
public class LoadBalanceRandom<T extends IServer> extends AbstractLoadBalance<T> {

    @Override
    protected T doSelect(ILoadBalanceContext<T> context) {
        List<T> servers = context.servers();

        Random random = ThreadLocalRandom.current();
        int nextIndex = random.nextInt(servers.size());
        return servers.get(nextIndex);
    }
}
