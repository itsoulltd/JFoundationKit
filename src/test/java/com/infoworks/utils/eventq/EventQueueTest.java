package com.infoworks.utils.eventq;

import com.infoworks.objects.Message;
import com.infoworks.utils.tasks.AbortTask;
import com.infoworks.utils.tasks.SimpleTestTask;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EventQueueTest {

    @Test
    public void syncEventQueueTest() {
        //Initialize:
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(8);
        //Default constructor: async == false;
        EventQueue queue = new EventQueue(Executors.newSingleThreadExecutor());
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
        queue.add(new SimpleTestTask("Wow bro! I am Adams"));
        queue.add(new SimpleTestTask("Hello bro! I am Hayes"));
        queue.add(new AbortTask("Api not available: code-01"));
        queue.add(new SimpleTestTask("Hi there! I am Cris"));
        queue.add(new SimpleTestTask("Let's bro! I am James"));
        queue.add(new AbortTask("Database connection close: code-02"));
        queue.add(new SimpleTestTask("Hi there! I am Cris 2"));
        queue.add(new SimpleTestTask("Hello bro! I am Hayes 2"));
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

    @Test
    public void asyncEventQueueTest() {
        //Initialize:
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(8);
        //For async-queue: async == true
        EventQueue queue = new EventQueue(Executors.newFixedThreadPool(3), true);
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
        queue.add(new SimpleTestTask("Wow bro! I am Adams"));
        queue.add(new SimpleTestTask("Hello bro! I am Hayes"));
        queue.add(new AbortTask("Api not available: code-01"));
        queue.add(new SimpleTestTask("Hi there! I am Cris"));
        queue.add(new SimpleTestTask("Let's bro! I am James"));
        queue.add(new AbortTask("Database connection close: code-02"));
        queue.add(new SimpleTestTask("Hi there! I am Cris 2"));
        queue.add(new SimpleTestTask("Hello bro! I am Hayes 2"));
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

    @Test
    public void eventQueueDispatchTest() throws ExecutionException, InterruptedException {
        //Initialize:
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(1);
        //For async-dispatcher: async == true
        EventQueue dispatcher = new EventQueue(Executors.newFixedThreadPool(3), true);
        //Do dispatching:
        dispatcher.dispatch(300, TimeUnit.MILLISECONDS, () -> new SimpleTestTask("{300ms} Wow bro! I am Adams").execute(null));
        dispatcher.dispatch(440, TimeUnit.MILLISECONDS, () -> new AbortTask("{440ms} Api not available: code-01").execute(null));
        dispatcher.dispatch(200, TimeUnit.MILLISECONDS, () -> new SimpleTestTask("{200ms} Hello bro! I am Hayes").execute(null));
        dispatcher.dispatch(450, TimeUnit.MILLISECONDS, () -> new SimpleTestTask("{450ms} Hi there! I am Cris").execute(null));
        dispatcher.dispatch(130, TimeUnit.MILLISECONDS, () -> new SimpleTestTask("{130ms} Let's bro! I am James").execute(null));
        //Why dispatch(...) is expecting Optional<Future<String>>? The reason behind is, () -> lambda returns String.
        Optional<Future<String>> futureStr = dispatcher.dispatch(270, TimeUnit.MILLISECONDS
                , () -> {
                    System.out.println("{270ms} Executing delayed message.");
                    return "";
                });
        //Most delayed one: 700ms
        //Why dispatch(...) is expecting Optional<Future<Message>>? The reason behind is, execute(null) of Task returns Message.
        Optional<Future<Message>> futureMessage = dispatcher.dispatch(700, TimeUnit.MILLISECONDS
                , () -> new AbortTask("{700ms} Database connection close: code-02").execute(null));
        //futureMessage.orElseThrow(() -> new RuntimeException("RuntimeException!")).get();
        //
        System.out.println("All dispatch(...) called.");
        try {
            latch.await(800, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {}
    }
}