package com.infoworks.utils.transaction;

import com.infoworks.tasks.stack.TaskStack;
import com.infoworks.utils.tasks.*;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class TransactionStackTest {

    @Test
    public void stackTest() {
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

    @Test
    public void reverseStackTest(){
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
        stack.commit(true, (result, state) -> {
            System.out.println("State: " + state.name());
            System.out.println(result.toString());
            latch.countDown();
        });
        //
        try {
            latch.await();
        } catch (InterruptedException e) {}
    }

    @Test
    public void stackAbortTest(){
        CountDownLatch latch = new CountDownLatch(1);
        TaskStack stack = new TransactionStack();
        //
        stack.push(new SimpleTestTask("Wow bro! I am Adams")); //EXE: 4
        stack.push(new AbortTask("Hello bro! I am Hayes")); //EXE: 3
        stack.push(new SimpleTestTask("Hi there! I am Cris")); //EXE: 2
        stack.push(new SimpleTestTask("Let's bro! I am James")); //EXE: 1
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

    @Test
    public void loginFlow() {
        TaskStack loginStack = new TransactionStack();
        loginStack.push(new CheckUserExistTask("james@gmail.com"));
        loginStack.push(new LoginTask("james@gmail.com", "432109"));
        loginStack.commit(true, (message, state) -> {
            System.out.println("Login Status: " + state.name());
        });
    }

    @Test
    public void registrationFlow() {
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
        });
    }

    @Test
    public void forgetPassFlow() {
        TaskStack forgetPassStack = new TransactionStack();
        forgetPassStack.push(new CheckUserExistTask("ahmed@yahoo.com"));
        forgetPassStack.push(new ForgotPasswordTask("ahmed@yahoo.com"));
        forgetPassStack.push(new SendEmailTask("xbox-noreply@msn.com"
                , "ahmed@yahoo.com"
                , "Hi There! .... Greetings"
                , "forgot-pass-email-temp-01"));
        forgetPassStack.commit(true, (message, state) -> {
            System.out.println("ForgetPassword Status: " + state.name());
        });
    }

    @Test
    public void resetPassFlow() {
        TaskStack resetPassStack = new TransactionStack();
        resetPassStack.push(new ResetPasswordTask("dadre-3434nndsfd-2323mkj454j5jn-llwer45"
                , "5467123879"
                , "he-he-he-funny:)"));
        resetPassStack.push(new SendEmailTask("xbox-noreply@msn.com"
                , "ahmed@yahoo.com"
                , "Hi There! .... Greetings"
                , "reset-pass-email-temp-01"));
        resetPassStack.commit(true, (message, state) -> {
            System.out.println("ResetPassword Status: " + state.name());
        });
    }

}