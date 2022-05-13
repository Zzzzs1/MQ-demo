package com.z2zz.mq.broker.support.valid;

import com.z2zz.mq.broker.dto.BrokerRegisterReq;

public class BrokerRegisterValidService implements IBrokerRegisterValidService {

    @Override
    public boolean producerValid(BrokerRegisterReq registerReq) {
        return true;
    }

    @Override
    public boolean consumerValid(BrokerRegisterReq registerReq) {
        return true;
    }
}
