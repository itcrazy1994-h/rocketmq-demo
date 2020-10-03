package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@Slf4j
public class MQConsumeMsgListenerProcessor  implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        if (CollectionUtils.isEmpty(msgList)) {
            log.info("MQ接收消息为空，直接返回成功");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        log.info("mq size : {}",msgList.size());
        MessageExt messageExt = msgList.get(0);
        log.info("MQ接收到的消息为：" + messageExt.toString());
        //重试次数
        int reconsume =  messageExt.getReconsumeTimes();
        try {
            String topic = messageExt.getTopic();
            String tags = messageExt.getTags();
            //一般设置全局唯一id 做重复消费凭证判断
            String keys =  messageExt.getKeys();
            String body = new String(messageExt.getBody(), "utf-8");

            log.info("MQ消息topic={}, tags={}, 消息内容={} ,keys:{} ,消费唯一Id {}", topic,tags,body,keys, messageExt.getTransactionId());
        } catch (Exception e) {
            log.error("获取MQ消息内容异常{}",e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        // TODO 处理业务逻辑
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}
