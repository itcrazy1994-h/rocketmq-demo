package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/myTrancProducer")
@Slf4j
public class TrancMQProducerController {
    private static  final String TOPIC_NAME="test_topic_user";

    @Autowired
    @Qualifier("transactionMQProducer")
    TransactionMQProducer transactionMQProducer;

    @GetMapping("/send/{msg}/{param}/{tag}")
    public Object send(@PathVariable("msg")String msg,@PathVariable("param")String param,@PathVariable("tag")String tag){

        Message message = new Message(TOPIC_NAME,tag,param,msg.getBytes());
        SendResult result = null;
        try {
            //消息体 和携带参数
            result =  transactionMQProducer.sendMessageInTransaction(message,param);
        } catch (MQClientException e) {
            e.printStackTrace();
        }
       log.info("生产者发送消息------SendResult: {}",result.toString());
        return result.getMsgId();
    }

}
