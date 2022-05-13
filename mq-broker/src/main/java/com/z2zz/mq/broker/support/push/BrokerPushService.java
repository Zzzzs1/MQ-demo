package com.z2zz.mq.broker.support.push;

import com.alibaba.fastjson.JSON;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.sisyphus.core.core.Retryer;
import com.z2zz.mq.broker.constant.BrokerRespCode;
import com.z2zz.mq.broker.dto.ChannelGroupNameDto;
import com.z2zz.mq.broker.dto.persist.MqMessagePersistPut;
import com.z2zz.mq.broker.support.persist.IMqBrokerPersist;
import com.z2zz.mq.common.constant.MessageStatusConst;
import com.z2zz.mq.common.constant.MethodType;
import com.z2zz.mq.common.dto.req.MqCommonReq;
import com.z2zz.mq.common.dto.req.MqMessage;
import com.z2zz.mq.common.dto.resp.MqCommonResp;
import com.z2zz.mq.common.dto.resp.MqConsumerResultResp;
import com.z2zz.mq.common.resp.ConsumerStatus;
import com.z2zz.mq.common.resp.MqCommonRespCode;
import com.z2zz.mq.common.resp.MqException;
import com.z2zz.mq.common.rpc.RpcMessageDto;
import com.z2zz.mq.common.support.invoke.IInvokeService;
import com.z2zz.mq.common.util.ChannelUtil;
import com.z2zz.mq.common.util.DelimiterUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrokerPushService implements IBrokerPushService {

    private static final Log log = LogFactory.getLog(BrokerPushService.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();


    @Override
    public void asyncPush(final BrokerPushContext context) {
        EXECUTOR_SERVICE.submit(new Runnable() {
            @Override
            public void run() {
                log.info("开始异步处理 {}", JSON.toJSON(context));
                final MqMessagePersistPut persistPut = context.mqMessagePersistPut();
                final MqMessage mqMessage = persistPut.getMqMessage();
                final List<ChannelGroupNameDto> channelList = context.channelList();
                final IMqBrokerPersist mqBrokerPersist = context.mqBrokerPersist();
                final IInvokeService invokeService = context.invokeService();
                final long responseTime = context.respTimeoutMills();
                final int pushMaxAttempt = context.pushMaxAttempt();

                //更新状态为处理中
                final String messageId = mqMessage.getTraceId();
                log.info("开始更新消息为处理中 :{}",messageId);

                for(final ChannelGroupNameDto channelGroupNameDto : channelList){
                    final Channel channel = channelGroupNameDto.getChannel();
                    final String consumerGroupName = channelGroupNameDto.getConsumerGroupName();

                    try {
                        mqBrokerPersist.updateStatus(messageId,consumerGroupName, MessageStatusConst.TO_CONSUMER_PROCESS);

                        String channelId = ChannelUtil.getChannelId(channel);

                        log.info("开始处理 channelId :{}",channelId);
                        //1.调用
                        mqMessage.setMethodType(MethodType.B_MESSAGE_PUSH);

                        //重试推送
                        MqConsumerResultResp resultResp = Retryer.<MqConsumerResultResp>newInstance()
                                .maxAttempt(pushMaxAttempt)
                                .callable(new Callable<MqConsumerResultResp>() {
                                    @Override
                                    public MqConsumerResultResp call() throws Exception {
                                        MqConsumerResultResp resp = callServer(channel,mqMessage,
                                                MqConsumerResultResp.class,invokeService,responseTime);

                                        //失败校验
                                        if(resp == null
                                        || !ConsumerStatus.SUCCESS.getCode().equals(resp.getConsumerStatus())){
                                            throw new MqException(BrokerRespCode.MSG_PUSH_FAILED);
                                        }
                                        return resp;
                                    }
                                }).retryCall();
                        //2.更新状态
                        //2.1 处理成功，取push消费状态
                        if(MqCommonRespCode.SUCCESS.getCode().equals(resultResp.getRespCode())){
                            mqBrokerPersist.updateStatus(messageId,consumerGroupName,resultResp.getConsumerStatus());
                        }else {
                            //2.2处理失败
                            log.error("消费失败 :{}",JSON.toJSON(resultResp));
                            mqBrokerPersist.updateStatus(messageId,consumerGroupName,MessageStatusConst.TO_CONSUMER_FAILED);
                        }
                        log.info("完成处理 channelId :{}",channelId);
                    }catch (Exception e){
                        log.error("处理异常");
                        mqBrokerPersist.updateStatus(messageId,consumerGroupName,MessageStatusConst.TO_CONSUMER_FAILED);
                    }
                }
                log.info("完成异步处理");
            }
        });
    }


    /**
     * 调用服务端
     * @param channel 调用通道
     * @param commonRep 通用请求
     * @param respClass 类
     * @param invokeService 调用管理类
     * @param respTimeoutMills 响应超时时间
     * @param <T> 泛型
     * @param <R> 结果
     * @return 结果
     */
    private <T extends MqCommonReq,R extends MqCommonResp> R callServer(Channel channel,
                                                                        T commonRep,
                                                                        Class<R> respClass,
                                                                        IInvokeService invokeService,
                                                                        long respTimeoutMills){
        final String traceId = commonRep.getTraceId();
        final long requestTime = System.currentTimeMillis();

        RpcMessageDto rpcMessageDto = new RpcMessageDto();
        rpcMessageDto.setTraceId(traceId);
        rpcMessageDto.setRequestTime(requestTime);
        rpcMessageDto.setJson(JSON.toJSONString(commonRep));
        rpcMessageDto.setMethodType(commonRep.getMethodType());
        rpcMessageDto.setRequest(true);


        //添加调用服务
        invokeService.addRequest(traceId,respTimeoutMills);

        //遍历channel
        //关闭当前线程，以获取对应的信息
        //使用序列化方式
        ByteBuf byteBuf = DelimiterUtil.getMessageDelimiterBuffer(rpcMessageDto);

        //负载均衡获取channel
        channel.writeAndFlush(byteBuf);

        String channelId = ChannelUtil.getChannelId(channel);
        log.debug("[Client] channelId {} 发送消息 {}",channelId,JSON.toJSON(rpcMessageDto));
        if(respClass == null){
            log.debug("[Client] 当前消息为 one-way消息，忽略响应");
            return null;
        }else {
            //channelHandler 中获取对应的响应
            RpcMessageDto messageDto = invokeService.getResponse(traceId);
            if(MqCommonRespCode.TIMEOUT.getCode().equals(messageDto.getRespCode())){
                throw new MqException(MqCommonRespCode.TIMEOUT);
            }

            String respJson = messageDto.getJson();
            return JSON.parseObject(respJson,respClass);
        }

    }
}
