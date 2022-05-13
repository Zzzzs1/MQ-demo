package com.z2zz.mq.broker.dto.consumer;


import com.z2zz.loadbalance.support.server.IServer;
import com.z2zz.mq.common.rpc.RpcAddress;

import java.util.Objects;

/**
 * 消费者注册业务对象
 */
public class ConsumerSubscribeBo extends RpcAddress implements IServer {

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 标题名称
     */
    private String topicName;

    /**
     * 标签正则
     */
    private String tagRegex;

    /**
     * 通道标识
     */
    private String channelId;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTagRegex() {
        return tagRegex;
    }

    public void setTagRegex(String tagRegex) {
        this.tagRegex = tagRegex;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ConsumerSubscribeBo that = (ConsumerSubscribeBo) object;
        return Objects.equals(groupName, that.groupName) &&
                Objects.equals(topicName, that.topicName) &&
                Objects.equals(tagRegex, that.tagRegex) &&
                Objects.equals(channelId, that.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName,topicName,tagRegex,channelId);
    }
}
