package com.z2zz.mq.broker.support.api;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.heaven.util.util.MapUtil;
import com.github.houbb.heaven.util.util.regex.RegexUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.z2zz.loadbalance.api.ILoadBalance;
import com.z2zz.mq.broker.api.IBrokerConsumerService;
import com.z2zz.mq.broker.constant.BrokerConst;
import com.z2zz.mq.broker.dto.BrokerServiceEntryChannel;
import com.z2zz.mq.broker.dto.ChannelGroupNameDto;
import com.z2zz.mq.broker.dto.ServiceEntry;
import com.z2zz.mq.broker.dto.consumer.ConsumerSubscribeBo;
import com.z2zz.mq.broker.dto.consumer.ConsumerSubscribeReq;
import com.z2zz.mq.broker.dto.consumer.ConsumerUnSubscribeReq;
import com.z2zz.mq.broker.resp.MqBrokerRespCode;
import com.z2zz.mq.broker.utils.InnerChannelUtils;
import com.z2zz.mq.common.dto.req.MqHeartBeatReq;
import com.z2zz.mq.common.dto.req.MqMessage;
import com.z2zz.mq.common.dto.resp.MqCommonResp;
import com.z2zz.mq.common.resp.MqCommonRespCode;
import com.z2zz.mq.common.resp.MqException;
import com.z2zz.mq.common.util.ChannelUtil;
import com.z2zz.mq.common.util.RandomUtils;
import io.netty.channel.Channel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocalBrokerConsumerService implements IBrokerConsumerService {

    private static final Log log = LogFactory.getLog(LocalBrokerConsumerService.class);

    private final Map<String, BrokerServiceEntryChannel> registerMap = new ConcurrentHashMap<>();

    /**
     * 订阅集合-推送策略
     * key: topicName
     * value: 对应的订阅列表
     */
    private final Map<String, Set<ConsumerSubscribeBo>> pushSubscribeMap = new ConcurrentHashMap<>();

    /**
     * 心跳map
     */
    private final Map<String,BrokerServiceEntryChannel> heartbeatMap = new ConcurrentHashMap<>();

    /**
     * 心跳定时任务
     */
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * 负载均衡策略

     */
    private ILoadBalance<ConsumerSubscribeBo> loadBalance;

    public LocalBrokerConsumerService(){
        //120s扫描一次
        final long limitMills = 2*60*1000;

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<String,BrokerServiceEntryChannel> entry : heartbeatMap.entrySet()){
                    String key = entry.getKey();
                    long lastAccessTime  = entry.getValue().getLastAccessTime();
                    long currentTime = System.currentTimeMillis();
                    if(currentTime - lastAccessTime > limitMills){
                        removeByChannelId(key);
                    }
                }
            }
        },2*60,2*60, TimeUnit.SECONDS);
    }

    /**
     * 根据channelId 移除信息
     * @param channelId
     */
    private void removeByChannelId(final String channelId){
        BrokerServiceEntryChannel channelRegister = registerMap.remove(channelId);
        log.info("移除注册信息 id :{},channel :{}",channelId, JSON.toJSON(channelId));
        BrokerServiceEntryChannel channelHeartBeat = heartbeatMap.remove(channelId);
        log.info("移除心跳信息 id :{},channel :{}",channelId,JSON.toJSON(channelHeartBeat));
    }

    @Override
    public void loadBalance(ILoadBalance<ConsumerSubscribeBo> loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public MqCommonResp register(ServiceEntry serviceEntry, Channel channel) {
        final String channelId = ChannelUtil.getChannelId(channel);
        BrokerServiceEntryChannel entryChannel = InnerChannelUtils.buildEntryChannel(serviceEntry, channel);
        registerMap.put(channelId, entryChannel);

        entryChannel.setLastAccessTime(System.currentTimeMillis());
        heartbeatMap.put(channelId, entryChannel);

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }

    @Override
    public MqCommonResp unRegister(ServiceEntry serviceEntry, Channel channel) {
        final String channelId = ChannelUtil.getChannelId(channel);
        removeByChannelId(channelId);

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }

    @Override
    public MqCommonResp subscribe(ConsumerSubscribeReq serviceEntry, Channel clientChannel) {
        final String channelId = ChannelUtil.getChannelId(clientChannel);
        final String topicName = serviceEntry.getTopicName();

        final String consumerType = serviceEntry.getConsumerType();
        Map<String,Set<ConsumerSubscribeBo>> subscribeMap = getSubscribeMapByConsumerType(consumerType);

        ConsumerSubscribeBo subscribeBo = new ConsumerSubscribeBo();
        subscribeBo.setChannelId(channelId);
        subscribeBo.setGroupName(serviceEntry.getGroupName());
        subscribeBo.setTopicName(topicName);
        subscribeBo.setTagRegex(serviceEntry.getTagRegex());

        //放入集合
        MapUtil.putToSetMap(subscribeMap,topicName,subscribeBo);

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());

        return resp;

    }

    private Map<String,Set<ConsumerSubscribeBo>> getSubscribeMapByConsumerType(String consumerType){
        return pushSubscribeMap;
    }


    @Override
    public MqCommonResp unSubscribe(ConsumerUnSubscribeReq serviceEntry, Channel clientChannel) {
        final String channelId = ChannelUtil.getChannelId(clientChannel);
        final String topicName = serviceEntry.getTopicName();
        final String consumerType = serviceEntry.getConsumerType();
        Map<String,Set<ConsumerSubscribeBo>> subscribeMap = getSubscribeMapByConsumerType(consumerType);

        ConsumerSubscribeBo subscribeBo = new ConsumerSubscribeBo();
        subscribeBo.setChannelId(channelId);
        subscribeBo.setGroupName(serviceEntry.getGroupName());
        subscribeBo.setTopicName(topicName);
        subscribeBo.setTagRegex(serviceEntry.getTagRegex());


        //集合
        Set<ConsumerSubscribeBo> set = subscribeMap.get(topicName);
        if(CollectionUtil.isNotEmpty(set)){
            set.remove(subscribeBo);
        }

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }


    @Override
    public List<ChannelGroupNameDto> getPushSubscribeList(MqMessage mqMessage) {
        final String topicName = mqMessage.getTopic();
        Set<ConsumerSubscribeBo> set = pushSubscribeMap.get(topicName);
        if(CollectionUtil.isEmpty(set)){
            return Collections.emptyList();
        }


        //2.获取匹配的tag列表
        final List<String> tagNameList = mqMessage.getTags();

        Map<String,List<ConsumerSubscribeBo>> groupMap = new HashMap<>();
        for(ConsumerSubscribeBo bo:set){
            String tagRegex = bo.getTagRegex();

            if(RegexUtil.hasMatch(tagNameList,tagRegex)){
                String groupName = bo.getGroupName();

                MapUtil.putToListMap(groupMap,groupName,bo);
            }
        }

        //3.按照groupName分组之后，每一组只随机返回一个，最好应该调整为以shardingkey 选择
        final String shardingKey = mqMessage.getShardingKey();
        List<ChannelGroupNameDto> channelGroupNameList = new ArrayList<>();

        for(Map.Entry<String,List<ConsumerSubscribeBo>> entry : groupMap.entrySet()){
            List<ConsumerSubscribeBo> list = entry.getValue();

            ConsumerSubscribeBo bo = RandomUtils.loadBalance(loadBalance,list,shardingKey);
            final String channelId = bo.getChannelId();
            BrokerServiceEntryChannel entryChannel = registerMap.get(channelId);
            if(entryChannel == null){
                log.warn("channelId :{} 对应的通道信息为空",channelId);
                continue;
            }

            final String groupName = entry.getKey();
            ChannelGroupNameDto channelGroupNameDto = ChannelGroupNameDto.of(groupName, entryChannel.getChannel());
            channelGroupNameList.add(channelGroupNameDto);

        }
        return channelGroupNameList;
    }

    @Override
    public void heartbeat(MqHeartBeatReq mqHeartBeatReq, Channel channel) {
        final String channelId = ChannelUtil.getChannelId(channel);
        log.info("[HEARTBEAT] 接受消费者心跳 {}, channelId :{}",
                JSON.toJSON(mqHeartBeatReq),channelId);
    }

    @Override
    public void checkValid(String channelId) {
        if(!registerMap.containsKey(channelId)){
            log.error("channelId :{} 未注册",channelId);
            throw new MqException(MqBrokerRespCode.C_REGISTER_CHANNEL_NOT_VALID);
        }
    }
}
