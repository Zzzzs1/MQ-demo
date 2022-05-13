package com.z2zz.loadbalance.api;

import com.z2zz.loadbalance.support.server.IServer;

public interface ILoadBalance<T extends IServer> {

    /**
     * 选择下一个节点
     *
     * 返回下标
     * @param context 上下文
     * @return
     */

    T select(final ILoadBalanceContext<T> context);


}
