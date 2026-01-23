package com.infoworks.utils.saga;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.ExecutableTask;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.tasks.stack.TaskStack;
import com.infoworks.utils.eventq.EventQueue;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Saga Pattern is a design pattern used in distributed systems and microservices
 * architecture to manage long-running transactions and ensure data consistency across
 * services without using distributed transactions (2PC).
 */
public class ChoreographedSagaTest {

    /**
     * A saga is a sequence of local transactions. Each transaction updates one service and
     * publishes an event or sends a message to trigger the next step.
     * If a step fails, the saga executes compensating transactions (rollbacks) for the previous steps.
     *
     * Choreography (Event-Based)
     * • Services listen to events and react.
     * • No central coordinator.
     * • Each service emits events to trigger the next service.
     * Pros:
     * • Decentralized
     * • Simple to implement with messaging/event buses
     * Cons:
     * • Harder to manage and monitor
     * • Difficult to debug
     * • Event ordering issues
     *
     * If a step fails, the service must emit a compensating event.
     *
     * Compensation:
     * When something fails, each completed step must undo its work. That’s done with
     * compensating transactions — logic to reverse what was done (e.g., refund payment, restock
     * items).
     * This means you must explicitly code for rollback in each service.
     */


    @Test
    public void choreographedSagaTest() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(7);
        //Service pipelines:
        TaskQueue orderService = new EventQueue(Executors.newFixedThreadPool(3), true);
        TaskQueue paymentService = new EventQueue(Executors.newSingleThreadExecutor());
        TaskQueue shippingService = new EventQueue(Executors.newFixedThreadPool(5), true);
        //
        //Order-Process:
        orderService.onTaskComplete((message, state) -> {
            //Main-Flow:
            if (state == TaskStack.State.Finished && message instanceof OrderResponse) {
                OrderResponse response = (OrderResponse) message;
                if (response.getOptStatus() == OptStatus.CREATE) {
                    paymentService.add(new PaymentTask(response.getOrderID(), response.getMessage()));
                    counter.incrementAndGet();
                } else {
                    counter.decrementAndGet();
                }
            } else {
                //TODO: When Failed
            }
            //Opt:
            if (counter.get() == 1) latch.countDown();
        });
        //
        //Payment-Process:
        paymentService.onTaskComplete((message, state) -> {
            //Main-Flow:
            if (state == TaskStack.State.Finished && message instanceof PaymentResponse) {
                PaymentResponse response = (PaymentResponse) message;
                if (response.getOptStatus() == OptStatus.CREATE) {
                    shippingService.add(new ShipmentTask(response.getOrderID(), response.getPaymentID(), response.getMessage()));
                    counter.incrementAndGet();
                } else if (response.getOptStatus() == OptStatus.CANCEL) {
                    orderService.add(new OrderCancelTask(response.getOrderID(), response.getMessage()));
                    counter.incrementAndGet();
                } else {
                    counter.decrementAndGet();
                }
            } else {
                //TODO: When Failed
            }
            //Opt:
            if (counter.get() == 1) latch.countDown();
        });
        //
        //Shipping-Process:
        shippingService.onTaskComplete((message, state) -> {
            //Main-Flow:
            if (state == TaskStack.State.Finished && message instanceof ShipmentResponse) {
                ShipmentResponse response = (ShipmentResponse) message;
                if (response.getOptStatus() == OptStatus.CREATE) {
                    System.out.println("==>|| Shipping Complete For OrderID:" + response.getOrderID() + " (" + response.getMessage() + ") ||<==");
                    counter.decrementAndGet();
                } else if(response.getOptStatus() == OptStatus.CANCEL) {
                    paymentService.add(new PaymentCancelTask(response.getOrderID(), response.getPaymentID(), response.getMessage()));
                    counter.incrementAndGet();
                } else {
                    counter.decrementAndGet();
                }
            } else {
                //TODO: When Failed
            }
            //Opt:
            if (counter.get() == 1) latch.countDown();
        });
        ///////Choreograph///////
        Random random = new Random();
        int orderId = 0;
        orderService.add(new OrderCreateTask(++orderId + "", "Order For Coffee + Croissant", random.nextBoolean()));
        orderService.add(new OrderCreateTask(++orderId + "", "Order For Biskit & Cake", random.nextBoolean()));
        orderService.add(new OrderCreateTask(++orderId + "", "Order For Glossary", random.nextBoolean()));
        orderService.add(new OrderCreateTask(++orderId + "", "Order For Fruits", random.nextBoolean()));
        orderService.add(new OrderCreateTask(++orderId + "", "Order For Tea + Coffee", random.nextBoolean()));
        orderService.add(new OrderCreateTask(++orderId + "", "Order For Black Coffee", random.nextBoolean()));
        orderService.add(new OrderCreateTask(++orderId + "", "Order For Breakfast", random.nextBoolean()));
        /////////////////////////
        //
        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {}
    }

    /**
     *
     */
    private enum OptStatus { CREATE, CANCEL, NONE}

    /**
     *
     */
    private class OrderResponse extends Response {
        private String orderID;
        private OptStatus optStatus = OptStatus.NONE;

        public String getOrderID() {
            return orderID;
        }

        public OrderResponse setOrderID(String orderID) {
            this.orderID = orderID;
            return this;
        }

        public OptStatus getOptStatus() {
            return optStatus;
        }

        public OrderResponse setOptStatus(OptStatus optStatus) {
            this.optStatus = optStatus;
            return this;
        }
    }

    /**
     *
     */
    private class OrderCreateTask extends ExecutableTask<Message, OrderResponse> {

        //Must need Zero param constructor in Case of JMSTask
        public OrderCreateTask() {}

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

    /**
     *
     */
    private class OrderCancelTask extends ExecutableTask<Message, OrderResponse> {

        public OrderCancelTask() {}

        public OrderCancelTask(String orderId, String message) {
            super(new Property("message", message)
                    , new Property("orderId", orderId));
        }

        @Override
        public OrderResponse execute(Message message) throws RuntimeException {
            String orderId = getPropertyValue("orderId").toString();
            String strMsg = getPropertyValue("message").toString();
            String msg = strMsg + " [ order-id: " + orderId + "] ";
            //True will be Success, failed other-wise:
            System.out.println(msg + "  ==>  " + "Commit: Order Cancel In DB [" + Thread.currentThread().getName() + "]");
            return (OrderResponse) new OrderResponse().setOptStatus(OptStatus.CANCEL).setOrderID(orderId).setStatus(200).setMessage(strMsg);
        }
    }

    /**
     *
     */
    private class PaymentResponse extends Response {
        private String orderID;
        private String paymentID;
        private OptStatus optStatus = OptStatus.NONE;

        public String getOrderID() {
            return orderID;
        }

        public PaymentResponse setOrderID(String orderID) {
            this.orderID = orderID;
            return this;
        }

        public String getPaymentID() {
            return paymentID;
        }

        public PaymentResponse setPaymentID(String paymentID) {
            this.paymentID = paymentID;
            return this;
        }

        public OptStatus getOptStatus() {
            return optStatus;
        }

        public PaymentResponse setOptStatus(OptStatus optStatus) {
            this.optStatus = optStatus;
            return this;
        }
    }

    /**
     *
     */
    private class PaymentTask extends ExecutableTask<Message, PaymentResponse> {

        public PaymentTask() {}

        public PaymentTask(String orderId, String message, boolean nextRandom) {
            super(new Property("message", message)
                    , new Property("orderId", orderId)
                    , new Property("nextRandom", nextRandom));
        }

        public PaymentTask(String orderId, String message) {
            this(orderId, message, new Random().nextBoolean());
        }

        @Override
        public PaymentResponse execute(Message message) throws RuntimeException {
            String orderId = getPropertyValue("orderId").toString();
            String strMsg = getPropertyValue("message").toString();
            String msg = strMsg + " [ order-id: " + orderId + "] ";
            boolean nextRandom = (getPropertyValue("nextRandom") != null)
                    ? Boolean.parseBoolean(getPropertyValue("nextRandom").toString())
                    : true;
            //True will be Success, failed other-wise:
            if (nextRandom) {
                String paymentID = UUID.randomUUID().toString(); //GENERATED FROM DATABASE
                /**
                 * All your payment tasks:
                 */
                System.out.println(msg + "  ==>  " + "Commit: Payment Create In DB [" + Thread.currentThread().getName() + "]");
                return (PaymentResponse) new PaymentResponse().setOptStatus(OptStatus.CREATE).setPaymentID(paymentID).setOrderID(orderId).setStatus(200).setMessage(strMsg);
            } else {
                System.out.println(msg + "  ==>  " + "Commit: Payment Create Failed In DB [" + Thread.currentThread().getName() + "]");
                return (PaymentResponse) new PaymentResponse().setOptStatus(OptStatus.CANCEL).setOrderID(orderId).setStatus(200).setMessage(strMsg);
            }
        }
    }

    /**
     *
     */
    private class PaymentCancelTask extends ExecutableTask<Message, PaymentResponse> {

        public PaymentCancelTask() {}

        public PaymentCancelTask(String orderId, String paymentId, String message) {
            super(new Property("message", message)
                    , new Property("orderId", orderId)
                    , new Property("paymentId", paymentId));
        }

        @Override
        public PaymentResponse execute(Message message) throws RuntimeException {
            String orderId = getPropertyValue("orderId").toString();
            String paymentId = getPropertyValue("paymentId").toString();
            String strMsg = getPropertyValue("message").toString();
            String msg = strMsg + " [ order-id: " + orderId + "] ";
            //True will be Success, failed other-wise:
            System.out.println(msg + "  ==>  " + "Commit: Payment Cancel In DB [" + Thread.currentThread().getName() + "]");
            return (PaymentResponse) new PaymentResponse().setOptStatus(OptStatus.CANCEL).setPaymentID(paymentId).setOrderID(orderId).setStatus(200).setMessage(strMsg);
        }
    }

    /**
     *
     */
    private class ShipmentResponse extends Response {
        private String orderID;
        private String paymentID;
        private String shippingID;
        private OptStatus optStatus = OptStatus.NONE;

        public String getOrderID() {
            return orderID;
        }

        public ShipmentResponse setOrderID(String orderID) {
            this.orderID = orderID;
            return this;
        }

        public String getPaymentID() {
            return paymentID;
        }

        public ShipmentResponse setPaymentID(String paymentID) {
            this.paymentID = paymentID;
            return this;
        }

        public String getShippingID() {
            return shippingID;
        }

        public ShipmentResponse setShippingID(String shippingID) {
            this.shippingID = shippingID;
            return this;
        }

        public OptStatus getOptStatus() {
            return optStatus;
        }

        public ShipmentResponse setOptStatus(OptStatus optStatus) {
            this.optStatus = optStatus;
            return this;
        }
    }

    /**
     *
     */
    private class ShipmentTask extends ExecutableTask<Message, ShipmentResponse> {

        public ShipmentTask() {}

        public ShipmentTask(String orderId, String paymentId, String message, boolean nextRandom) {
            super(new Property("message", message)
                    , new Property("orderId", orderId)
                    , new Property("paymentId", paymentId)
                    , new Property("nextRandom", nextRandom));
        }

        public ShipmentTask(String orderId, String paymentId, String message) {
            this(orderId, paymentId, message, new Random().nextBoolean());
        }

        @Override
        public ShipmentResponse execute(Message message) throws RuntimeException {
            String orderId = getPropertyValue("orderId").toString();
            String paymentId = getPropertyValue("paymentId").toString();
            String strMsg = getPropertyValue("message").toString();
            String msg = strMsg + " [ order-id: " + orderId + "] ";
            boolean nextRandom = (getPropertyValue("nextRandom") != null)
                    ? Boolean.parseBoolean(getPropertyValue("nextRandom").toString())
                    : true;
            //True will be Success, failed other-wise:
            if (nextRandom) {
                String shipmentID = UUID.randomUUID().toString(); //GENERATED FROM DATABASE
                /**
                 * All your shipping tasks:
                 */
                System.out.println(msg + "  ==>  " + "Commit: Shipment Create In DB [" + Thread.currentThread().getName() + "]");
                return (ShipmentResponse) new ShipmentResponse().setOptStatus(OptStatus.CREATE).setShippingID(shipmentID).setPaymentID(paymentId).setOrderID(orderId).setStatus(200).setMessage(strMsg);
            } else {
                System.out.println(msg + "  ==>  " + "Commit: Shipment Create Failed In DB [" + Thread.currentThread().getName() + "]");
                return (ShipmentResponse) new ShipmentResponse().setOptStatus(OptStatus.CANCEL).setPaymentID(paymentId).setOrderID(orderId).setStatus(200).setMessage(strMsg);
            }
        }
    }

    /**
     *
     */
    private class ShipmentCancelTask extends ExecutableTask<Message, ShipmentResponse> {

        public ShipmentCancelTask() {}

        public ShipmentCancelTask(String orderId, String paymentId, String shipmentId, String message) {
            super(new Property("message", message)
                    , new Property("orderId", orderId)
                    , new Property("paymentId", paymentId)
                    , new Property("shipmentId", shipmentId));
        }

        @Override
        public ShipmentResponse execute(Message message) throws RuntimeException {
            String orderId = getPropertyValue("orderId").toString();
            String paymentId = getPropertyValue("paymentId").toString();
            String shipmentId = getPropertyValue("shipmentId").toString();
            String strMsg = getPropertyValue("message").toString();
            String msg = strMsg + " [ order-id: " + orderId + "] ";
            //True will be Success, failed other-wise:
            System.out.println(msg + "  ==>  " + "Commit: Shipment Cancel In DB [" + Thread.currentThread().getName() + "]");
            return (ShipmentResponse) new ShipmentResponse().setOptStatus(OptStatus.CANCEL).setShippingID(shipmentId).setPaymentID(paymentId).setOrderID(orderId).setStatus(200).setMessage(strMsg);
        }
    }
}
