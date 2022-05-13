package com.z2zz.mq.broker.support.push;


/**
 * 消息推送服务
 */
public interface IBrokerPushService {

    /**
     * 异步推送
     * @param context 消息
     */
    void asyncPush(final BrokerPushContext context);



}
