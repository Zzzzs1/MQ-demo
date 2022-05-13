package com.z2zz.mq.broker.dto;


import io.netty.channel.Channel;

public class BrokerServiceEntryChannel extends ServiceEntry {

    private Channel channel;

    /**
     * 最后访问时间
     */
    private long lastAccessTime;

    public Channel getChannel(){return channel;}

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
