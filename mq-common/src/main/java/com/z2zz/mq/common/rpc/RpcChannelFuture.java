package com.z2zz.mq.common.rpc;

import io.netty.channel.ChannelFuture;


public class RpcChannelFuture extends RpcAddress {

    /**
     * 信息
     */
    private ChannelFuture channelFuture;

    public ChannelFuture getChannelFuture(){
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }
}
