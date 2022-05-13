package com.z2zz.mq.common.support.status;

public interface IStatusManager {

    /**
     * 获取状态编码
     */
    boolean status();

    /**
     * 设置状态编码
     * @param status 编码
     * @return this
     */
    IStatusManager status(final boolean status);

    /**
     * 初始化失败
     * @return 初始化失败
     */
    boolean initFailed();

    /**
     * 设置初始化失败
     * @param failed 编码
     * @return this
     */
    IStatusManager initFailed(final boolean failed);


}
