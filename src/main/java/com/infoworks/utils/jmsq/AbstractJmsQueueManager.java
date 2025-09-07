package com.infoworks.utils.jmsq;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.queue.AbstractQueueManager;
import com.infoworks.tasks.queue.QueuedTaskStateListener;
import com.infoworks.objects.MessageParser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractJmsQueueManager extends AbstractQueueManager {

    protected static Logger LOG = Logger.getLogger(AbstractJmsQueueManager.class.getSimpleName());
    private QueuedTaskStateListener listener;
    public QueuedTaskStateListener getListener() {
        return listener;
    }
    public void setListener(QueuedTaskStateListener queuedTaskLifecycleListener) {
        this.listener = queuedTaskLifecycleListener;
    }

    public AbstractJmsQueueManager(QueuedTaskStateListener listener) {
        this.listener = listener;
    }

    protected Task createTask(String text)
            throws ClassNotFoundException, IOException, IllegalAccessException
            , InstantiationException, NoSuchMethodException, InvocationTargetException {
        //Defined:JmsMessage Protocol
        JmsMessage jmsMessage = MessageParser.unmarshal(JmsMessage.class, text);
        //Task task = (Task) Class.forName(jmsMessage.getTaskClassName()).newInstance();
        Class taskType = Class.forName(jmsMessage.getTaskClassName());
        Task task = (Task) taskType.getDeclaredConstructor().newInstance();
        //
        Class<? extends Message> messageClass = (Class<? extends Message>) Class.forName(jmsMessage.getMessageClassName());
        Message taskMessage = MessageParser.unmarshal(messageClass, jmsMessage.getPayload());
        task.setMessage(taskMessage);
        return task;
    }

    protected boolean handleTextOnStart(String text) throws RuntimeException {
        try {
            Task task = createTask(text);
            start(task, null);
            return true;
        }catch (RuntimeException | IOException
                | ClassNotFoundException
                | IllegalAccessException | InstantiationException
                | NoSuchMethodException | InvocationTargetException e){
            LOG.log(Level.WARNING, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    protected Message getErrorMessage(String text) throws IOException, ClassNotFoundException {
        JmsMessage jmsMessage = MessageParser.unmarshal(JmsMessage.class, text);
        //Handle error-message:
        Class<? extends Message> errorClass = (Class<? extends Message>) Class.forName(jmsMessage.getErrorClassName());
        Message errorMessage = MessageParser.unmarshal(errorClass, jmsMessage.getErrorPayload());
        return errorMessage;
    }

    protected boolean handleTextOnStop(String text) throws RuntimeException {
        try {
            Task task = createTask(text);
            Message errorMessage = getErrorMessage(text);
            stop(task, errorMessage);
            return true;
        }catch (RuntimeException | IOException
                | ClassNotFoundException
                | IllegalAccessException | InstantiationException
                | NoSuchMethodException | InvocationTargetException e){
            LOG.log(Level.WARNING, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Note:
     * Send termination to jms-template for stopping current processing or abandon all active task from
     * e.g. exeQueue, abortQueue, testQueue
     * @param delay
     * @param timeUnit
     */
    @Override
    public void terminateRunningTasks(long delay, TimeUnit timeUnit) {
        /**/
    }

    @Override
    public void close() throws Exception {
        terminateRunningTasks(0l, TimeUnit.SECONDS);
        this.listener = null;
    }

}
