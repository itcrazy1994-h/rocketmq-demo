package com.example.demo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Data
@Configuration
@ConfigurationProperties(prefix = "rocketmq.producer")
@Slf4j
public class MQProducerConfigure {
    //group 代表具有相同角色的生产者组合或消费者组合，称为生产者组或消费者组
    private String groupName;
    private String namesrvAddr;
    // 消息最大值
    private Integer maxMessageSize;
    // 消息发送超时时间
    private Integer sendMsgTimeOut;
    // 失败重试次数
    private Integer retryTimesWhenSendFailed;
    private boolean  isOffTransaction;
    private String transactionGroupName;
    private ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("client-transaction-msg-check-thread");
            return thread;
        }
    });

    /**
     * mq 生成者配置
     * @return
     * @throws MQClientException
     */
    @Bean("defaultProducer")
    @ConditionalOnProperty(prefix = "rocketmq.producer", value = "isOnOff", havingValue = "on")
    public DefaultMQProducer defaultProducer() throws MQClientException {
        log.info("defaultProducer 正在创建---------------------------------------");
        DefaultMQProducer producer = new DefaultMQProducer(groupName);
        producer.setNamesrvAddr(namesrvAddr);
        producer.setVipChannelEnabled(false);
        producer.setMaxMessageSize(maxMessageSize);
        producer.setSendMsgTimeout(sendMsgTimeOut);
        producer.setRetryTimesWhenSendAsyncFailed(retryTimesWhenSendFailed);
        producer.start();
        log.info("rocketmq producer server 开启成功----------------------------------");
        return producer;
    }

   @Bean("transactionMQProducer")
   @ConditionalOnProperty(prefix = "rocketmq.producer", value = "isOffTransaction", havingValue = "true")
    public TransactionMQProducer transactionMQProducer() throws MQClientException {
       log.info("TransactionMQProducer 正在创建---------------------------------------");
       TransactionMQProducer transactionMQProducer = new TransactionMQProducer(transactionGroupName);
       transactionMQProducer.setNamesrvAddr(namesrvAddr);
       transactionMQProducer.setExecutorService(executorService);
       transactionMQProducer.setTransactionListener(new DefaultTransactionListenerImpl());
       transactionMQProducer.start();
       log.info("TransactionMQProducer 开启成功---------------------------------------");
       return  transactionMQProducer;
   }

}