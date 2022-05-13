package com.z2zz.loadbalance.util;


import com.github.houbb.hash.api.IHashCode;
import com.z2zz.loadbalance.api.impl.LoadBalances;
import com.z2zz.loadbalance.bs.LoadBalanceBs;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.List;

/**
 * 负载均衡工具类
 */
public class LoadBalanceHelper {

    private LoadBalanceHelper() {
    }

    /**
     * 随机选择
     *
     * @param servers 列表
     * @return 结果
     * @since 0.0.2
     */
    public static <T extends IServer> T random(final List<T> servers) {
        return LoadBalanceBs.<T>newInstance()
                .servers(servers)
                .loadBalance(LoadBalances.<T>random())
                .select();
    }

    /**
     * 轮训
     *
     * @param servers 列表
     * @return 结果
     * @since 0.0.2
     */
    public static <T extends IServer> T roundRobbin(final List<T> servers) {
        return LoadBalanceBs.<T>newInstance()
                .servers(servers)
                .loadBalance(LoadBalances.<T>roundRobbin())
                .select();
    }

    /**
     * 轮训权重
     *
     * @param servers 列表
     * @return 结果
     * @since 0.0.2
     */
    public static <T extends IServer> T weightRoundRobbin(final List<T> servers) {
        return LoadBalanceBs.<T>newInstance()
                .servers(servers)
                .loadBalance(LoadBalances.<T>weightRoundRobbin())
                .select();
    }

    /**
     * 通用 hash 策略
     *
     * @param servers 列表
     * @param hash    hash 策略
     * @param hashKey hash
     * @return 结果
     * @since 0.0.2
     */
    public static <T extends IServer> T commonHash(final List<T> servers,
                                                   final IHashCode hash,
                                                   final String hashKey) {
        return LoadBalanceBs.<T>newInstance()
                .servers(servers)
                .hashKey(hashKey)
                .loadBalance(LoadBalances.<T>commonHash(hash))
                .select();
    }

    /**
     * 一致性 hash 策略
     *
     * @param servers 列表
     * @param hash    hash 策略
     * @param hashKey hash
     * @return 结果
     * @since 0.0.2
     */
    public static <T extends IServer> T consistentHash(final List<T> servers,
                                                       final IHashCode hash,
                                                       final String hashKey) {
        return LoadBalanceBs.<T>newInstance()
                .servers(servers)
                .hashKey(hashKey)
                .loadBalance(LoadBalances.<T>consistentHash(hash))
                .select();
    }

}
