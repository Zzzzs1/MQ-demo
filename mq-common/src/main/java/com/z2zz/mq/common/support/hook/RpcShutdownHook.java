package com.z2zz.mq.common.support.hook;


/**
 * rpc关闭hook
 * （1）可以添加对应的hook管理类
 */

public interface RpcShutdownHook {

    /**
     * 钩子函数实现
     */
    void hook();


}
