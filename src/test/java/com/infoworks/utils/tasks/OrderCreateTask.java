package com.infoworks.utils.tasks;

import com.infoworks.objects.Message;
import com.infoworks.orm.Property;
import com.infoworks.tasks.ExecutableTask;
import com.infoworks.utils.tasks.models.OptStatus;
import com.infoworks.utils.tasks.models.OrderResponse;

import java.util.Random;

/**
 *
 */
public class OrderCreateTask extends ExecutableTask<Message, OrderResponse> {

    //Must need Zero param constructor in Case of JMSTask
    public OrderCreateTask() {
    }

    public OrderCreateTask(String orderId, String message, boolean nextRandom) {
        super(new Property("message", message)
                , new Property("orderId", orderId)
                , new Property("nextRandom", nextRandom));
    }

    public OrderCreateTask(String orderId, String message) {
        this(orderId, message, new Random().nextBoolean());
    }

    @Override
    public OrderResponse execute(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String strMsg = getPropertyValue("message").toString();
        String msg = strMsg + " [ order-id: " + orderId + "] ";
        boolean nextRandom = (getPropertyValue("nextRandom") != null)
                ? Boolean.parseBoolean(getPropertyValue("nextRandom").toString())
                : true;
        //True will be Success, failed other-wise:
        if (nextRandom) {
            System.out.println(msg + "  ==>  " + "Commit: Order Create In DB [" + Thread.currentThread().getName() + "]");
            return (OrderResponse) new OrderResponse().setOptStatus(OptStatus.CREATE).setOrderID(orderId).setStatus(200).setMessage(strMsg);
        } else {
            System.out.println(msg + "  ==>  " + "Commit: Order Create Failed In DB [" + Thread.currentThread().getName() + "]");
            return (OrderResponse) new OrderResponse().setOrderID(orderId).setStatus(500).setMessage(strMsg);
        }
    }
}
