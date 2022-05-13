package com.z2zz.mq.broker.core;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.z2zz.loadbalance.api.ILoadBalance;
import com.z2zz.loadbalance.api.impl.LoadBalances;
import com.z2zz.mq.broker.api.IBrokerConsumerService;
import com.z2zz.mq.broker.api.IBrokerProducerService;
import com.z2zz.mq.broker.api.IMqBroker;
import com.z2zz.mq.broker.constant.BrokerConst;
import com.z2zz.mq.broker.constant.BrokerRespCode;
import com.z2zz.mq.broker.dto.consumer.ConsumerSubscribeBo;
import com.z2zz.mq.broker.handler.MqBrokerHandler;
import com.z2zz.mq.broker.support.api.LocalBrokerConsumerService;
import com.z2zz.mq.broker.support.api.LocalBrokerProducerService;
import com.z2zz.mq.broker.support.persist.IMqBrokerPersist;
import com.z2zz.mq.broker.support.persist.LocalMqBrokerPersist;
import com.z2zz.mq.broker.support.push.BrokerPushService;
import com.z2zz.mq.broker.support.push.IBrokerPushService;
import com.z2zz.mq.broker.support.valid.BrokerRegisterValidService;
import com.z2zz.mq.broker.support.valid.IBrokerRegisterValidService;
import com.z2zz.mq.common.resp.MqException;
import com.z2zz.mq.common.support.invoke.IInvokeService;
import com.z2zz.mq.common.support.invoke.Impl.InvokeService;
import com.z2zz.mq.common.util.DelimiterUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class MqBroker extends Thread implements IMqBroker {

    private static final Log log = LogFactory.getLog(MqBroker.class);

    /**
     * 端口号
     */
    private int port = BrokerConst.DEFAULT_PORT;

    /**
     * 调用管理类
     *
     * @since 1.0.0
     */
    private final IInvokeService invokeService = new InvokeService();

    /**
     * 消费者管理
     *
     * @since 0.0.3
     */
    private IBrokerConsumerService registerConsumerService = new LocalBrokerConsumerService();

    /**
     * 生产者管理
     *
     * @since 0.0.3
     */
    private IBrokerProducerService registerProducerService = new LocalBrokerProducerService();

    /**
     * 持久化类
     *
     * @since 0.0.3
     */
    private IMqBrokerPersist mqBrokerPersist = new LocalMqBrokerPersist();

    /**
     * 推送服务
     *
     * @since 0.0.3
     */
    private IBrokerPushService brokerPushService = new BrokerPushService();

    /**
     * 获取响应超时时间
     * @since 0.0.3
     */
    private long respTimeoutMills = 5000;

    /**
     * 负载均衡
     * @since 0.0.7
     */
    private ILoadBalance<ConsumerSubscribeBo> loadBalance = LoadBalances.weightRoundRobbin();

    /**
     * 推送最大尝试次数
     * @since 0.0.8
     */
    private int pushMaxAttempt = 3;

    /**
     * 注册验证服务类
     * @since 0.1.4
     */
    private IBrokerRegisterValidService brokerRegisterValidService = new BrokerRegisterValidService();

    public MqBroker port(int port) {
        this.port = port;
        return this;
    }

    public MqBroker registerConsumerService(IBrokerConsumerService registerConsumerService) {
        this.registerConsumerService = registerConsumerService;
        return this;
    }

    public MqBroker registerProducerService(IBrokerProducerService registerProducerService) {
        this.registerProducerService = registerProducerService;
        return this;
    }

    public MqBroker mqBrokerPersist(IMqBrokerPersist mqBrokerPersist) {
        this.mqBrokerPersist = mqBrokerPersist;
        return this;
    }

    public MqBroker brokerPushService(IBrokerPushService brokerPushService) {
        this.brokerPushService = brokerPushService;
        return this;
    }

    public MqBroker respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public MqBroker loadBalance(ILoadBalance<ConsumerSubscribeBo> loadBalance) {
        this.loadBalance = loadBalance;
        return this;
    }

    public MqBroker pushMaxAttempt(int pushMaxAttempt) {
        this.pushMaxAttempt = pushMaxAttempt;
        return this;
    }

    public MqBroker brokerRegisterValidService(IBrokerRegisterValidService brokerRegisterValidService) {
        this.brokerRegisterValidService = brokerRegisterValidService;
        return this;
    }

    public ChannelHandler initChannelHandler(){
        registerConsumerService.loadBalance(this.loadBalance);

        MqBrokerHandler handler = new MqBrokerHandler();

        handler.invokeService(invokeService)
                .respTimeoutMills(respTimeoutMills)
                .registerConsumerService(registerConsumerService)
                .registerProducerService(registerProducerService)
                .mqBrokerPersist(mqBrokerPersist)
                .brokerPushService(brokerPushService)
                .respTimeoutMills(respTimeoutMills)
                .pushMaxAttempt(pushMaxAttempt)
                .brokerRegisterValidService(brokerRegisterValidService);

        return handler;
    }

    @Override
    public void run() {
        //启动服务端
        log.info("MQ 中间人开始启动服务端 port :{}",port);

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            final ByteBuf delimiterBuf = DelimiterUtil.getByteBuf(DelimiterUtil.DELIMITER);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(workerGroup,bossGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH,delimiterBuf))
                                    .addLast(initChannelHandler());
                        }
                    })
                    //这个参数影响的是还没有被accept取出的链接
            .option(ChannelOption.SO_BACKLOG,128)
                    //这个参数只是过一段时间内客户端没有响应，服务端会发送一个ack包，以判断客户端是否还活着
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            //绑定端口，开始接受进来的链接
            ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly();
            log.info("MQ 中间人启动完成，监听{"+port+"} 端口");

            channelFuture.channel().closeFuture().syncUninterruptibly();
            log.info("MQ 中间人关闭完成");
        }catch (Exception e){
            log.error("MQ 中间人启动异常",e);
            throw new MqException(BrokerRespCode.RPC_INIT_FAILED);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
