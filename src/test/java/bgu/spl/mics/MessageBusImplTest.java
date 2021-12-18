package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Hashtable;

import static org.junit.Assert.*;

public class MessageBusImplTest {

    private MessageBusImpl messageBus;
    private MicroService ms1;
    private MicroService ms2;
    private ExampleEvent event;
    private ExampleBroadcast broad;


    @Before
    public void setUp() throws Exception {
        messageBus = MessageBusImpl.getInstance();
        ms1 = new MicroService("Bob") {
            @Override
            protected void initialize() {
                messageBus.register(ms1);
            }
        };
        ms1.initialize();
        ms2 = new MicroService("Alice") {
            @Override
            protected void initialize() {
                messageBus.register(ms2);
            }
        };
        ms2.initialize();

        event = new ExampleEvent("senderName");
        broad = new ExampleBroadcast("10");
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
        messageBus.subscribeBroadcast(broad.getClass(), ms2);
        ms1.sendBroadcast(broad);
        try {
            Message broad2 = messageBus.awaitMessage(ms2);
            assertTrue(((ExampleBroadcast) broad2).getSenderId().equals(broad.getSenderId()));
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void sendEvent() {
        //This test also checks the subscribeEvent method
        assertNull(ms1.sendEvent(event));  //make sure it asserts null because no microservice has subscribed
        messageBus.subscribeEvent(event.getClass(), ms2);
        Future future = ms1.sendEvent(event);
        try{
            Message event2 = messageBus.awaitMessage(ms2);
            assertTrue(((ExampleEvent)event2).getSenderName().equals(event.getSenderName()));
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
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