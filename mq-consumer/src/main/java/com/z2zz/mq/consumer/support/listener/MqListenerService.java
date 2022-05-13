package com.z2zz.mq.consumer.support.listener;


import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.annotation.NotThreadSafe;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.z2zz.mq.common.dto.req.MqMessage;
import com.z2zz.mq.common.resp.ConsumerStatus;
import com.z2zz.mq.consumer.api.IMqConsumerListener;
import com.z2zz.mq.consumer.api.IMqConsumerListenerContext;

@NotThreadSafe
public class MqListenerService implements IMqListenerService {

    private static final Log log = LogFactory.getLog(MqListenerService.class);

    private IMqConsumerListener mqConsumerListener;

    @Override
    public void register(IMqConsumerListener listener) {
        this.mqConsumerListener = listener;
    }

    @Override
    public ConsumerStatus consumer(MqMessage mqMessage, IMqConsumerListenerContext context) {
        if(mqConsumerListener == null){
            log.warn("当前监听类为空，直接忽略处理。message:{}", JSON.toJSON(mqMessage));
            return ConsumerStatus.SUCCESS;
        }else {
            return mqConsumerListener.consumer(mqMessage,context);
        }
    }
}
