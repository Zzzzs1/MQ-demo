package com.z2zz.loadbalance.api.impl;

import com.github.houbb.hash.api.IHashCode;
import com.z2zz.consistenhashing.api.IConsistentHashing;
import com.z2zz.consistenhashing.bs.ConsistentHashingBs;
import com.z2zz.loadbalance.api.ILoadBalanceContext;
import com.z2zz.loadbalance.support.server.IServer;

public class LoadBalanceConsistentHash <T extends IServer> extends AbstractLocalBalanceHash<T> {

    public LoadBalanceConsistentHash(IHashCode hashCode) {
        super(hashCode);
    }

    @Override
    protected T doSelect(ILoadBalanceContext<T> context) {
        IConsistentHashing<T> consistentHashing = ConsistentHashingBs
                .<T> newInstance()
                .hashCode(hashCode)
                .nodes(context.servers())
                .build();

        final String hashKey = context.hashKey();
        return consistentHashing.get(hashKey);
    }
}
