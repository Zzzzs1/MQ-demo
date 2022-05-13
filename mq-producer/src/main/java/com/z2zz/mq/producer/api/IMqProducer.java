package com.z2zz.mq.producer.api;

import com.z2zz.mq.common.dto.req.MqMessage;
import com.z2zz.mq.producer.dto.SendBatchResult;
import com.z2zz.mq.producer.dto.SendResult;

import java.util.List;

public interface IMqProducer {

    /**
     * 同步发送消息
     * @param mqMessage 消息类型
     * @return 结果
     */
    SendResult send(final MqMessage mqMessage);

    /**
     * 单向发送消息
     * @param mqMessage 消息类型
     * @return
     */
    SendResult sendOneWay(final MqMessage mqMessage);

    /**
     * 同步发送消息-批量
     * @param mqMessageList 消息类型
     * @return
     */
    SendBatchResult sendBatch(final List<MqMessage> mqMessageList);


    /**
     * 单向发送消息-批量
     * @param mqMessageList 消息类型
     * @return
     */
    SendBatchResult sendOneWayBatch(final List<MqMessage> mqMessageList);
}
