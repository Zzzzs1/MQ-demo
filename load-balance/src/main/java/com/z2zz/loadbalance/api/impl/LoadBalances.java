package com.z2zz.loadbalance.api.impl;

import com.github.houbb.hash.api.IHashCode;
import com.z2zz.loadbalance.api.ILoadBalance;
import com.z2zz.loadbalance.support.server.IServer;

public class LoadBalances {

    private LoadBalances(){}

    /**
     * 随机
     * @return
     */
    public static <T extends IServer> ILoadBalance<T> random(){
        return new LoadBalanceRandom<>();
    }

    /**
     * 轮询
     */
    public static <T extends IServer> ILoadBalance<T> roundRobbin(){
        return new LoadBalanceRoundRobbin<>();
    }

    /**
     * 权重轮训
     * @return 结果
     * @since 0.0.1
     */
    public static <T extends IServer> ILoadBalance<T> weightRoundRobbin() {
        return new LoadBalanceWeightRobRobbin<>();
    }

    /**
     * 普通 Hash
     * @param hashCode 哈希策略
     * @return 结果
     * @since 0.0.1
     */
    public static <T extends IServer> ILoadBalance<T> commonHash(final IHashCode hashCode) {
        return new LoadBalanceCommonHash<>(hashCode);
    }

    /**
     * 一致性 Hash
     * @param hashCode 哈希策略
     * @return 结果
     * @since 0.0.1
     */
    public static <T extends IServer> ILoadBalance<T> consistentHash(final IHashCode hashCode) {
        return new LoadBalanceConsistentHash<>(hashCode);
    }


}
