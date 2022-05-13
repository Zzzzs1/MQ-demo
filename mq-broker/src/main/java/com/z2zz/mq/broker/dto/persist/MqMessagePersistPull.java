package com.z2zz.mq.broker.dto.persist;

import com.z2zz.mq.common.dto.req.MqConsumerPullReq;
import com.z2zz.mq.common.rpc.RpcAddress;

public class MqMessagePersistPull {

    /**
     * 消息体
     */
    private MqConsumerPullReq pullReq;

    /**
     * 地址信息
     */
    private RpcAddress rpcAddress;

    public MqConsumerPullReq getPullReq() {
        return pullReq;
    }

    public void setPullReq(MqConsumerPullReq pullReq) {
        this.pullReq = pullReq;
    }

    public RpcAddress getRpcAddress() {
        return rpcAddress;
    }

    public void setRpcAddress(RpcAddress rpcAddress) {
        this.rpcAddress = rpcAddress;
    }
}
