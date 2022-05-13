package com.z2zz.loadbalance.bs;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.z2zz.loadbalance.api.ILoadBalance;
import com.z2zz.loadbalance.api.ILoadBalanceContext;
import com.z2zz.loadbalance.api.impl.LoadBalanceContext;
import com.z2zz.loadbalance.api.impl.LoadBalances;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.ArrayList;
import java.util.List;

/**
 * 负载均衡引导类
 */
public class LoadBalanceBs<T extends IServer> {


    /**
     * 引导类私有化
     */
    private LoadBalanceBs(){}

    /**
     * 新建对象实例
     * @return
     */
    public static <T extends IServer> LoadBalanceBs<T> newInstance(){
        return new LoadBalanceBs<>();
    }

    /**
     * 负载策略
     */
    private ILoadBalance<T> loadBalance = LoadBalances.random();

    /**
     * hash key
     */
    private String hashKey = "";

    /**
     * 服务器列表
     */
    private List<T> servers = new ArrayList<>();

    /**
     * 设置负载策略
     */
    public LoadBalanceBs<T> loadBalance(ILoadBalance<T> loadBalance) {
        ArgUtil.notNull(loadBalance, "loadBalance");

        this.loadBalance = loadBalance;
        return this;
    }

    public LoadBalanceBs<T> hashKey(String hashKey) {
        this.hashKey = hashKey;
        return this;
    }

    public LoadBalanceBs<T> servers(List<T> servers) {
        ArgUtil.notEmpty(servers, "servers");
        this.servers = servers;
        return this;
    }

    /**
     * 选择对应的服务器信息
     */

    public T select(){
        ArgUtil.notEmpty(servers,"servers");
        ArgUtil.notNull(loadBalance,"loadBalance");


        ILoadBalanceContext<T> context = LoadBalanceContext
                .<T>newInstance()
                .hashKey(hashKey)
                .servers(servers);

        return loadBalance.select(context);
    }

}
