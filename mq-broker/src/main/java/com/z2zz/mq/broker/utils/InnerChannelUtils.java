package com.z2zz.mq.broker.utils;

import com.z2zz.mq.broker.dto.BrokerServiceEntryChannel;
import com.z2zz.mq.broker.dto.ServiceEntry;
import com.z2zz.mq.common.rpc.RpcChannelFuture;
import io.netty.channel.Channel;

import javax.print.DocFlavor;

public class InnerChannelUtils {

    private InnerChannelUtils(){}

    /**
     * 构建基本服务地址
     * @param rpcChannelFuture 信息
     * @return
     */
    public static ServiceEntry buildServiceEntry(RpcChannelFuture rpcChannelFuture){
        ServiceEntry serviceEntry = new ServiceEntry();

        serviceEntry.setAddress(rpcChannelFuture.getAddress());
        serviceEntry.setPort(rpcChannelFuture.getPort());
        serviceEntry.setWeight(rpcChannelFuture.getWeight());
        return serviceEntry;
    }


    public static BrokerServiceEntryChannel buildEntryChannel(ServiceEntry serviceEntry,
                                                              Channel channel){
        BrokerServiceEntryChannel result = new BrokerServiceEntryChannel();
        result.setChannel(channel);
        result.setGroupName(serviceEntry.getGroupName());
        result.setAddress(serviceEntry.getAddress());
        result.setPort(serviceEntry.getPort());
        result.setWeight(serviceEntry.getWeight());
        return result;
    }



}
