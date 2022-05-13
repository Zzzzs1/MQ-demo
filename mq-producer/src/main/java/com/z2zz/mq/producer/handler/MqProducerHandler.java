package com.z2zz.mq.producer.handler;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.z2zz.mq.common.rpc.RpcMessageDto;
import com.z2zz.mq.common.support.invoke.IInvokeService;
import com.z2zz.mq.common.util.ChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MqProducerHandler extends SimpleChannelInboundHandler {

    private static final Log log = LogFactory.getLog(MqProducerHandler.class);


    /**
     * 调用管理类
     */
    private IInvokeService invokeService;

    public void setInvokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        String text = new String(bytes);
        log.debug("[Client] channelId {} 接收到消息 {}", ChannelUtil.getChannelId(ctx),text);

        RpcMessageDto rpcMessageDto = null;
        try {
            rpcMessageDto = JSON.parseObject(bytes,RpcMessageDto.class);
        }catch (Exception e){
            log.error("RpcMessageDto json 格式转换异常{}",JSON.parse(bytes));
            return;
        }
        if(rpcMessageDto.isRequest()){

            //请求类
            final String methodType = rpcMessageDto.getMethodType();
            final String json = rpcMessageDto.getJson();
        }else {
            //丢掉traceId为空的消息
            if(StringUtil.isBlank(rpcMessageDto.getTraceId())){
                log.debug("[Client] response traceId 为空 直接丢弃",JSON.toJSON(rpcMessageDto));
                return;
            }
            invokeService.addResponse(rpcMessageDto.getTraceId(),rpcMessageDto);
            log.debug("[Client] response is :{}",JSON.toJSON(rpcMessageDto));
        }
    }
}
