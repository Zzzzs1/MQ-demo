package com.z2zz.consistenhashing.api;

import java.util.Map;

public interface IConsistentHashing<T> {

    /**
     * 获取对应的节点
     *
     * @param key key
     * @return 节点
     */
    T get(final String key);

    /**
     * 添加节点
     * @param node 节点
     * @return
     */
    IConsistentHashing add(final T node);

    /**
     * 移除节点
     * @param node 节点
     * @return this
     */
    IConsistentHashing remove(final T node);

    /**
     * 获取节点信息
     * @return 节点
     */
    Map<Integer,T> nodeMap();

}
