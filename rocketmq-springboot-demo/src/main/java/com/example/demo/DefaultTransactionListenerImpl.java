package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
public class DefaultTransactionListenerImpl  implements TransactionListener {
    //executeLocalTransaction 方法来执行本地事务
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
         String body = msg.getBody().toString();
         String keys =  msg.getKeys();
         String topic =  msg.getTopic();
         log.info("事务消息id: {}",msg.getTransactionId());
         log.info("消息内容 : 内容:{}, keys:{} ,主题: {}",body,keys,topic);
         //todo 模拟业务逻辑操作 例如本地订单创建成功
         Integer resultStatus = Integer.parseInt(arg.toString());
         switch (resultStatus){
             case 1:
                 return LocalTransactionState.COMMIT_MESSAGE;
             case 2:
                 return LocalTransactionState.ROLLBACK_MESSAGE;

             default:
                 msg.setBody("消息回调测试".getBytes());
                 return LocalTransactionState.UNKNOW;
         }

    }
    //checkLocalTransaction 方法用于检查本地事务状态
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        log.info("检查消息回调 : {}",msg.toString());

        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
