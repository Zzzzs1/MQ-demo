package com.z2zz.mq.common.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * channel 工具类
 */
public class ChannelUtil {

    private ChannelUtil(){}

    /**
     * 获取channel 标识
     * @param channel 管道
     * @return
     */
    public static String getChannelId(Channel channel){
        return channel.id().asLongText();
    }

    /**
     * 获取channel 标识
     * @param ctx 管道
     * @return
     */
    public static String getChannelId(ChannelHandlerContext ctx){
        return getChannelId(ctx.channel());
    }



}
