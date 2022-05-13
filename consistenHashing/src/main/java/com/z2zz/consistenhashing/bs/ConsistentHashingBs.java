package com.z2zz.consistenhashing.bs;

import com.github.houbb.hash.api.IHashCode;
import com.github.houbb.hash.core.code.HasheCodes;
import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.z2zz.consistenhashing.api.IConsistentHashing;
import com.z2zz.consistenhashing.api.impl.ConsistentHashing;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.DoubleAccumulator;

public final class ConsistentHashingBs<T> {

    public ConsistentHashingBs(){

    }

    /**
     * 新建对象实例
     * @param <T> 泛型
     * @return
     */
    public static <T> ConsistentHashingBs<T> newInstance(){
        return new ConsistentHashingBs<>();
    }

    /**
     * 虚拟节点数
     */
    private int virtualNum = 16;

    /**
     * hash 实现策略
     */
    private IHashCode hashCode = HasheCodes.jdk();

    /**
     * 设置节点
     */
    private Collection<T> nodes = new HashSet<>();

    /**
     *设置虚拟节点数量
     * @param virtualNum 虚拟节点
     * @return
     */
    public ConsistentHashingBs<T> virtualNum(int virtualNum){
        ArgUtil.gt("virtualNum",virtualNum,0);

        this.virtualNum = virtualNum;
        return this;
    }

    /**
     * 设置hash策略
     *
     * @param hashCode hashCode策略
     * @return
     */
    public ConsistentHashingBs<T> hashCode(IHashCode hashCode){
        ArgUtil.notNull(hashCode,"hashCode");

        this.hashCode = hashCode;
        return this;

    }


    /**
     * 设置初始化节点
     *
     * @param nodes 节点
     * @return 结果
     */
    public ConsistentHashingBs<T> nodes(Collection<T> nodes){
        ArgUtil.notEmpty(nodes,"nodes");

        this.nodes = nodes;
        return this;

    }

    /**
     * 构建结果
     * @return 实现
     */
    public IConsistentHashing<T> build(){
        IConsistentHashing<T> hashing = new ConsistentHashing<>(hashCode,virtualNum);

        if(CollectionUtil.isNotEmpty(nodes)){
            for(T node : nodes){
                hashing.add(node);
            }
        }
        return hashing;
    }

}
