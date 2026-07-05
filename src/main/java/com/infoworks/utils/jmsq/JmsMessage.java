package com.infoworks.utils.jmsq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.objects.MessageParser;
import com.infoworks.tasks.Task;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Defined:JmsMessage Protocol
 */

public class JmsMessage extends Message {

    private String taskClassName;
    private String messageClassName;
    private String errorClassName;
    private String errorPayload;

    public String getTaskClassName() {
        return taskClassName;
    }

    public JmsMessage setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
        return this;
    }

    public String getMessageClassName() {
        return messageClassName;
    }

    public JmsMessage setMessageClassName(String messageClassName) {
        this.messageClassName = messageClassName;
        return this;
    }

    public String getErrorPayload() {
        return errorPayload;
    }

    public JmsMessage setErrorPayload(String errorPayload) {
        this.errorPayload = errorPayload;
        return this;
    }

    public String getErrorClassName() {
        return errorClassName;
    }

    public JmsMessage setErrorClassName(String errorClassName) {
        this.errorClassName = errorClassName;
        return this;
    }

    public static JmsMessage convert(Task task, ObjectMapper mapper){
        //Defined:JmsMessage Protocol
        JmsMessage jmsMessage = new JmsMessage()
                .setTaskClassName(task.getClass().getName())
                .setMessageClassName(Message.class.getName());
        if (task.getMessage() != null) {
            jmsMessage.setMessageClassName(task.getMessage().getClass().getName())
                    .setPayload(MessageParser.printString(task.getMessage(), mapper));
        }
        return jmsMessage;
    }

    public static JmsMessage convert(Task task, Message error, ObjectMapper mapper){
        //Defined:JmsMessage Protocol
        JmsMessage jmsMessage = convert(task, mapper)
                .setErrorClassName(Message.class.getName());
        if (error != null){
            jmsMessage.setErrorClassName(error.getClass().getName())
                    .setErrorPayload(MessageParser.printString(error, mapper));
        }
        return jmsMessage;
    }

    public static Task revert(String text) throws RuntimeException {
        //Defined:JmsMessage Protocol
        try {
            JmsMessage jmsMessage = MessageParser.unmarshal(JmsMessage.class, text);
            //Task task = (Task) Class.forName(jmsMessage.getTaskClassName()).newInstance();
            Class taskType = Class.forName(jmsMessage.getTaskClassName());
            Task task = (Task) taskType.getDeclaredConstructor().newInstance();
            //
            Class<? extends Message> messageClass = (Class<? extends Message>) Class.forName(jmsMessage.getMessageClassName());
            Message taskMessage = MessageParser.unmarshal(messageClass, jmsMessage.getPayload());
            task.setMessage(taskMessage);
            return task;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
