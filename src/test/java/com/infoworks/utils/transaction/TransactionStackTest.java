package com.infoworks.utils.transaction;

import com.infoworks.tasks.stack.TaskStack;
import com.infoworks.utils.tasks.SimpleTestTask;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class TransactionStackTest {

    @Test
    public void firstTest() {
        CountDownLatch latch = new CountDownLatch(1);
        TaskStack stack = new TransactionStack();
        //EXE: 4
        stack.push(new SimpleTestTask("Hello bro! I am Hayes", (message) -> {
            System.out.println(message.toString());
            return message;
        }));
        //EXE: 3
        stack.push(new SimpleTestTask("Wow bro! I am Adams"));
        //EXE: 2
        stack.push(new SimpleTestTask("Hi there! I am Cris", (message) -> {
            message.setPayload("Converted Message");
            return message;
        }));
        //EXE: 1
        stack.push(new SimpleTestTask("Let's bro! I am James"));
        //
        stack.commit(false, (result, state) -> {
            System.out.println("State: " + state.name());
            System.out.println(result.toString());
            latch.countDown();
        });
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

}