package com.z2zz.mq.consumer.support.listener;


import com.z2zz.mq.common.dto.req.MqMessage;
import com.z2zz.mq.common.resp.ConsumerStatus;
import com.z2zz.mq.consumer.api.IMqConsumerListener;
import com.z2zz.mq.consumer.api.IMqConsumerListenerContext;

public interface IMqListenerService {
    /**
     * 注册
     * @param listener 监听器
     */
    void register(final IMqConsumerListener listener);


    /**
     * 消费消息
     * @param mqMessage 消息
     * @param context 上下文
     * @return 结果
     */
ConsumerStatus consumer(final MqMessage mqMessage,
                        final IMqConsumerListenerContext context);
}
