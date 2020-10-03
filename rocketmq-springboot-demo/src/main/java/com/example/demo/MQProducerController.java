package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
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
@RequestMapping("/myProducer")
@Slf4j
public class MQProducerController {
    private static  final String TOPIC_NAME="test_topic_user";


    DefaultMQProducer defaultMQProducer;

    @GetMapping("/send/{msg}")
    public Object send(@PathVariable("msg")String msg){

        Message message = new Message(TOPIC_NAME,"tag01",msg.getBytes());
        SendResult result = null;
        try {
            result =  defaultMQProducer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       log.info("生产者发送消息------SendResult: {}",result.toString());
        return result.getMsgId();
    }

    @GetMapping("/asynSend/{msg}")
    public Object asynSend(@PathVariable("msg")String msg) throws RemotingException, MQClientException, InterruptedException {
        int i =0;
        String fullMsg = msg + (i++);
        Message message = new Message(TOPIC_NAME,"tag01",fullMsg.getBytes());
        message.setDelayTimeLevel(1);
        defaultMQProducer.send(message, new DefaultSendCallback());

        return "异步发送";
    }
    @GetMapping("/selectorSend/{msg}/{offset}")
    public Object selectorSend(@PathVariable("msg")String msg,@PathVariable("offset")Integer offset) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        Message message = new Message(TOPIC_NAME,"tag01",msg.getBytes());
        SendResult sendResult =  defaultMQProducer.send(message,new DefaultSelectorQueue(),offset);
        log.info("发送结果 ，{}",sendResult);
        return sendResult.getMsgId();
    }
    @GetMapping("/selectorAysnSend/{msg}/{offset}")
    public Object selectorAysnSend(@PathVariable("msg")String msg,@PathVariable("offset")Integer offset) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {
        Message message = new Message(TOPIC_NAME,"tag01",msg.getBytes());
        defaultMQProducer.send(message,new DefaultSelectorQueue(),offset,new DefaultSendCallback());
        return "异步发送";
    }
   class DefaultSendCallback implements  SendCallback{

       @Override
       public void onSuccess(SendResult sendResult) {
           log.info(sendResult.toString());
       }

       @Override
       public void onException(Throwable e) {
           //todo 通常做法 人工介入，要么就是叫调用次接口的开发人员提供回调方法
           e.printStackTrace();
       }
   }

   class DefaultSelectorQueue implements MessageQueueSelector{

       @Override
       public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
           int offset = Integer.parseInt(arg.toString());
           return mqs.get(offset);
       }
   }

    @Autowired
    @Qualifier("defaultProducer")
    public void setDefaultMQProducer(DefaultMQProducer defaultMQProducer) {
        this.defaultMQProducer = defaultMQProducer;
    }
}
