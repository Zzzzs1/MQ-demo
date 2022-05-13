package com.z2zz.loadbalance.api;

import com.z2zz.loadbalance.support.server.IServer;

import java.util.List;

public interface ILoadBalanceContext<T extends IServer> {

    /**
     * 调用的hashKey
     * @return hashKey
     */
    String hashKey();

    /**
     * 服务端列表
     * @return 列表
     */
    List<T> servers();


}
