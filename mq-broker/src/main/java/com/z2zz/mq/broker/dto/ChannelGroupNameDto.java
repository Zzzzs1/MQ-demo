package com.z2zz.mq.broker.dto;

import io.netty.channel.Channel;

public class ChannelGroupNameDto {

    /**
     * 分组名称
     */
    private String consumerGroupName;

    /**
     * 通道
     */
    private Channel channel;

    public static ChannelGroupNameDto of(String consumerGroupName,
                                         Channel channel){
        ChannelGroupNameDto  dto = new ChannelGroupNameDto();

        dto.setChannel(channel);
        dto.setConsumerGroupName(consumerGroupName);
        return dto;
    }

    public String getConsumerGroupName() {
        return consumerGroupName;
    }

    public void setConsumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
