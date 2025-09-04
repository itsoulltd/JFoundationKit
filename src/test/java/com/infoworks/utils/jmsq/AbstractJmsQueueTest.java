package com.infoworks.utils.jmsq;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.AbstractTask;
import com.infoworks.tasks.ExecutableTask;
import com.infoworks.utils.fakejms.JMSQueue;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AbstractJmsQueueTest {

    @Test
    public void jmsQueueTest() {
        //Initialize:
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(4);
        //
        JMSQueue queue = new JMSQueue();
        queue.onTaskComplete((message, state) -> {
            System.out.println("State: " + state.name());
            System.out.println(message.toString());
            if (counter.get() > 1) {
                counter.decrementAndGet();
            } else {
                latch.countDown();
            }
        });
        //Adding Into Queue:
        queue.add(new ExampleTask("Wow bro! I am Adams"));
        queue.add(new ExampleTask("Hello bro! I am Hayes"));
        queue.add(new ExampleTask("Hi there! I am Cris"));
        queue.add(new ExampleTask("Let's bro! I am James"));
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

    @Test
    public void taskSubclassTestInJMSQueue() {
        //Initialize:
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(4);
        //
        JMSQueue jmsQueue = new JMSQueue();
        jmsQueue.onTaskComplete((message, state) -> {
            System.out.println("State: " + state.name());
            System.out.println(message.toString());
            if (counter.get() > 1) {
                counter.decrementAndGet();
            } else {
                latch.countDown();
            }
        });
        //Only 4 - Item should be there!
        jmsQueue.add(new BasicTask());
        jmsQueue.add(new BasicExecutableTask());
        jmsQueue.add(new JMSTaskWithCustomConstructor("James", 29));
        jmsQueue.add(new JMSExeTaskWithCustomConstructor("Sohana", 23));
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

}

///////////////////////////////////////////////////////////////////////////////

class BasicTask extends AbstractTask<Message, Response> {

    @Override
    public Response execute(Message message) throws RuntimeException {
        System.out.println(String.format("%s: %s", BasicTask.class.getSimpleName(), "Success!"));
        return new Response().setStatus(200).setMessage("Success!");
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        System.out.println(String.format("%s: %s", BasicTask.class.getSimpleName(), "Error!"));
        return new Response().setStatus(500).setMessage("Error!");
    }
}

class BasicExecutableTask extends ExecutableTask<Message, Response> {

    @Override
    public Response execute(Message message) throws RuntimeException {
        System.out.println(String.format("%s: %s", BasicExecutableTask.class.getSimpleName(), "Success!"));
        return new Response().setStatus(200).setMessage("Success!");
    }
}

class JMSTaskWithCustomConstructor extends AbstractTask<Message, Response> {

    //Must need Zero param constructor in Case of JMSTask
    public JMSTaskWithCustomConstructor() {}

    public JMSTaskWithCustomConstructor(String name, int age) {
        super(new Property("name", name), new Property("age", age));
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        String name = getPropertyValue("name").toString();
        int age = Integer.valueOf(getPropertyValue("age").toString());
        System.out.println(String.format("%s: Success! %s, %s"
                , JMSTaskWithCustomConstructor.class.getSimpleName(), name, age));
        return new Response().setStatus(200).setMessage(String.format("Success! %s, %s", name, age));
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        System.out.println(String.format("%s: %s", JMSTaskWithCustomConstructor.class.getSimpleName(), "Error!"));
        return new Response().setStatus(500).setMessage("Error!");
    }
}

class JMSExeTaskWithCustomConstructor extends ExecutableTask<Message, Response> {

    //Must need Zero param constructor in Case of JMSTask
    public JMSExeTaskWithCustomConstructor() {}

    public JMSExeTaskWithCustomConstructor(String name, int age) {
        super(new Property("name", name), new Property("age", age));
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        String name = getPropertyValue("name").toString();
        int age = Integer.valueOf(getPropertyValue("age").toString());
        System.out.println(String.format("%s: Success! %s, %s"
                , JMSExeTaskWithCustomConstructor.class.getSimpleName(), name, age));
        return new Response().setStatus(200).setMessage(String.format("Success! %s, %s", name, age));
    }
}

///////////////////////////////////////////////////////////////////////////////