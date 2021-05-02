package com.atguigu.gmall.mq.receiver;

import com.atguigu.gmall.mq.config.DeadLetterMqConfig;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MqTestReceiver {

    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = "a", autoDelete = "false"),
            value = @Queue(value = "c", autoDelete = "false"),
            key = {"b"}
    ))
    public void a(Channel channel, Message message, String c) {
        System.out.println("abc的监听");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        System.out.println(1);

        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SneakyThrows
    @RabbitListener(queues = "queue.dead.2")
    public void c(Channel channel, Message message, String c) throws IOException {
        System.out.println("死信2的监听");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        channel.basicAck(deliveryTag, false);

        // channel.basicNack(1,false,false);

    }

    @SneakyThrows
    @RabbitListener(queues = "queue.delay.1")
    public void d(Channel channel, Message message, String c) throws IOException {
        System.out.println("延迟队列的监听");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        channel.basicAck(deliveryTag, false);

        // channel.basicNack(1,false,false);

    }



//    @SneakyThrows
//    @RabbitListener(queues = "queue.dead.1")
//    public void b(Channel channel, Message message, String c) throws IOException {
//        System.out.println("死信1的监听");
//
//        int i = 1 / 0;
//
//        long deliveryTag = message.getMessageProperties().getDeliveryTag();
//
//        channel.basicNAck(deliveryTag, false);
//
//
//    }


}
