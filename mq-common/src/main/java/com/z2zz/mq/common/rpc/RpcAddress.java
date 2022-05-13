package com.z2zz.mq.common.rpc;


import com.z2zz.loadbalance.support.server.IServer;

public class RpcAddress implements IServer {


    /**
     * address 信息
     */
    private String address;

    /**
     * 端口号
     */
    private int port;


    /**
     * 权重
     */
    private int weight;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String url() {
        return address+":"+port;
    }

    @Override
    public int weight() {
        return weight;
    }
}
