package com.infoworks.tasks;

import com.infoworks.objects.Message;

import java.util.function.Function;

public class SimpleTask extends AbstractTask {

    private Function<Message, Message> executor;
    private Function<Message, Message> aborted;

    public SimpleTask() {super();}

    public SimpleTask(Function<Message, Message> executor) {
        super(new Message(), null);
        this.executor = executor;
    }

    public SimpleTask(Function<Message, Message> executor
            , Function<Message, Message> aborted) {
        this(executor);
        this.aborted = aborted;
    }

    @Override
    public Message execute(Message message) throws RuntimeException {
        if (executor != null)
            return executor.apply(message);
        return null;
    }

    @Override
    public Message abort(Message message) throws RuntimeException {
        if (aborted != null)
            return aborted.apply(message);
        return null;
    }
}
