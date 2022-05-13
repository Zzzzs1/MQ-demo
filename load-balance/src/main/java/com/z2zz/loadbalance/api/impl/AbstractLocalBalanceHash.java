package com.z2zz.loadbalance.api.impl;

import com.github.houbb.hash.api.IHashCode;
import com.z2zz.loadbalance.support.server.IServer;

public abstract class AbstractLocalBalanceHash<T extends IServer> extends AbstractLoadBalance<T> {

    /**
     * hash策略
     */
    protected final IHashCode hashCode;

    public AbstractLocalBalanceHash(IHashCode hashCode){
        this.hashCode = hashCode;
    }


}
