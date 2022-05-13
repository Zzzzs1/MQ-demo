package com.z2zz.loadbalance.support.server.impl;

import com.z2zz.loadbalance.support.server.IServer;

import java.util.Objects;

/**
 * 服务器信息
 */
public class Server  implements IServer {

    /**
     * 地址
     */
    private String url;

    /**
     * 权重
     */
    private int weight;

    public static Server newInstance(){
        return new Server();
    }

    /**
     * 实现
     * @param url 地址
     * @param weight 权重
     * @return this
     */
    public static Server of(final String url,
                            final int weight){
        return newInstance().url(url).weight(weight);
    }

    @Override
    public String url() {
        return url;
    }

    public Server url(String url) {
        this.url = url;
        return this;
    }


    @Override
    public int weight() {
        return weight;
    }

    public Server weight(int weight) {
        this.weight = weight;
        return this;
    }

    @Override
    public String toString() {
        return "Server{" +
                "url='" + url + '\'' +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Server server = (Server) object;
        return weight == server.weight &&
                Objects.equals(url, server.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, weight);
    }



}
