package com.z2zz.loadbalance.support.server;

public interface IServer {

    /**
     * 地址信息
     * @return 地址
     */
    String url();

    /**
     * 权重
     * @return 权重
     */
    int weight();


}
