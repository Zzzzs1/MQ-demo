package com.z2zz.mq.producer.core;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.z2zz.loadbalance.api.ILoadBalance;
import com.z2zz.loadbalance.api.impl.LoadBalances;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.z2zz.mq.common.dto.req.MqMessage;
import com.z2zz.mq.common.resp.MqException;
import com.z2zz.mq.common.rpc.RpcChannelFuture;
import com.z2zz.mq.common.support.hook.DefaultShutdownHook;
import com.z2zz.mq.common.support.hook.ShutdownHooks;
import com.z2zz.mq.common.support.invoke.IInvokeService;
import com.z2zz.mq.common.support.invoke.Impl.InvokeService;
import com.z2zz.mq.common.support.status.IStatusManager;
import com.z2zz.mq.common.support.status.StatusManager;
import com.z2zz.mq.producer.api.IMqProducer;
import com.z2zz.mq.producer.constant.ProducerConst;
import com.z2zz.mq.producer.constant.ProducerRespCode;
import com.z2zz.mq.producer.dto.SendBatchResult;
import com.z2zz.mq.producer.dto.SendResult;
import com.z2zz.mq.producer.support.broker.IProducerBrokerService;
import com.z2zz.mq.producer.support.broker.ProducerBrokerConfig;
import com.z2zz.mq.producer.support.broker.ProducerBrokerService;

import java.util.List;

public class MqProducer extends Thread implements IMqProducer {
    private static final Log log = LogFactory.getLog(MqProducer.class);

    /**
     * 分组名称
     */
    private String groupName = ProducerConst.DEFAULT_GROUP_NAME;

    /**
     * 中间人地址
     */
    private String brokerAddress  = "127.0.0.1:9999";

    /**
     * 获取响应超时时间
     * @since 0.0.2
     */
    private long respTimeoutMills = 5000;

    /**
     * 检测 broker 可用性
     * @since 0.0.4
     */
    private volatile boolean check = true;

    /**
     * 调用管理服务
     * @since 0.0.2
     */
    private final IInvokeService invokeService = new InvokeService();

    /**
     * 状态管理类
     * @since 0.0.5
     */
    private final IStatusManager statusManager = new StatusManager();

    /**
     * 生产者-中间服务端服务类
     * @since 0.0.5
     */
    private final IProducerBrokerService producerBrokerService = new ProducerBrokerService();

    /**
     * 为剩余的请求等待时间
     * @since 0.0.5
     */
    private long waitMillsForRemainRequest = 60 * 1000;

    /**
     * 负载均衡策略
     * @since 0.0.7
     */
    private ILoadBalance<RpcChannelFuture> loadBalance = LoadBalances.weightRoundRobbin();

    /**
     * 消息发送最大尝试次数
     * @since 0.0.8
     */
    private int maxAttempt = 3;

    /**
     * 账户标识
     * @since 0.1.4
     */
    private String appKey;

    /**
     * 账户密码
     * @since 0.1.4
     */
    private String appSecret;

    public MqProducer appKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public MqProducer appSecret(String appSecret) {
        this.appSecret = appSecret;
        return this;
    }

    public MqProducer groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public MqProducer brokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
        return this;
    }

    public MqProducer respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public MqProducer check(boolean check) {
        this.check = check;
        return this;
    }

    public MqProducer waitMillsForRemainRequest(long waitMillsForRemainRequest) {
        this.waitMillsForRemainRequest = waitMillsForRemainRequest;
        return this;
    }

    public MqProducer loadBalance(ILoadBalance<RpcChannelFuture> loadBalance) {
        this.loadBalance = loadBalance;
        return this;
    }

    public MqProducer maxAttempt(int maxAttempt) {
        this.maxAttempt = maxAttempt;
        return this;
    }

    /**
     * 参数校验
     */
    private void paramCheck() {
        ArgUtil.notEmpty(groupName, "groupName");
        ArgUtil.notEmpty(brokerAddress, "brokerAddress");
    }


    @Override
    public void run() {
        this.paramCheck();

        //启动服务端
        log.info("MQ 生产者开始启动客户端 GROUP:{} brokerAddress:{}",
                groupName,brokerAddress);

        try {
            //0.配置信息
            ProducerBrokerConfig config = ProducerBrokerConfig.newInstance()
                    .groupName(groupName)
                    .brokerAddress(brokerAddress)
                    .check(check)
                    .respTimeoutMills(respTimeoutMills)
                    .invokeService(invokeService)
                    .statusManager(statusManager)
                    .loadBalance(loadBalance)
                    .maxAttempt(maxAttempt)
                    .appKey(appKey)
                    .appSecret(appSecret);


            //1.初始化
            this.producerBrokerService.initChannelFutureList(config);

            //2.连接到服务端
            this.producerBrokerService.registerToBroker();

            //3.标识为可用
            statusManager.status(true);

            //4.添加钩子函数
            final DefaultShutdownHook rpcShutdownHook = new DefaultShutdownHook();
            rpcShutdownHook.setStatusManager(statusManager);
            rpcShutdownHook.setInvokeService(invokeService);
            rpcShutdownHook.setWaitMillsForRemainRequest(waitMillsForRemainRequest);
            rpcShutdownHook.setDestroyable(this.producerBrokerService);
            ShutdownHooks.rpcShutdownHook(rpcShutdownHook);

            log.info("MQ生产者启动完成");

        }catch (Exception e){
            log.error("MQ生产者启动遇到异常",e);
            //设置为初始化失败
            statusManager.initFailed(true);

            throw new MqException(ProducerRespCode.RPC_INIT_FAILED);
        }
    }

    @Override
    public SendResult send(MqMessage mqMessage) {
        return this.producerBrokerService.send(mqMessage);
    }

    @Override
    public SendResult sendOneWay(MqMessage mqMessage) {
        return this.producerBrokerService.sendOneWay(mqMessage);
    }

    @Override
    public SendBatchResult sendBatch(List<MqMessage> mqMessageList) {
        return producerBrokerService.sendBatch(mqMessageList);
    }

    @Override
    public SendBatchResult sendOneWayBatch(List<MqMessage> mqMessageList) {
        return producerBrokerService.sendOneWayBatch(mqMessageList);
    }


}
