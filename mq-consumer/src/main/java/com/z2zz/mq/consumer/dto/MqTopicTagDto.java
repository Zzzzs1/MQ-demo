package com.z2zz.mq.consumer.dto;

import java.util.Objects;

public class MqTopicTagDto {

    /**
     * 消费者分组名称
     */
    private String groupName;

    /**
     * 标题名称
     */
    private String topicName;

    /**
     * 标签名称
     */
    private String tagRegex;

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

    @Override
    public int hashCode() {
        return Objects.hash(groupName,topicName,tagRegex);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass())
            return false;
        MqTopicTagDto tagDto = (MqTopicTagDto) obj;
        return Objects.equals(groupName,tagDto.groupName)&&
                Objects.equals(topicName,tagDto.topicName)&&
                Objects.equals(tagRegex,tagDto.tagRegex);
    }
}
