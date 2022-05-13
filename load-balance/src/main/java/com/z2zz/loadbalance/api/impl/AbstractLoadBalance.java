package com.z2zz.loadbalance.api.impl;

import com.z2zz.loadbalance.api.ILoadBalanceContext;
import com.z2zz.loadbalance.api.ILoadBalance;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.List;

public abstract class AbstractLoadBalance<T extends IServer> implements ILoadBalance<T> {

    @Override
    public T select(ILoadBalanceContext<T> context) {
        List<T> servers = context.servers();

        if(servers.size()<=1){
            return servers.get(0);
        }

        return doSelect(context);
    }

    /**
     * 执行选择
     * @param context 上下文
     * @return 结果
     */
    protected abstract T doSelect(final ILoadBalanceContext<T> context);
}
