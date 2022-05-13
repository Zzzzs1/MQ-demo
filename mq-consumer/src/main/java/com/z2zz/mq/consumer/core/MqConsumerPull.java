package com.z2zz.mq.consumer.core;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.z2zz.mq.common.constant.ConsumerTypeConst;
import com.z2zz.mq.common.dto.req.MqMessage;
import com.z2zz.mq.common.dto.req.component.MqConsumerUpdateStatusDto;
import com.z2zz.mq.common.dto.resp.MqCommonResp;
import com.z2zz.mq.common.dto.resp.MqConsumerPullResp;
import com.z2zz.mq.common.resp.ConsumerStatus;
import com.z2zz.mq.common.resp.MqCommonRespCode;
import com.z2zz.mq.consumer.api.IMqConsumerListenerContext;
import com.z2zz.mq.consumer.dto.MqTopicTagDto;
import com.z2zz.mq.consumer.support.listener.MqConsumerListenerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MqConsumerPull extends MqConsumerPush {

    private static final Log log = LogFactory.getLog(MqConsumerPull.class);

    /**
     * 拉取定时任务
     */
private final ScheduledExecutorService  scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

/**
 * 单次拉取大小
 */
private int size = 10;

/**
 * 初始化延迟毫秒数
 */
private int pullInitDelaySeconds = 5;

/**
 * 拉取周期
 */
private int pullPeriodSeconds = 5;

/**
 * 订阅列表
 */
private final List<MqTopicTagDto> subscribeList = new ArrayList<>();

/**
 * 状态回执是否批量
 */
private boolean ackBatchFlag = true;

public MqConsumerPull size(int size){
    this.size = size;
    return this;
}
public MqConsumerPull pullInitDelaySeconds (int pullInitDelaySeconds){
    this.pullInitDelaySeconds = pullInitDelaySeconds;
    return this;
}
    public MqConsumerPull pullPeriodSeconds(int pullPeriodSeconds) {
        this.pullPeriodSeconds = pullPeriodSeconds;
        return this;
    }

    public MqConsumerPull ackBatchFlag(boolean ackBatchFlag) {
        this.ackBatchFlag = ackBatchFlag;
        return this;
    }

    @Override
    protected String getConsumerType() {
        return ConsumerTypeConst.PULL;
    }

    @Override
    public void subscribe(String topicName, String tagRegex) {
        MqTopicTagDto tagDto = buildMqTopidTagDto(topicName,tagRegex);

        if(!subscribeList.contains(tagDto)){
            subscribeList.add(tagDto);
        }
    }

    @Override
    public void unSubscribe(String topicName, String tagRegex) {
        MqTopicTagDto tagDto = buildMqTopidTagDto(topicName,tagRegex);
        subscribeList.remove(tagDto);
    }

    private MqTopicTagDto buildMqTopidTagDto(String topicName,String tagRegex){
    MqTopicTagDto dto = new MqTopicTagDto();
    dto.setTagRegex(tagRegex);
    dto.setTopicName(topicName);
    //主动拉取这里应该会有问题，会导致不同的groupName的消息，实际上已经被消费了
        //所以实际上应该有一个消息+group的映射关系表，单个消息可以被多次重复消费
        //groupName+messageId+status==>在数据库层面实现
        dto.setGroupName(groupName);
        return dto;
    }

    /**
     * 初始化拉取消息
     */
    @Override
    protected void afterInit() {

        //5s发一次心跳
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(CollectionUtil.isEmpty(subscribeList)) {
                    log.warn("订阅列表为空，忽略处理。");
                    return;
                }
                for(MqTopicTagDto tagDto:subscribeList){
                    final String topicName = tagDto.getTopicName();
                    final String tagRegex = tagDto.getTagRegex();
                    MqConsumerPullResp resp = consumerBrokerService.pull(topicName,tagRegex,size);
                    if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())){
                        List<MqMessage> mqMessageList = resp.getList();
                        if(CollectionUtil.isNotEmpty(mqMessageList)){
                            List<MqConsumerUpdateStatusDto> statusDtoList = new ArrayList<>(mqMessageList.size());
                            for(MqMessage mqMessage : mqMessageList){
                                IMqConsumerListenerContext context = new MqConsumerListenerContext();
                                final String messageId = mqMessage.getTraceId();
                                ConsumerStatus consumerStatus = mqListenerService.consumer(mqMessage,context);
                                log.info("消息：{} 消费结果 {}",messageId,consumerStatus);

                                //状态同步更新
                                if(!ackBatchFlag){
                                    MqCommonResp ackResp = consumerBrokerService.consumerStatusAck(messageId,consumerStatus);
                                    log.info("消息：{} 状态回执结果 {}",messageId, JSON.toJSON(ackResp));
                                }else {
                                    //批量
                                    MqConsumerUpdateStatusDto statusDto = new MqConsumerUpdateStatusDto();
                                    statusDto.setMessageId(messageId);
                                    statusDto.setMessageStatus(consumerStatus.getCode());
                                    statusDto.setConsumerGroupName(groupName);
                                    statusDtoList.add(statusDto);
                                }
                            }
                            //批量执行
                            if(ackBatchFlag){
                                MqCommonResp ackResp = consumerBrokerService.consumerStatusAckBatch(statusDtoList);
                                log.info("消息:{} 状态批量回执结果 {}",statusDtoList,JSON.toJSON(ackResp));
                                statusDtoList = null;
                            }
                        }else {
                            log.error("拉取消息失败:{}",JSON.toJSON(resp));
                        }
                    }
                }
            }
        },pullInitDelaySeconds,pullPeriodSeconds, TimeUnit.SECONDS);
    }
}
