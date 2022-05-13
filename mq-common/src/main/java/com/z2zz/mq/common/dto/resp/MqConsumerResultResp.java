package com.z2zz.mq.common.dto.resp;

/**
 * 消息消费结果
 * @author binbin.hou
 * @since 0.0.3
 */
public class MqConsumerResultResp extends MqCommonResp {

    /**
     * 消费状态
     * @since 0.0.3
     */
    private String consumerStatus;

    public String getConsumerStatus() {
        return consumerStatus;
    }

    public void setConsumerStatus(String consumerStatus) {
        this.consumerStatus = consumerStatus;
    }
}
