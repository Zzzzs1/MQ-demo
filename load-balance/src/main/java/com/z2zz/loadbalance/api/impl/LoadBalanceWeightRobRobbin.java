package com.z2zz.loadbalance.api.impl;


import com.github.houbb.heaven.annotation.ThreadSafe;
import com.github.houbb.heaven.support.filter.IFilter;
import com.github.houbb.heaven.support.handler.IHandler;
import com.github.houbb.heaven.util.lang.MathUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.z2zz.loadbalance.api.ILoadBalanceContext;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 加权轮询
 */
@ThreadSafe
public class LoadBalanceWeightRobRobbin<T extends IServer> extends AbstractLoadBalance<T> {

    //普通的加权轮询
//    /**
//     * 位移指针
//     */
//    private final AtomicLong indexHolder = new AtomicLong();
//
//    /**
//     * 初始化
//     *
//     * @param serverList 服务列表
//     * @return
//     */
//    private List<T> buildActualList(final List<T> serverList){
//        final List<T> actualList = new ArrayList<>();
//
//        //1.过滤权重为0的机器
//        List<T> notZeroServers = CollectionUtil.filterList(serverList, new IFilter<T>() {
//            @Override
//            public boolean filter(IServer server) {
//                return server.weight()<=0;
//            }
//        });
//        //2.获取权重列表
//        List<Integer> weightList = CollectionUtil.toList(notZeroServers, new IHandler<T, Integer>() {
//            @Override
//            public Integer handle(IServer server) {
//                return server.weight();
//            }
//        });
//
//        //3.获取最大权重
//        int maxDivisor = MathUtil.ngcd(weightList);
//
//        //4.重新计算构建基于权重的列表
//        for(T server : notZeroServers){
//            int weight = server.weight();
//
//            int time = weight / maxDivisor;
//
//            for(int i = 0;i < time;i++){
//                actualList.add(server);
//            }
//        }
//        return actualList;
//    }
//
//
//    @Override
//    protected T doSelect(ILoadBalanceContext<T> context) {
//        List<T> servers = context.servers();
//        List<T> actualList = buildActualList(servers);
//
//        long index = indexHolder.getAndIncrement();
//
//        //基于真实的列表构建
//        int actual = (int) (index % actualList.size());
//        return actualList.get(actual);
//    }

    //nginx平滑的基于权重的轮询算法

    /**
     * 算法所用节点
     */
    class node {
        int index;//该服务器在列表中的索引
        int cur;//当前权重
        int weight;//本身权重

        public node(int index, int cur, int weight) {
            this.index = index;
            this.cur = cur;
            this.weight = weight;
        }
    }
    List<node> nodeList = new ArrayList<>();

    /**
     * 构建节点列表
     * @param serverList
     * @return
     */
    private List<node> buildNodeList(final List<T> serverList){
        List<node> nodeList = new ArrayList<>();
        for(int i = 0; i < serverList.size();i++){
            nodeList.add(new node(i,0,serverList.get(i).weight()));
        }
        return nodeList;
    }


    /**
     * 根据算法获取下一个服务器节点下标
     *
     * @param serverList
     * @return
     */
    public int next(List<T> serverList){
        nodeList = nodeList.isEmpty()?buildNodeList(serverList):nodeList;
        /**
         * 总权重
         */
        int total = 0;

        /**
         * 标记选中的节点下标
         */
        int nodeIndex = 0;
        /**
         * 计算总权重以及进行第一步
         */
        for(int i = 0; i < nodeList.size(); i++){
            node n = nodeList.get(i);
            total += n.weight;
            n.cur += n.weight;

            if(n.cur > nodeList.get(nodeIndex).cur){
                nodeIndex = i;
            }
        }
        nodeList.get(nodeIndex).cur -= total;
        return nodeIndex;
    }

    @Override
    protected T doSelect(ILoadBalanceContext<T> context) {
        List<T> serverList = context.servers();
        nodeList = buildNodeList(serverList);

        return serverList.get(nodeList.get(next(serverList)).index);
    }
}
