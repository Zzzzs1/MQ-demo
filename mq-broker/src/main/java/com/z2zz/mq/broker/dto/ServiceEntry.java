package com.z2zz.mq.broker.dto;

import com.z2zz.mq.common.rpc.RpcAddress;

public class ServiceEntry extends RpcAddress {

    /**
     * 分组名称
     */
    private String groupName;

    public String getGroupName(){
        return groupName;
    }

    public void setGroupName(String groupName){
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "ServiceEntry{" +
                "groupName='" + groupName + '\'' +
                "} " + super.toString();
    }
}
