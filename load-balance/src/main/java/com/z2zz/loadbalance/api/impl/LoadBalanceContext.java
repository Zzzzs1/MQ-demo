package com.z2zz.loadbalance.api.impl;

import com.z2zz.loadbalance.api.ILoadBalanceContext;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.List;

public class LoadBalanceContext <T extends IServer> implements ILoadBalanceContext<T> {


    private String hashKey;

    /**
     * 服务端列表
     * @return
     */
    private List<T> servers;


    /**
     * 新建对象实例
     * @return
     */
    public static <T extends IServer> LoadBalanceContext<T> newInstance(){
        return new LoadBalanceContext<>();
    }


    @Override
    public String hashKey() {
        return hashKey;
    }

    public LoadBalanceContext<T> hashKey(String hashKey) {
        this.hashKey = hashKey;
        return this;
    }

    @Override
    public List<T> servers() {
        return servers;
    }

    public LoadBalanceContext<T> servers(List<T> servers) {
        this.servers = servers;
        return this;
    }

    @Override
    public String toString() {
        return "LoadBalanceContext{" +
                "hashKey='" + hashKey + '\'' +
                ", servers=" + servers +
                '}';
    }
}
