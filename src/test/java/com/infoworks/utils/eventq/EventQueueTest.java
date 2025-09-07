package com.infoworks.utils.eventq;

import com.infoworks.utils.tasks.AbortTask;
import com.infoworks.utils.tasks.SimpleTestTask;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class EventQueueTest {

    @Test
    public void eventQueueTest() {
        //Initialize:
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(4);
        //
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
        queue.add(new SimpleTestTask("Hi there! I am Cris"));
        queue.add(new SimpleTestTask("Let's bro! I am James"));
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

    @Test
    public void eventConcurrentQueueTest() {
        //Initialize:
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(8);
        //
        EventQueue queue = new EventQueue(Executors.newFixedThreadPool(3));
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
        queue.add(new AbortTask("Hi there! I am aborting 1"));
        queue.add(new SimpleTestTask("Hi there! I am Cris"));
        queue.add(new SimpleTestTask("Let's bro! I am James"));
        queue.add(new AbortTask("Hi there! I am aborting 1"));
        queue.add(new SimpleTestTask("Hi there! I am Cris 2"));
        queue.add(new SimpleTestTask("Hello bro! I am Hayes 2"));
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }
}