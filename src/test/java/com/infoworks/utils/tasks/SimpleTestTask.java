package com.infoworks.utils.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.tasks.AbstractTask;
import com.infoworks.tasks.Task;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleTestTask extends AbstractTask {

    private Task nextTask;
    private Message message;
    private Function<Message, Message> converter;
    private Consumer<Message> callback;

    public SimpleTestTask() {}

    public SimpleTestTask(String message) {
        this.message = new Message();
        this.message.setPayload(message);
    }

    public SimpleTestTask(String message, Function<Message, Message> converter) {
        this.message = new Message();
        this.message.setPayload(message);
        this.converter = converter;
    }

    public SimpleTestTask(Consumer<Message> callback) {
        this.message = new Message();
        this.callback = callback;
    }

    @Override
    public Task next() {
        return nextTask;
    }

    @Override
    public void linkedTo(Task task) {
        nextTask = task;
    }

    private static Random RANDOM = new Random();

    @Override
    public Message execute(Message message) throws RuntimeException {
        System.out.println("("+Thread.currentThread().getName()+") Doing jobs..." + getMessage().getPayload());
        Response response = new Response();
        int rand = RANDOM.nextInt(6) + 1;
        try {
            Thread.sleep(rand * 1000);
            response.setStatus(200);
            if (message == null || message.getPayload() == null) {
                response.setMessage("Working!");
                response.setStatus(200 + rand);
            }else{
                response.setMessage(message.getPayload());
                response.setStatus(200);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (callback != null) callback.accept(message);
        return response;
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        System.out.println("("+Thread.currentThread().getName()+") Doing revert ...:" + getMessage().getPayload());
        Response response = new Response();
        int rand = RANDOM.nextInt(3) + 1;
        try {
            Thread.sleep(rand * 1000);
            response.setStatus(500 + rand);
            if (message == null || message.getPayload() == null){
                response.setError("Not Sure why! May be Covid-19");
            }else {
                response.setError(message.getPayload());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (callback != null) callback.accept(message);
        return response;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public Function<Message, Message> getConverter() {
        return converter;
    }
}
