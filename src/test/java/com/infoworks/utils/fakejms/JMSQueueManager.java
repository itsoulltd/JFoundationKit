package com.infoworks.utils.fakejms;

import com.infoworks.tasks.Task;
import com.infoworks.tasks.queue.QueuedTaskStateListener;
import com.infoworks.utils.jmsq.AbstractJmsQueueManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JMSQueueManager extends AbstractJmsQueueManager implements JMSBrokerListener {

    private final ExecutorService exeQueue;
    private final ExecutorService abortQueue;

    public JMSQueueManager(QueuedTaskStateListener listener, int numberOfThreads) {
        super(listener);
        numberOfThreads = numberOfThreads <= 0
                ? (Runtime.getRuntime().availableProcessors() / 2)
                : numberOfThreads;
        this.exeQueue = Executors.newFixedThreadPool(numberOfThreads);
        this.abortQueue = Executors.newFixedThreadPool(numberOfThreads);
    }

    @Override
    protected Task createTask(String text) throws ClassNotFoundException, IOException
            , IllegalAccessException, InstantiationException
            , NoSuchMethodException, InvocationTargetException {
        Task task = super.createTask(text);
        //Inject dependency into Task during MOM's task execution.
        return task;
    }

    @Override
    public void startListener(String msg) {
        exeQueue.submit(() -> handleTextOnStart(msg));
    }

    @Override
    public void abortListener(String msg) {
        abortQueue.submit(() -> handleTextOnStop(msg));
    }
}
