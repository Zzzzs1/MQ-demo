package com.z2zz.consistenhashing.api.impl;

import com.github.houbb.hash.api.IHashCode;
import com.z2zz.consistenhashing.api.IConsistentHashing;

import javax.swing.tree.TreeNode;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashing<T> implements IConsistentHashing<T> {

    /**
     * hash策略
     */
    private final IHashCode hashCode;

    /**
     * nodemap 节点信息
     * key:节点hash
     * Node:节点
     */
    private final TreeMap<Integer,T> nodeMap = new TreeMap<>();

    public ConsistentHashing(IHashCode hashCode, int virtualNum) {
        this.hashCode = hashCode;
        this.virtualNum = virtualNum;
    }

    /**
     * 虚拟节点数量
     */
    private final int virtualNum;

    /**
     * 沿环的顺时针找到虚拟结点
     *
     * @param key key
     * @return
     */
    @Override
    public T get(String key) {
        final int hashCode = this.hashCode.hash(key);
        Integer target = hashCode;

        if(!nodeMap.containsKey(hashCode)){
            target = nodeMap.ceilingKey(hashCode);
            if(target == null && !nodeMap.isEmpty()){
                target = nodeMap.firstKey();
            }
        }
        return nodeMap.get(target);
    }

    @Override
    public IConsistentHashing add(T node) {
        //初始化虚拟节点
        for(int i = 0; i < virtualNum;i++){
            int nodeKey = this.hashCode.hash(node.toString()+"-"+i);
            nodeMap.put(nodeKey,node);
        }
        return this;
    }

    @Override
    public IConsistentHashing remove(T node) {
        //移除虚拟节点
        //这里有一个问题，如果hash冲突，直接移除会不会不够严谨
        for(int i = 0;i < virtualNum;i++){
            int nodeKey = this.hashCode.hash(node.toString() + " -" + i);
            nodeMap.remove(nodeKey);
        }
        return this;
    }


    @Override
    public Map<Integer, T> nodeMap() {
        return Collections.unmodifiableMap(this.nodeMap);
    }
}
