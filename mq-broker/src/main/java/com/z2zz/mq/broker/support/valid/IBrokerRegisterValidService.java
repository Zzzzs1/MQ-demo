package com.z2zz.mq.broker.support.valid;


import com.z2zz.mq.broker.dto.BrokerRegisterReq;

/**
 * 注册验证方法
 */
public interface IBrokerRegisterValidService {


    /**
     * 生产者验证合法性
     * @param registerReq 注册信息
     * @return
     */
    boolean producerValid(BrokerRegisterReq registerReq);

    /**
     * 消费者验证合法性
     * @param registerReq 注册信息
     * @return
     */
    boolean consumerValid(BrokerRegisterReq registerReq);


}
