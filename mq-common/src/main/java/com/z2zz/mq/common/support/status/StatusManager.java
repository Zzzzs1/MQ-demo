package com.z2zz.mq.common.support.status;

public class StatusManager implements IStatusManager  {

    private boolean status;

    private boolean initFailed;


    @Override
    public boolean status() {
        return this.status;
    }

    @Override
    public IStatusManager status(boolean status) {
        this.status = status;

        return this;
    }

    @Override
    public boolean initFailed() {
        return initFailed;
    }

    @Override
    public IStatusManager initFailed(boolean failed) {

        this.initFailed = initFailed;

        return this;
    }
}
