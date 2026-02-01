package com.infoworks.utils.saga;

import com.infoworks.tasks.stack.TaskStack;
import com.infoworks.utils.tasks.*;
import com.infoworks.utils.transaction.TransactionStack;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * The Saga Pattern is a design pattern used in distributed systems and microservices
 * architecture to manage long-running transactions and ensure data consistency across
 * services without using distributed transactions (2PC).
 */

public class OrchestratedSagaTest {

    /**
     * A saga is a sequence of local transactions. Each transaction updates one service and
     * publishes an event or sends a message to trigger the next step.
     * If a step fails, the saga executes compensating transactions (rollbacks) for the previous steps.
     *
     * Orchestration (Command-Based) [e.g. com.infoworks.tasks.Task.java interface can be used as a Commend interface.]
     * • A central orchestrator service manages the saga flow.
     * • It explicitly tells each service what to do next.
     * Pros:
     * • Easier to understand, monitor, and test
     * • Centralized logic
     * Cons:
     * • Introduces coupling to the orchestrator
     * • Orchestrator can become a bottleneck
     *
     * If a step fails, the orchestrator tells previous services to compensate.
     *
     * Compensation:
     * When something fails, each completed step must undo its work. That’s done with
     * compensating transactions — logic to reverse what was done (e.g., refund payment, restock
     * items).
     * This means you must explicitly code for rollback in each service.
     */

    @Test
    public void orchestrationSagaTest() {
        //To create a command we will use Task.java interface.
        //To manage the orchestrator flow we will use TaskStack.java interface (..utils.transaction.TransactionStack.java).
        CountDownLatch latch = new CountDownLatch(1);
        //Saga-Flow:
        TaskStack orchestration = new TransactionStack();
        orchestration.push(new OrderTask("001", "Order For Coffee + Croissant"));
        orchestration.push(new PaymentTask("001", "Order For Coffee + Croissant"));
        orchestration.push(new ShipmentTask("001", "", "Order For Coffee + Croissant"));
        orchestration.commit(true, (message, state) -> {
            System.out.println("OrderProcessing Status: " + state.name());
            latch.countDown();
        });
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

    @Test
    public void orchestrationSagaTestV2() {
        //To create a command we will use Task.java interface.
        //To manage the orchestrator flow we will use TaskStack.java interface (..utils.transaction.TransactionStack.java).
        CountDownLatch latch = new CountDownLatch(1);
        //Saga-Flow:
        TaskStack regStack = new TransactionStack();
        regStack.push(new CheckUserExistTask("ahmed@yahoo.com"));
        regStack.push(new RegistrationTask("ahmed@yahoo.com"
                , "5467123879"
                , "ahmed@yahoo.com"
                , "0101991246"
                , new Date()
                , 32));
        regStack.push(new SendEmailTask("xbox-support@msn.com"
                , "ahmed@yahoo.com"
                , "Hi There! .... Greetings"
                , "new-reg-email-temp-01"));
        regStack.push(new SendSMSTask("01100909001"
                , "01786987908"
                , "Your Registration Completed! Plz check your email."
                , "new-reg-sms-temp-01"));
        regStack.commit(true, (message, state) -> {
            System.out.println("Registration Status: " + state.name());
            latch.countDown();
        });
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

    @Test
    public void orchestrationSagaWithCompensationTest() {
        CountDownLatch latch = new CountDownLatch(1);
        //Saga-Flow:
        TaskStack forgetPassStack = new TransactionStack();
        forgetPassStack.push(new CheckUserExistTask("ahmed@yahoo.com"));
        forgetPassStack.push(new ForgotPasswordTask("ahmed@yahoo.com"));
        forgetPassStack.push(new AbortTask("Caution: Abort-Action!!!")); //EXE: An Abort situation
        forgetPassStack.push(new SendEmailTask("xbox-noreply@msn.com"
                , "ahmed@yahoo.com"
                , "Hi There! .... Greetings"
                , "forgot-pass-email-temp-01"));
        forgetPassStack.commit(true, (message, state) -> {
            System.out.println("ForgetPassword Status: " + state.name());
            System.out.println("Message: " + message);
            latch.countDown();
        });
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }
}
