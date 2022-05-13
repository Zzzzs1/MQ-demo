package com.z2zz.mq.common.support.hook;


import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;

/**
 * rpc关闭Hook
 * (1)可以添加对应的hook管理类
 */
public abstract class AbstractShutdownHook implements RpcShutdownHook {

    /**
     * AbstractShutdownHook logger
     */
    private static final Log log = LogFactory.getLog(AbstractShutdownHook.class);

    @Override
    public void hook() {
        log.info("[Shutdown Hook] start");
        this.doHook();
        log.info("[Shutdown Hook] end");
    }
    /**
     * 执行hook操作
     */
    protected abstract void doHook();

}
