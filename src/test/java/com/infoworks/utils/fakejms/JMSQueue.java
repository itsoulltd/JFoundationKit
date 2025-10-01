package com.infoworks.utils.fakejms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.tasks.stack.TaskStack;
import com.infoworks.utils.jmsq.AbstractJmsQueue;
import com.infoworks.utils.jmsq.JmsMessage;

import java.util.function.BiConsumer;

public class JMSQueue extends AbstractJmsQueue {

    private final JMSBrokerTemplate jmsTemplate;

    public JMSQueue(int numberOfThreads) {
        this.jmsTemplate = new JMSBrokerTemplate(this, numberOfThreads);
    }

    public JMSQueue() {
        this(1);
    }

    private ObjectMapper objectMapper;
    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onTaskComplete(BiConsumer<Message, TaskStack.State> biConsumer) {
        super.onTaskComplete(biConsumer);
    }

    @Override
    public TaskQueue add(Task task) {
        JmsMessage jmsMessage = convert(task);
        //THIS IS FOR SIMULATION for MOM/AMQP/RabbitMQ/ActiveMQ/Redis/Kafka:
        jmsTemplate.convertAndSend(jmsMessage.toString());
        return this;
    }

    @Override
    public void abort(Task task, Message error) {
        JmsMessage jmsMessage = convert(task, error);
        //THIS IS FOR SIMULATION for MOM/AMQP/RabbitMQ/ActiveMQ/Redis/Kafka:
        jmsTemplate.send(jmsMessage.toString());
    }

    @Override
    public TaskQueue cancel(Task task) {
        return this;
    }
}
