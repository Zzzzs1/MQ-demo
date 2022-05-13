package com.z2zz.mq.consumer.core;


import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.z2zz.loadbalance.api.ILoadBalance;
import com.z2zz.loadbalance.api.impl.LoadBalances;
import com.z2zz.mq.common.constant.ConsumerTypeConst;
import com.z2zz.mq.common.resp.MqException;
import com.z2zz.mq.common.rpc.RpcChannelFuture;
import com.z2zz.mq.common.support.hook.DefaultShutdownHook;
import com.z2zz.mq.common.support.hook.ShutdownHooks;
import com.z2zz.mq.common.support.invoke.IInvokeService;
import com.z2zz.mq.common.support.invoke.Impl.InvokeService;
import com.z2zz.mq.common.support.status.IStatusManager;
import com.z2zz.mq.common.support.status.StatusManager;
import com.z2zz.mq.consumer.api.IMqConsumer;
import com.z2zz.mq.consumer.api.IMqConsumerListener;
import com.z2zz.mq.consumer.constant.ConsumerConst;
import com.z2zz.mq.consumer.constant.ConsumerRespCode;
import com.z2zz.mq.consumer.support.broker.ConsumerBrokerConfig;
import com.z2zz.mq.consumer.support.broker.ConsumerBrokerService;
import com.z2zz.mq.consumer.support.broker.IConsumerBrokerService;
import com.z2zz.mq.consumer.support.listener.IMqListenerService;
import com.z2zz.mq.consumer.support.listener.MqListenerService;

/**
 * 推送消费策略
 */
public class MqConsumerPush extends Thread implements IMqConsumer {

    private static  final Log log = LogFactory.getLog(MqConsumerPush.class);

    /**
     * 组名称
     */
    protected String groupName = ConsumerConst.DEFAULT_GROUP_NAME;

    /**
     * 中间人地址
     */
    protected String brokerAddress = "127.0.0.1:9999";

    /**
     * 获取响应超时时间
     */
    protected long respTimeoutMills = 5000;

    /**
     * 检测broker可用性
     */
    protected volatile boolean check = true;

    /**
     * 为剩余的请求等待时间
     */
    protected long waitMillsForRemainRequest = 60*1000;

    /**
     * 调用管理类
     */
    protected final IInvokeService invokeService = new InvokeService();

    /**
     * 消息监听服务类
     */
    protected final IMqListenerService mqListenerService = new MqListenerService();

    /**
     * 状态管理类
     */
    protected final IStatusManager statusManager = new StatusManager();

    /**
     * 生产者-中间服务端服务类
     */
    protected final IConsumerBrokerService consumerBrokerService = new ConsumerBrokerService();

    /**
     * 负载均衡策略
     */
    protected ILoadBalance<RpcChannelFuture> loadBalance = LoadBalances.weightRoundRobbin();

    /**
     * 订阅最大尝试次数
     */
    protected int subscribeMaxAttempt = 3;

    /**
     * 取消订阅最大尝试次数
     */
    protected int unSubscribeMaxAttempt = 3;

    /**
     * 消费状态更新最大尝试次数
     */
    protected int consumerStatusMaxAttempt = 3;

    /**
     * 账户标识
     */
    protected String appKey;

    /**
     * 账户密码
     */
    protected String appSecret;
    public String appKey() {
        return appKey;
    }

    public MqConsumerPush appKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String appSecret() {
        return appSecret;
    }

    public MqConsumerPush appSecret(String appSecret) {
        this.appSecret = appSecret;
        return this;
    }

    public MqConsumerPush consumerStatusMaxAttempt(int consumerStatusMaxAttempt) {
        this.consumerStatusMaxAttempt = consumerStatusMaxAttempt;
        return this;
    }

    public MqConsumerPush subscribeMaxAttempt(int subscribeMaxAttempt) {
        this.subscribeMaxAttempt = subscribeMaxAttempt;
        return this;
    }

    public MqConsumerPush unSubscribeMaxAttempt(int unSubscribeMaxAttempt) {
        this.unSubscribeMaxAttempt = unSubscribeMaxAttempt;
        return this;
    }

    public MqConsumerPush groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public MqConsumerPush brokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
        return this;
    }

    public MqConsumerPush respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public MqConsumerPush check(boolean check) {
        this.check = check;
        return this;
    }

    public MqConsumerPush waitMillsForRemainRequest(long waitMillsForRemainRequest) {
        this.waitMillsForRemainRequest = waitMillsForRemainRequest;
        return this;
    }

    public MqConsumerPush loadBalance(ILoadBalance<RpcChannelFuture> loadBalance) {
        this.loadBalance = loadBalance;
        return this;
    }

    /**
     * 参数校验
     */
    private void paramCheck(){
        ArgUtil.notEmpty(brokerAddress,"brokerAddress");
        ArgUtil.notEmpty(groupName,"groupName");
    }

    @Override
    public void run() {
        // 启动服务端
        log.info("MQ 消费者开始启动服务端 groupName :{}, brokerAddress:{}",groupName,brokerAddress);

        //1.参数校验
        this.paramCheck();

        try {
            //0.配置信息
            ConsumerBrokerConfig config = ConsumerBrokerConfig.newInstance()
                    .groupName(groupName).brokerAddress(brokerAddress).check(check)
                    .respTimeoutMills(respTimeoutMills).invokeService(invokeService)
                    .statusManager(statusManager).mqListenerService(mqListenerService)
                    .loadBalance(loadBalance).subscribeMaxAttempt(subscribeMaxAttempt)
                    .unSubscribeMaxAttempt(unSubscribeMaxAttempt).consumerStatusMaxAttempt(consumerStatusMaxAttempt)
                    .appKey(appKey).appSecret(appSecret);

            //1.初始化
            this.consumerBrokerService.initChannelFutureList(config);

            //2.连接到服务端
            this.consumerBrokerService.registerToBroker();

            //3.标识为可用
            statusManager.status(true);

            //4.添加钩子函数
            final DefaultShutdownHook rpcShutdownHook = new DefaultShutdownHook();
            rpcShutdownHook.setStatusManager(statusManager);
            rpcShutdownHook.setInvokeService(invokeService);
            rpcShutdownHook.setWaitMillsForRemainRequest(waitMillsForRemainRequest);
            rpcShutdownHook.setDestroyable(this.consumerBrokerService);
            ShutdownHooks.rpcShutdownHook(rpcShutdownHook);

            //5.启动完成以后的时间
            afterInit();

            log.info("MQ 消费者启动完成");
        }catch (Exception e){
            log.error("MQ 消费者启动异常",e);

            statusManager.initFailed(true);

            throw  new MqException(ConsumerRespCode.RPC_INIT_FAILED);
        }
    }

    /**
     * 初始化完成后
     */
    protected void afterInit(){}

    @Override
    public void subscribe(String topicName, String tagRegex) {
        final String consumerType = getConsumerType();
        consumerBrokerService.subscribe(topicName,tagRegex,consumerType);
    }

    /**
     * 获取消费策略类型
     * @return
     */
    protected String getConsumerType(){
        return ConsumerTypeConst.PUSH;
    }



    @Override
    public void unSubscribe(String topicName, String tagRegex) {
        final String consumerType = getConsumerType();
        consumerBrokerService.unSubscribe(topicName,tagRegex,consumerType);
    }

    @Override
    public void registerListener(IMqConsumerListener listener) {
        this.mqListenerService.register(listener);
    }
}
