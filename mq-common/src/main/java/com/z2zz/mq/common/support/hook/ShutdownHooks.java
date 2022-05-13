package com.z2zz.mq.common.support.hook;

public class ShutdownHooks {

    private ShutdownHooks(){}

    /**
     * 添加rpc shutdown hook
     * @param rpcShutdownHook 钩子函数实现
     */
    public static void rpcShutdownHook(final RpcShutdownHook rpcShutdownHook){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                rpcShutdownHook.hook();
            }
        });
    }

}
