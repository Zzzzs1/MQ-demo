package com.z2zz.loadbalance.api.impl;

import com.github.houbb.hash.api.IHashCode;
import com.z2zz.loadbalance.api.ILoadBalanceContext;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.List;

public class LoadBalanceCommonHash<T extends IServer> extends AbstractLocalBalanceHash<T> {

    public LoadBalanceCommonHash(IHashCode hashCode){
        super(hashCode);
    }

    @Override
    protected T doSelect(ILoadBalanceContext<T> context) {
        List<T> servers = context.servers();

        final String hashKey = context.hashKey();
        int code = hashCode.hash(hashKey);
        int hashCode = Math.abs(code);
        int index = hashCode%servers.size();
        return servers.get(index);
    }
}
