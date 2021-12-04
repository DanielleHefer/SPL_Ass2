package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {

    private MessageBusImpl messageBus;
    ExampleEvent event = new ExampleEvent("senderName");
    ExampleBroadcast broad = new ExampleBroadcast("10");
    MicroService ms1;
    MicroService ms2;

    @Before
    public void setUp() throws Exception {
        messageBus = MessageBusImpl.getInstance();
        ms1 = new MicroService("Bob") {
            @Override
            protected void initialize() {
                messageBus.register(this);
            }
        };
        ms2= new MicroService("Alice") {
            @Override
            protected void initialize() {
                messageBus.register(this);
            }
        };
    }

    @Test
    public void subscribeEvent() {
        assertFalse(messageBus.isSubscribedToEvent(event.getClass(),ms1));
        messageBus.subscribeEvent(event.getClass(),ms1);
        assertTrue(messageBus.isSubscribedToEvent(event.getClass(),ms1));
    }

    @Test
    public void subscribeBroadcast() {
        assertFalse(messageBus.isSubscribedToBroadcast(broad.getClass(),ms1));
        messageBus.subscribeBroadcast(broad.getClass(),ms1);
        assertTrue(messageBus.isSubscribedToBroadcast(broad.getClass(),ms1));
    }

    @Test
    public void complete() {
        messageBus.subscribeEvent(event.getClass(), ms2);
        Future<String> future = ms1.sendEvent(event);
        assertFalse(future.isDone());
        String result = "Resolved";
        messageBus.complete(event, result);
        assertTrue(future.isDone());
        assertTrue(future.get().equals(result));
    }

    @Test
    public void sendBroadcast() {
        //This test also checks the subscribeBroadcast method
        messageBus.subscribeBroadcast(broad.getClass(), ms2);
        ms1.sendBroadcast(broad);
        try{
            Message broad2 = messageBus.awaitMessage(ms2);
            assertTrue(((ExampleBroadcast)broad2).getSenderId().equals(broad.getSenderId()));
        }
        catch (InterruptedException k){};
    }

    @Test
    public void sendEvent() {
        //This test also checks the subscribeEvent method
        assertNull(ms1.sendEvent(event));  //make sure it asserts null because no microservice has subscribed
        messageBus.subscribeEvent(event.getClass(), ms2);
        ms1.sendEvent(event);
        try{
            Message event2 = messageBus.awaitMessage(ms2);
            assertTrue(((ExampleEvent)event2).getSenderName().equals(event.getSenderName()));
        }
        catch (InterruptedException k){};
    }

    @Test
    public void register() {
        messageBus.unregister(ms1);
        assertFalse(messageBus.isRegistered(ms1));
        messageBus.register(ms1);
        assertTrue(messageBus.isRegistered(ms1));
    }

    @Test
    public void unregister() {
        assertTrue(messageBus.isRegistered(ms1));
        messageBus.unregister(ms1);
        assertFalse(messageBus.isRegistered(ms1));
    }

    @Test
    public void awaitMessage() {
        // Check the scenario in which the microservice isn't registered to the message bus
        messageBus.unregister(ms2);
        try {
            messageBus.awaitMessage(ms2);
            fail("Exception Expected!");
        }
        catch (IllegalStateException e){
            assertTrue(true);
        } catch (InterruptedException e) {}

        // Check the scenario in which the microservice got the message
        messageBus.register(ms2);
        messageBus.subscribeEvent(event.getClass(),ms2);
        ms1.sendEvent(event);
        ExampleEvent event2 = null;
        try {
            event2 = (ExampleEvent) messageBus.awaitMessage(ms2);
        }
        catch (InterruptedException e) {}
        assertTrue(event.equals(event2));
        assertFalse(messageBus.isMessageInQueue(event, ms2));
    }

    @After
    public void tearDown() throws Exception {
        messageBus.unregister(ms1);
        messageBus.unregister(ms2);
    }
}